package br.com.naepoca.merchant.service;

import br.com.naepoca.catalog.domain.Produto;
import br.com.naepoca.catalog.repo.ProdutoRepository;
import br.com.naepoca.merchant.domain.*;
import br.com.naepoca.merchant.repo.*;
import br.com.naepoca.seasonality.domain.Recomendacao;
import br.com.naepoca.seasonality.repo.RecomendacaoRepository;
import br.com.naepoca.shared.exception.ApiException;
import br.com.naepoca.shared.security.SecurityUtils;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class MerchantService {

    private final ComercianteRepository comercianteRepository;
    private final CompraRepository compraRepository;
    private final VendaRepository vendaRepository;
    private final PerdaRepository perdaRepository;
    private final LoteEnvioRepository loteEnvioRepository;
    private final ProdutoRepository produtoRepository;
    private final RecomendacaoRepository recomendacaoRepository;

    public MerchantService(
            ComercianteRepository comercianteRepository,
            CompraRepository compraRepository,
            VendaRepository vendaRepository,
            PerdaRepository perdaRepository,
            LoteEnvioRepository loteEnvioRepository,
            ProdutoRepository produtoRepository,
            RecomendacaoRepository recomendacaoRepository
    ) {
        this.comercianteRepository = comercianteRepository;
        this.compraRepository = compraRepository;
        this.vendaRepository = vendaRepository;
        this.perdaRepository = perdaRepository;
        this.loteEnvioRepository = loteEnvioRepository;
        this.produtoRepository = produtoRepository;
        this.recomendacaoRepository = recomendacaoRepository;
    }

    public Comerciante currentMerchant() {
        return comercianteRepository.findByUsuarioId(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NOT_MERCHANT", "Usuário não é comerciante"));
    }

    @Transactional
    public Compra registerPurchase(Map<String, Object> body) {
        Comerciante c = currentMerchant();
        LoteEnvio lote = newLote(c.getId(), "COMPRA", "MANUAL", null, 1);
        Compra compra = new Compra();
        compra.setComercianteId(c.getId());
        compra.setProdutoId(asLong(body.get("produtoId")));
        compra.setKg(asDecimal(body.get("kg")));
        compra.setValorTotal(asDecimal(body.get("valorTotal")));
        compra.setValorUnitarioEnvio(compra.getValorTotal().divide(compra.getKg(), 4, RoundingMode.HALF_UP));
        compra.setDataRef(asDate(body.get("dataRef")));
        compra.setFornecedor((String) body.get("fornecedor"));
        compra.setLoteEnvioId(lote.getId());
        return compraRepository.save(compra);
    }

    @Transactional
    public Venda registerSale(Map<String, Object> body) {
        Comerciante c = currentMerchant();
        LoteEnvio lote = newLote(c.getId(), "VENDA", "MANUAL", null, 1);
        Venda venda = new Venda();
        venda.setComercianteId(c.getId());
        venda.setProdutoId(asLong(body.get("produtoId")));
        venda.setKg(asDecimal(body.get("kg")));
        venda.setValorTotal(asDecimal(body.get("valorTotal")));
        venda.setValorUnitarioEnvio(venda.getValorTotal().divide(venda.getKg(), 4, RoundingMode.HALF_UP));
        venda.setDataRef(asDate(body.get("dataRef")));
        venda.setLoteEnvioId(lote.getId());
        return vendaRepository.save(venda);
    }

    @Transactional
    public Perda registerLoss(Map<String, Object> body) {
        Comerciante c = currentMerchant();
        String motivo = String.valueOf(body.get("motivo")).toUpperCase();
        if (motivo == null || motivo.isBlank() || "null".equals(motivo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MOTIVO_REQUIRED", "Motivo da perda é obrigatório (RN-08)");
        }
        LoteEnvio lote = newLote(c.getId(), "PERDA", "MANUAL", null, 1);
        Perda perda = new Perda();
        perda.setComercianteId(c.getId());
        perda.setProdutoId(asLong(body.get("produtoId")));
        perda.setKg(asDecimal(body.get("kg")));
        perda.setMotivo(motivo);
        if (body.get("valorEstimado") != null) {
            perda.setValorEstimadoEnvio(asDecimal(body.get("valorEstimado")));
        }
        perda.setDataRef(asDate(body.get("dataRef")));
        perda.setLoteEnvioId(lote.getId());
        return perdaRepository.save(perda);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listRecords(String tipo, Long produtoId, LocalDate de, LocalDate ate) {
        Comerciante c = currentMerchant();
        LocalDate ini = de == null ? LocalDate.now().minusMonths(3) : de;
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        Map<String, Object> out = new LinkedHashMap<>();
        if (tipo == null || tipo.equalsIgnoreCase("COMPRA") || tipo.equalsIgnoreCase("ALL")) {
            out.put("compras", compraRepository.findByComercianteIdAndDataRefBetween(c.getId(), ini, fim).stream()
                    .filter(x -> produtoId == null || produtoId.equals(x.getProdutoId())).toList());
        }
        if (tipo == null || tipo.equalsIgnoreCase("VENDA") || tipo.equalsIgnoreCase("ALL")) {
            out.put("vendas", vendaRepository.findByComercianteIdAndDataRefBetween(c.getId(), ini, fim).stream()
                    .filter(x -> produtoId == null || produtoId.equals(x.getProdutoId())).toList());
        }
        if (tipo == null || tipo.equalsIgnoreCase("PERDA") || tipo.equalsIgnoreCase("ALL")) {
            out.put("perdas", perdaRepository.findByComercianteIdAndDataRefBetween(c.getId(), ini, fim).stream()
                    .filter(x -> produtoId == null || produtoId.equals(x.getProdutoId())).toList());
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<LoteEnvio> lots() {
        return loteEnvioRepository.findByComercianteIdOrderByCriadoEmDesc(currentMerchant().getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> lotDetail(Long loteId) {
        Comerciante c = currentMerchant();
        LoteEnvio lote = loteEnvioRepository.findById(loteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "LOT_NOT_FOUND", "Lote não encontrado"));
        if (!lote.getComercianteId().equals(c.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Lote de outro comerciante");
        }
        return Map.of(
                "lote", lote,
                "compras", compraRepository.findByLoteEnvioId(loteId),
                "vendas", vendaRepository.findByLoteEnvioId(loteId),
                "perdas", perdaRepository.findByLoteEnvioId(loteId)
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(LocalDate de, LocalDate ate) {
        Comerciante c = currentMerchant();
        LocalDate ini = de == null ? LocalDate.now().withDayOfMonth(1) : de;
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        List<Compra> compras = compraRepository.findByComercianteIdAndDataRefBetween(c.getId(), ini, fim);
        List<Venda> vendas = vendaRepository.findByComercianteIdAndDataRefBetween(c.getId(), ini, fim);
        List<Perda> perdas = perdaRepository.findByComercianteIdAndDataRefBetween(c.getId(), ini, fim);

        BigDecimal custo = compras.stream().map(Compra::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receita = vendas.stream().map(Venda::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal kgPerda = perdas.stream().map(Perda::getKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorPerda = perdas.stream()
                .map(p -> p.getValorEstimadoEnvio() == null ? BigDecimal.ZERO : p.getValorEstimadoEnvio())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> perdaPorProduto = new HashMap<>();
        for (Perda p : perdas) {
            perdaPorProduto.merge(p.getProdutoId(), p.getKg(), BigDecimal::add);
        }

        List<Map<String, Object>> ranking = perdaPorProduto.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> {
                    String nome = produtoRepository.findById(e.getKey()).map(Produto::getNomeCanonico).orElse("#" + e.getKey());
                    return Map.<String, Object>of("produtoId", e.getKey(), "nome", nome, "kgPerda", e.getValue());
                }).toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("periodo", Map.of("de", ini, "ate", fim));
        out.put("receita", receita);
        out.put("custoCompras", custo);
        out.put("lucroEstimado", receita.subtract(custo).subtract(valorPerda));
        out.put("kgPerda", kgPerda);
        out.put("valorPerda", valorPerda);
        out.put("rankingPerdas", ranking);
        out.put("kgEvitadosEstimado", BigDecimal.ZERO);
        return out;
    }

    @Transactional(readOnly = true)
    public List<Recomendacao> recommendations() {
        Comerciante c = currentMerchant();
        return recomendacaoRepository.findByComercianteIdOrderByGeradoEmDesc(c.getId());
    }

    @Transactional(readOnly = true)
    public String exportCsv(String tipo) {
        Comerciante c = currentMerchant();
        StringBuilder sb = new StringBuilder();
        if ("COMPRA".equalsIgnoreCase(tipo)) {
            sb.append("id,produtoId,kg,valorTotal,valorUnitarioEnvio,dataRef,fornecedor\n");
            for (Compra x : compraRepository.findByComercianteIdOrderByDataRefDesc(c.getId())) {
                sb.append("%d,%d,%s,%s,%s,%s,%s%n".formatted(
                        x.getId(), x.getProdutoId(), x.getKg(), x.getValorTotal(),
                        x.getValorUnitarioEnvio(), x.getDataRef(),
                        x.getFornecedor() == null ? "" : x.getFornecedor()));
            }
        } else if ("VENDA".equalsIgnoreCase(tipo)) {
            sb.append("id,produtoId,kg,valorTotal,valorUnitarioEnvio,dataRef\n");
            for (Venda x : vendaRepository.findByComercianteIdOrderByDataRefDesc(c.getId())) {
                sb.append("%d,%d,%s,%s,%s,%s%n".formatted(
                        x.getId(), x.getProdutoId(), x.getKg(), x.getValorTotal(),
                        x.getValorUnitarioEnvio(), x.getDataRef()));
            }
        } else {
            sb.append("id,produtoId,kg,motivo,valorEstimadoEnvio,dataRef\n");
            for (Perda x : perdaRepository.findByComercianteIdOrderByDataRefDesc(c.getId())) {
                sb.append("%d,%d,%s,%s,%s,%s%n".formatted(
                        x.getId(), x.getProdutoId(), x.getKg(), x.getMotivo(),
                        x.getValorEstimadoEnvio(), x.getDataRef()));
            }
        }
        return sb.toString();
    }

    @Transactional
    public Map<String, Object> importSpreadsheet(MultipartFile file, String tipo) {
        Comerciante c = currentMerchant();
        if (!"PRO".equalsIgnoreCase(c.getPlano())) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "PRO_REQUIRED", "Importação disponível no plano Pro");
        }
        String name = file.getOriginalFilename() == null ? "import" : file.getOriginalFilename().toLowerCase();
        List<Map<String, String>> rows;
        try {
            if (name.endsWith(".xlsx")) {
                rows = readXlsx(file);
            } else if (name.endsWith(".csv")) {
                rows = readCsv(file);
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_TYPE", "Aceito apenas CSV ou XLSX");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARSE_ERROR", "Falha ao ler arquivo: " + e.getMessage());
        }

        LoteEnvio lote = newLote(c.getId(), "IMPORT", "PLANILHA", file.getOriginalFilename(), rows.size());
        int saved = 0;
        for (Map<String, String> row : rows) {
            Long produtoId = Long.valueOf(row.get("produtoId"));
            BigDecimal kg = new BigDecimal(row.get("kg"));
            LocalDate data = LocalDate.parse(row.get("dataRef"));
            if ("COMPRA".equalsIgnoreCase(tipo)) {
                BigDecimal valor = new BigDecimal(row.get("valorTotal"));
                Compra compra = new Compra();
                compra.setComercianteId(c.getId());
                compra.setProdutoId(produtoId);
                compra.setKg(kg);
                compra.setValorTotal(valor);
                compra.setValorUnitarioEnvio(valor.divide(kg, 4, RoundingMode.HALF_UP));
                compra.setDataRef(data);
                compra.setFornecedor(row.getOrDefault("fornecedor", null));
                compra.setLoteEnvioId(lote.getId());
                compraRepository.save(compra);
                saved++;
            } else if ("VENDA".equalsIgnoreCase(tipo)) {
                BigDecimal valor = new BigDecimal(row.get("valorTotal"));
                Venda venda = new Venda();
                venda.setComercianteId(c.getId());
                venda.setProdutoId(produtoId);
                venda.setKg(kg);
                venda.setValorTotal(valor);
                venda.setValorUnitarioEnvio(valor.divide(kg, 4, RoundingMode.HALF_UP));
                venda.setDataRef(data);
                venda.setLoteEnvioId(lote.getId());
                vendaRepository.save(venda);
                saved++;
            } else {
                Perda perda = new Perda();
                perda.setComercianteId(c.getId());
                perda.setProdutoId(produtoId);
                perda.setKg(kg);
                perda.setMotivo(row.getOrDefault("motivo", "OUTRO").toUpperCase());
                perda.setDataRef(data);
                perda.setLoteEnvioId(lote.getId());
                perdaRepository.save(perda);
                saved++;
            }
        }
        lote.setQtdLinhas(saved);
        return Map.of("loteId", lote.getId(), "linhas", saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> praçaComparativo(Long produtoId) {
        Comerciante c = currentMerchant();
        // RN-06: só com consentimento e mínimo 5 — no seed devolvemos estrutura com aviso
        return Map.of(
                "produtoId", produtoId,
                "pracaId", c.getPracaId(),
                "amostraMinima", 5,
                "disponivel", false,
                "motivo", "Comparativo requer ao menos 5 comerciantes consentidos na praça (RN-06)."
        );
    }

    private LoteEnvio newLote(Long comercianteId, String tipo, String origem, String arquivo, int qtd) {
        LoteEnvio lote = new LoteEnvio();
        lote.setComercianteId(comercianteId);
        lote.setTipo(tipo);
        lote.setOrigem(origem);
        lote.setArquivoNome(arquivo);
        lote.setQtdLinhas(qtd);
        return loteEnvioRepository.save(lote);
    }

    private List<Map<String, String>> readCsv(MultipartFile file) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> all = reader.readAll();
            if (all.isEmpty()) return List.of();
            String[] header = all.getFirst();
            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i < all.size(); i++) {
                Map<String, String> map = new HashMap<>();
                for (int c = 0; c < header.length && c < all.get(i).length; c++) {
                    map.put(header[c].trim(), all.get(i)[c].trim());
                }
                rows.add(map);
            }
            return rows;
        }
    }

    private List<Map<String, String>> readXlsx(MultipartFile file) throws Exception {
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> it = sheet.iterator();
            if (!it.hasNext()) return List.of();
            Row headerRow = it.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) headers.add(cell.getStringCellValue().trim());
            List<Map<String, String>> rows = new ArrayList<>();
            DataFormatter fmt = new DataFormatter();
            while (it.hasNext()) {
                Row row = it.next();
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    map.put(headers.get(i), fmt.formatCellValue(row.getCell(i)));
                }
                rows.add(map);
            }
            return rows;
        }
    }

    private static Long asLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        return Long.valueOf(String.valueOf(v));
    }

    private static BigDecimal asDecimal(Object v) {
        return new BigDecimal(String.valueOf(v));
    }

    private static LocalDate asDate(Object v) {
        if (v == null) return LocalDate.now();
        return LocalDate.parse(String.valueOf(v));
    }
}
