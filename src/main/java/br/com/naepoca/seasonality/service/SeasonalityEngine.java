package br.com.naepoca.seasonality.service;

import br.com.naepoca.catalog.domain.Produto;
import br.com.naepoca.catalog.repo.ProdutoRepository;
import br.com.naepoca.market.domain.IpcaMensal;
import br.com.naepoca.market.domain.PrecoMercado;
import br.com.naepoca.market.repo.IpcaMensalRepository;
import br.com.naepoca.market.repo.PrecoMercadoRepository;
import br.com.naepoca.merchant.domain.Comerciante;
import br.com.naepoca.merchant.domain.Venda;
import br.com.naepoca.merchant.repo.ComercianteRepository;
import br.com.naepoca.merchant.repo.VendaRepository;
import br.com.naepoca.seasonality.domain.IndiceSazonal;
import br.com.naepoca.seasonality.domain.Recomendacao;
import br.com.naepoca.seasonality.repo.IndiceSazonalRepository;
import br.com.naepoca.seasonality.repo.RecomendacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeasonalityEngine {

    private final PrecoMercadoRepository precoMercadoRepository;
    private final IpcaMensalRepository ipcaMensalRepository;
    private final IndiceSazonalRepository indiceSazonalRepository;
    private final RecomendacaoRepository recomendacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final ComercianteRepository comercianteRepository;
    private final VendaRepository vendaRepository;

    public SeasonalityEngine(
            PrecoMercadoRepository precoMercadoRepository,
            IpcaMensalRepository ipcaMensalRepository,
            IndiceSazonalRepository indiceSazonalRepository,
            RecomendacaoRepository recomendacaoRepository,
            ProdutoRepository produtoRepository,
            ComercianteRepository comercianteRepository,
            VendaRepository vendaRepository
    ) {
        this.precoMercadoRepository = precoMercadoRepository;
        this.ipcaMensalRepository = ipcaMensalRepository;
        this.indiceSazonalRepository = indiceSazonalRepository;
        this.recomendacaoRepository = recomendacaoRepository;
        this.produtoRepository = produtoRepository;
        this.comercianteRepository = comercianteRepository;
        this.vendaRepository = vendaRepository;
    }

    @Transactional
    public int recalculateAll() {
        Map<String, BigDecimal> ipca = ipcaMensalRepository.findAllByOrderByAnoMesAsc().stream()
                .collect(Collectors.toMap(IpcaMensal::getAnoMes, IpcaMensal::getIndice, (a, b) -> b, LinkedHashMap::new));
        BigDecimal ipcaRef = ipca.values().stream().reduce((a, b) -> b).orElse(BigDecimal.valueOf(100));

        int count = 0;
        for (Produto produto : produtoRepository.findByAtivoTrue()) {
            for (Long pracaId : List.of(1L)) {
                List<PrecoMercado> serie = precoMercadoRepository
                        .findByProdutoIdAndPracaIdAndDataRefBetweenOrderByDataRefAsc(
                                produto.getId(), pracaId, LocalDate.now().minusYears(5), LocalDate.now());
                count += upsertIndices(produto.getId(), pracaId, null, "PRECO", serie, ipca, ipcaRef, true);
                count += upsertIndices(produto.getId(), pracaId, null, "OFERTA", serie, ipca, ipcaRef, false);
            }
        }

        for (Comerciante c : comercianteRepository.findAll()) {
            Map<Long, List<Venda>> byProduct = vendaRepository
                    .findByComercianteIdAndDataRefBetween(c.getId(), LocalDate.now().minusYears(3), LocalDate.now())
                    .stream().collect(Collectors.groupingBy(Venda::getProdutoId));
            for (Map.Entry<Long, List<Venda>> e : byProduct.entrySet()) {
                count += upsertSalesIndex(e.getKey(), c.getPracaId(), c.getId(), e.getValue());
            }
            generateRecommendations(c);
        }
        generatePublicRecommendations();
        return count;
    }

    private int upsertIndices(
            Long produtoId, Long pracaId, Long comercianteId, String tipo,
            List<PrecoMercado> serie, Map<String, BigDecimal> ipca, BigDecimal ipcaRef, boolean price
    ) {
        Map<Integer, List<BigDecimal>> byMonth = new HashMap<>();
        Map<Integer, Set<Integer>> years = new HashMap<>();
        for (PrecoMercado pm : serie) {
            String ym = "%04d-%02d".formatted(pm.getDataRef().getYear(), pm.getDataRef().getMonthValue());
            BigDecimal raw = price ? pm.getPrecoMedio() :
                    (pm.getVolumeKg() == null ? BigDecimal.ONE : pm.getVolumeKg());
            BigDecimal defl = price
                    ? raw.multiply(ipcaRef).divide(ipca.getOrDefault(ym, ipcaRef), 6, RoundingMode.HALF_UP)
                    : raw;
            int m = pm.getDataRef().getMonthValue();
            byMonth.computeIfAbsent(m, k -> new ArrayList<>()).add(defl);
            years.computeIfAbsent(m, k -> new HashSet<>()).add(pm.getDataRef().getYear());
        }

        Map<Integer, BigDecimal> yearAvg = new HashMap<>();
        Map<Integer, List<BigDecimal>> byYear = new HashMap<>();
        for (PrecoMercado pm : serie) {
            String ym = "%04d-%02d".formatted(pm.getDataRef().getYear(), pm.getDataRef().getMonthValue());
            BigDecimal raw = price ? pm.getPrecoMedio() :
                    (pm.getVolumeKg() == null ? BigDecimal.ONE : pm.getVolumeKg());
            BigDecimal defl = price
                    ? raw.multiply(ipcaRef).divide(ipca.getOrDefault(ym, ipcaRef), 6, RoundingMode.HALF_UP)
                    : raw;
            byYear.computeIfAbsent(pm.getDataRef().getYear(), k -> new ArrayList<>()).add(defl);
        }
        byYear.forEach((y, vals) -> yearAvg.put(y, avg(vals)));

        int saved = 0;
        for (int mes = 1; mes <= 12; mes++) {
            final int mesAtual = mes;
            List<BigDecimal> ratios = new ArrayList<>();
            Set<Integer> usedYears = years.getOrDefault(mesAtual, Set.of());
            for (Integer year : usedYears) {
                final int yearAtual = year;
                List<BigDecimal> monthVals = serie.stream()
                        .filter(pm -> pm.getDataRef().getYear() == yearAtual && pm.getDataRef().getMonthValue() == mesAtual)
                        .map(pm -> {
                            String ym = "%04d-%02d".formatted(pm.getDataRef().getYear(), pm.getDataRef().getMonthValue());
                            BigDecimal raw = price ? pm.getPrecoMedio() :
                                    (pm.getVolumeKg() == null ? BigDecimal.ONE : pm.getVolumeKg());
                            return price
                                    ? raw.multiply(ipcaRef).divide(ipca.getOrDefault(ym, ipcaRef), 6, RoundingMode.HALF_UP)
                                    : raw;
                        }).toList();
                if (monthVals.isEmpty() || !yearAvg.containsKey(yearAtual) || yearAvg.get(yearAtual).signum() == 0) continue;
                ratios.add(avg(monthVals).divide(yearAvg.get(yearAtual), 6, RoundingMode.HALF_UP));
            }
            if (ratios.isEmpty()) continue;
            BigDecimal is = avg(ratios);
            // RN-01 uses months of history; use distinct month-year count
            int monthCount = (int) serie.stream()
                    .map(pm -> pm.getDataRef().withDayOfMonth(1))
                    .distinct().count();
            String conf = confidence(monthCount);

            IndiceSazonal idx = indiceSazonalRepository.findByProdutoIdAndPracaIdAndTipo(produtoId, pracaId, tipo)
                    .stream().filter(i -> Objects.equals(i.getMes(), mesAtual)
                            && Objects.equals(i.getComercianteId(), comercianteId))
                    .findFirst().orElseGet(IndiceSazonal::new);
            idx.setProdutoId(produtoId);
            idx.setPracaId(pracaId);
            idx.setComercianteId(comercianteId);
            idx.setMes(mesAtual);
            idx.setTipo(tipo);
            idx.setValor(is.setScale(4, RoundingMode.HALF_UP));
            idx.setAnosUsados(Math.max(1, usedYears.size()));
            idx.setConfianca(conf);
            idx.setCalculadoEm(Instant.now());
            indiceSazonalRepository.save(idx);
            saved++;
        }
        return saved;
    }

    private int upsertSalesIndex(Long produtoId, Long pracaId, Long comercianteId, List<Venda> vendas) {
        Map<Integer, List<BigDecimal>> byMonth = new HashMap<>();
        Map<Integer, Set<Integer>> years = new HashMap<>();
        for (Venda v : vendas) {
            byMonth.computeIfAbsent(v.getDataRef().getMonthValue(), k -> new ArrayList<>()).add(v.getKg());
            years.computeIfAbsent(v.getDataRef().getMonthValue(), k -> new HashSet<>()).add(v.getDataRef().getYear());
        }
        Map<Integer, BigDecimal> yearAvg = new HashMap<>();
        Map<Integer, List<BigDecimal>> byYear = new HashMap<>();
        for (Venda v : vendas) {
            byYear.computeIfAbsent(v.getDataRef().getYear(), k -> new ArrayList<>()).add(v.getKg());
        }
        byYear.forEach((y, vals) -> yearAvg.put(y, avg(vals)));

        int saved = 0;
        int monthCount = (int) vendas.stream().map(v -> v.getDataRef().withDayOfMonth(1)).distinct().count();
        for (int mes = 1; mes <= 12; mes++) {
            final int mesAtual = mes;
            List<BigDecimal> ratios = new ArrayList<>();
            for (Integer year : years.getOrDefault(mesAtual, Set.of())) {
                final int yearAtual = year;
                BigDecimal monthAvg = avg(vendas.stream()
                        .filter(v -> v.getDataRef().getYear() == yearAtual && v.getDataRef().getMonthValue() == mesAtual)
                        .map(Venda::getKg).toList());
                BigDecimal ya = yearAvg.get(yearAtual);
                if (ya == null || ya.signum() == 0) continue;
                ratios.add(monthAvg.divide(ya, 6, RoundingMode.HALF_UP));
            }
            if (ratios.isEmpty()) continue;
            IndiceSazonal idx = indiceSazonalRepository.findByComercianteIdAndTipo(comercianteId, "VENDAS")
                    .stream().filter(i -> Objects.equals(i.getMes(), mesAtual) && Objects.equals(i.getProdutoId(), produtoId))
                    .findFirst().orElseGet(IndiceSazonal::new);
            idx.setProdutoId(produtoId);
            idx.setPracaId(pracaId);
            idx.setComercianteId(comercianteId);
            idx.setMes(mesAtual);
            idx.setTipo("VENDAS");
            idx.setValor(avg(ratios).setScale(4, RoundingMode.HALF_UP));
            idx.setAnosUsados(Math.max(1, years.getOrDefault(mes, Set.of()).size()));
            idx.setConfianca(confidence(monthCount));
            idx.setCalculadoEm(Instant.now());
            indiceSazonalRepository.save(idx);
            saved++;
        }
        return saved;
    }

    private void generateRecommendations(Comerciante c) {
        int nextMonth = LocalDate.now().plusMonths(1).getMonthValue();
        int currentMonth = LocalDate.now().getMonthValue();
        for (Produto p : produtoRepository.findByAtivoTrue()) {
            Optional<IndiceSazonal> preco = findIdx(p.getId(), c.getPracaId(), null, "PRECO", nextMonth);
            Optional<IndiceSazonal> oferta = findIdx(p.getId(), c.getPracaId(), null, "OFERTA", nextMonth);
            Optional<IndiceSazonal> vendas = findIdx(p.getId(), c.getPracaId(), c.getId(), "VENDAS", nextMonth == 12 ? 1 : nextMonth);

            if (preco.isEmpty() || "INSUFICIENTE".equals(preco.get().getConfianca())) {
                continue; // RN-01
            }
            if (preco.get().getValor().doubleValue() < 0.90
                    && oferta.map(i -> i.getValor().doubleValue() > 1.05).orElse(false)) {
                saveRec(c.getId(), p.getId(), "JANELA_COMPRA",
                        "Boa época para comprar " + p.getNomeCanonico(),
                        "Janela de compra: preço abaixo da média e oferta elevada no próximo mês.",
                        preco.get().getConfianca(),
                        Map.of("is_preco", preco.get().getValor(), "is_oferta", oferta.map(IndiceSazonal::getValor).orElse(null),
                                "mes", nextMonth, "fonte", "PROHORT/SEED"));
            }
            if (vendas.isPresent() && vendas.get().getValor().doubleValue() > 1.30
                    && !"INSUFICIENTE".equals(vendas.get().getConfianca())) {
                saveRec(c.getId(), p.getId(), "PICO_DEMANDA",
                        "Prepare o estoque de " + p.getNomeCanonico(),
                        "Seu pico de vendas costuma ocorrer no próximo mês.",
                        vendas.get().getConfianca(),
                        Map.of("is_vendas", vendas.get().getValor(), "mes", nextMonth));
            }
            Optional<IndiceSazonal> precoAtual = findIdx(p.getId(), c.getPracaId(), null, "PRECO", currentMonth);
            if (precoAtual.isPresent() && precoAtual.get().getValor().doubleValue() > 1.20) {
                saveRec(c.getId(), p.getId(), "PRECO_ALTO",
                        p.getNomeCanonico() + " acima do normal",
                        "Mais caro que o normal; veja uma alternativa em safra.",
                        precoAtual.get().getConfianca(),
                        Map.of("is_preco", precoAtual.get().getValor(), "mes", currentMonth));
            }
        }
    }

    private void generatePublicRecommendations() {
        int mes = LocalDate.now().getMonthValue();
        for (Produto p : produtoRepository.findByAtivoTrue()) {
            Optional<IndiceSazonal> preco = findIdx(p.getId(), 1L, null, "PRECO", mes);
            Optional<IndiceSazonal> oferta = findIdx(p.getId(), 1L, null, "OFERTA", mes);
            if (preco.isEmpty() || "INSUFICIENTE".equals(preco.get().getConfianca())) continue;
            if (preco.get().getValor().doubleValue() < 0.90
                    && oferta.map(i -> i.getValor().doubleValue() > 1.05).orElse(false)) {
                saveRec(null, p.getId(), "EM_SAFRA",
                        p.getNomeCanonico() + " em época",
                        "Preço abaixo da média e oferta elevada na praça de referência.",
                        preco.get().getConfianca(),
                        Map.of("is_preco", preco.get().getValor(), "is_oferta", oferta.map(IndiceSazonal::getValor).orElse(null)));
            }
        }
    }

    private Optional<IndiceSazonal> findIdx(Long produtoId, Long pracaId, Long comercianteId, String tipo, int mes) {
        List<IndiceSazonal> list = comercianteId == null
                ? indiceSazonalRepository.findByProdutoIdAndPracaIdAndTipo(produtoId, pracaId, tipo)
                : indiceSazonalRepository.findByComercianteIdAndTipo(comercianteId, tipo).stream()
                .filter(i -> Objects.equals(i.getProdutoId(), produtoId)).toList();
        return list.stream().filter(i -> Objects.equals(i.getMes(), mes)).findFirst();
    }

    private void saveRec(Long comercianteId, Long produtoId, String tipo, String titulo, String msg,
                         String confianca, Map<String, Object> dados) {
        Recomendacao r = new Recomendacao();
        r.setComercianteId(comercianteId);
        r.setProdutoId(produtoId);
        r.setTipo(tipo);
        r.setTitulo(titulo);
        r.setMensagem(msg);
        r.setConfianca(confianca);
        r.setDadosJson(dados);
        r.setPeriodoInicio(LocalDate.now().withDayOfMonth(1));
        r.setPeriodoFim(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        r.setFonte("ENGINE");
        recomendacaoRepository.save(r);
    }

    private static String confidence(int months) {
        if (months >= 36) return "ALTA";
        if (months >= 24) return "MEDIA";
        if (months >= 12) return "BAIXA";
        return "INSUFICIENTE";
    }

    private static BigDecimal avg(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 6, RoundingMode.HALF_UP);
    }
}
