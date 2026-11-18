package br.com.naepoca.catalog.service;

import br.com.naepoca.catalog.domain.EventoSazonal;
import br.com.naepoca.catalog.domain.Produto;
import br.com.naepoca.catalog.repo.EventoSazonalRepository;
import br.com.naepoca.catalog.repo.ProdutoRepository;
import br.com.naepoca.catalog.repo.ProdutoSinonimoRepository;
import br.com.naepoca.market.domain.PrecoMercado;
import br.com.naepoca.market.domain.Producao;
import br.com.naepoca.market.repo.PrecoMercadoRepository;
import br.com.naepoca.market.repo.ProducaoRepository;
import br.com.naepoca.seasonality.domain.IndiceSazonal;
import br.com.naepoca.seasonality.repo.IndiceSazonalRepository;
import br.com.naepoca.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
public class CatalogService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoSinonimoRepository sinonimoRepository;
    private final PrecoMercadoRepository precoMercadoRepository;
    private final ProducaoRepository producaoRepository;
    private final IndiceSazonalRepository indiceSazonalRepository;
    private final EventoSazonalRepository eventoSazonalRepository;
    private final JdbcTemplate jdbcTemplate;

    public CatalogService(
            ProdutoRepository produtoRepository,
            ProdutoSinonimoRepository sinonimoRepository,
            PrecoMercadoRepository precoMercadoRepository,
            ProducaoRepository producaoRepository,
            IndiceSazonalRepository indiceSazonalRepository,
            EventoSazonalRepository eventoSazonalRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.produtoRepository = produtoRepository;
        this.sinonimoRepository = sinonimoRepository;
        this.precoMercadoRepository = precoMercadoRepository;
        this.producaoRepository = producaoRepository;
        this.indiceSazonalRepository = indiceSazonalRepository;
        this.eventoSazonalRepository = eventoSazonalRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> search(String q, String categoria, String epoca) {
        List<Produto> produtos = produtoRepository.search(
                q == null || q.isBlank() ? null : q.trim(),
                categoria == null || categoria.isBlank() ? null : categoria.trim().toUpperCase());
        int mes = LocalDate.now().getMonthValue();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Produto p : produtos) {
            String selo = seasonLabel(p.getId(), 1L, mes);
            if (epoca != null && !epoca.isBlank() && !selo.equalsIgnoreCase(epoca)) {
                continue;
            }
            Optional<PrecoMercado> preco = precoMercadoRepository
                    .findFirstByProdutoIdAndPracaIdOrderByDataRefDesc(p.getId(), 1L);
            Optional<Producao> producao = producaoRepository.findByProdutoIdOrderByAnoDesc(p.getId()).stream().findFirst();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("nome", p.getNomeCanonico());
            item.put("categoria", p.getCategoria());
            item.put("unidadeBase", p.getUnidadeBase());
            item.put("seloEpoca", selo);
            item.put("precoAtacado", preco.map(PrecoMercado::getPrecoMedio).orElse(null));
            item.put("fontePreco", preco.map(PrecoMercado::getFonte).orElse(null));
            item.put("producaoSpT", producao.map(Producao::getProducaoT).orElse(null));
            item.put("producaoAno", producao.map(Producao::getAno).orElse(null));
            item.put("fonteProducao", producao.map(Producao::getFonte).orElse(null));
            out.add(item);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> productDetail(Long id, Long pracaId) {
        Produto p = produtoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Produto não encontrado"));
        Long praca = pracaId == null ? 1L : pracaId;
        LocalDate fim = LocalDate.now();
        LocalDate ini = fim.minusMonths(12);
        List<PrecoMercado> precos = precoMercadoRepository
                .findByProdutoIdAndPracaIdAndDataRefBetweenOrderByDataRefAsc(id, praca, ini, fim);
        List<IndiceSazonal> indices = indiceSazonalRepository.findByProdutoIdAndPracaIdAndTipo(id, praca, "PRECO");
        if (indices.isEmpty()) {
            indices = indiceSazonalRepository.findByProdutoIdAndTipoAndComercianteIdIsNull(id, "PRECO");
        }
        List<String> sinonimos = sinonimoRepository.findByProdutoId(id).stream().map(s -> s.getSinonimo()).toList();
        List<Map<String, Object>> substitutos = jdbcTemplate.query(
                """
                select p.id, p.nome_canonico
                from produto_substituto ps
                join produto p on p.id = ps.substituto_id
                where ps.produto_id = ?
                """,
                (rs, i) -> Map.<String, Object>of("id", rs.getLong("id"), "nome", rs.getString("nome_canonico")),
                id);
        String selo = seasonLabel(id, praca, LocalDate.now().getMonthValue());
        Optional<PrecoMercado> atual = precoMercadoRepository.findFirstByProdutoIdAndPracaIdOrderByDataRefDesc(id, praca);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", p.getId());
        body.put("nome", p.getNomeCanonico());
        body.put("categoria", p.getCategoria());
        body.put("unidadeBase", p.getUnidadeBase());
        body.put("sinonimos", sinonimos);
        body.put("seloEpoca", selo);
        body.put("precoAtualAtacado", atual.map(PrecoMercado::getPrecoMedio).orElse(null));
        body.put("precoAtualData", atual.map(PrecoMercado::getDataRef).orElse(null));
        body.put("fontePreco", atual.map(PrecoMercado::getFonte).orElse(null));
        List<Producao> producoes = producaoRepository.findByProdutoIdOrderByAnoDesc(id);
        body.put("producao", producoes.stream().map(pr -> Map.of(
                "ano", pr.getAno(),
                "uf", pr.getUf(),
                "producaoT", pr.getProducaoT(),
                "fonte", pr.getFonte()
        )).toList());
        body.put("aviso", "Preço de Ceasa é referência de atacado, não preço de loja (RN-02).");
        body.put("seriePrecos", precos.stream().map(pm -> Map.of(
                "data", pm.getDataRef(),
                "precoMedio", pm.getPrecoMedio(),
                "volumeKg", pm.getVolumeKg(),
                "fonte", pm.getFonte()
        )).toList());
        body.put("calendario12Meses", indices.stream()
                .sorted(Comparator.comparing(IndiceSazonal::getMes))
                .map(i -> Map.of(
                        "mes", i.getMes(),
                        "mesNome", Month.of(i.getMes()).name(),
                        "indicePreco", i.getValor(),
                        "confianca", i.getConfianca(),
                        "selo", labelFromIndex(i.getValor())
                )).toList());
        body.put("substitutos", substitutos);
        return body;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> seasonsNow(String uf) {
        int mes = LocalDate.now().getMonthValue();
        Long pracaId = 1L;
        List<Map<String, Object>> emSafra = new ArrayList<>();
        List<Map<String, Object>> caros = new ArrayList<>();
        for (Produto p : produtoRepository.findByAtivoTrue()) {
            Optional<IndiceSazonal> preco = indiceSazonalRepository.findByProdutoIdAndPracaIdAndTipo(p.getId(), pracaId, "PRECO")
                    .stream().filter(i -> i.getMes() == mes).findFirst();
            Optional<IndiceSazonal> oferta = indiceSazonalRepository.findByProdutoIdAndPracaIdAndTipo(p.getId(), pracaId, "OFERTA")
                    .stream().filter(i -> i.getMes() == mes).findFirst();
            BigDecimal isPreco = preco.map(IndiceSazonal::getValor).orElse(BigDecimal.ONE);
            BigDecimal isOferta = oferta.map(IndiceSazonal::getValor).orElse(BigDecimal.ONE);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("produtoId", p.getId());
            item.put("nome", p.getNomeCanonico());
            item.put("categoria", p.getCategoria());
            item.put("indicePreco", isPreco);
            item.put("indiceOferta", isOferta);
            item.put("confianca", preco.map(IndiceSazonal::getConfianca).orElse("INSUFICIENTE"));
            if (isPreco.doubleValue() < 0.90 && isOferta.doubleValue() > 1.05) {
                item.put("selo", "EM_SAFRA");
                emSafra.add(item);
            } else if (isPreco.doubleValue() > 1.20) {
                item.put("selo", "PRECO_ALTO");
                caros.add(item);
            }
        }
        return Map.of(
                "uf", uf == null ? "SP" : uf,
                "mes", mes,
                "emSafra", emSafra,
                "precoAcimaDoNormal", caros,
                "atualizadoEm", LocalDate.now()
        );
    }

    @Transactional(readOnly = true)
    public List<EventoSazonal> eventos() {
        return eventoSazonalRepository.findAll();
    }

    private String seasonLabel(Long produtoId, Long pracaId, int mes) {
        return indiceSazonalRepository.findByProdutoIdAndPracaIdAndTipo(produtoId, pracaId, "PRECO").stream()
                .filter(i -> i.getMes() == mes)
                .findFirst()
                .map(i -> labelFromIndex(i.getValor()))
                .orElse("NEUTRO");
    }

    private String labelFromIndex(BigDecimal valor) {
        double v = valor.doubleValue();
        if (v < 0.90) return "EM_SAFRA";
        if (v > 1.20) return "FORA_DE_EPOCA";
        return "NEUTRO";
    }
}
