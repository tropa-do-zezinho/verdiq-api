package br.com.naepoca.etl.service;

import br.com.naepoca.admin.domain.AuditLog;
import br.com.naepoca.admin.repo.AuditLogRepository;
import br.com.naepoca.catalog.domain.Produto;
import br.com.naepoca.catalog.repo.ProdutoRepository;
import br.com.naepoca.etl.connector.*;
import br.com.naepoca.etl.domain.EtlRaw;
import br.com.naepoca.etl.domain.EtlRun;
import br.com.naepoca.etl.domain.MarketUpload;
import br.com.naepoca.etl.repo.EtlRawRepository;
import br.com.naepoca.etl.repo.EtlRunRepository;
import br.com.naepoca.etl.repo.MarketUploadRepository;
import br.com.naepoca.market.domain.PrecoMercado;
import br.com.naepoca.market.repo.PrecoMercadoRepository;
import br.com.naepoca.seasonality.service.SeasonalityEngine;
import br.com.naepoca.shared.exception.ApiException;
import br.com.naepoca.shared.security.SecurityUtils;
import com.opencsv.CSVReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class EtlService {

    private final EtlRunRepository etlRunRepository;
    private final EtlRawRepository etlRawRepository;
    private final MarketUploadRepository marketUploadRepository;
    private final AuditLogRepository auditLogRepository;
    private final ProhortConnector prohortConnector;
    private final BcbIpcaConnector bcbIpcaConnector;
    private final SidraConnector sidraConnector;
    private final CeagespConnector ceagespConnector;
    private final SeasonalityEngine seasonalityEngine;
    private final PrecoMercadoRepository precoMercadoRepository;
    private final ProdutoRepository produtoRepository;

    public EtlService(
            EtlRunRepository etlRunRepository,
            EtlRawRepository etlRawRepository,
            MarketUploadRepository marketUploadRepository,
            AuditLogRepository auditLogRepository,
            ProhortConnector prohortConnector,
            BcbIpcaConnector bcbIpcaConnector,
            SidraConnector sidraConnector,
            CeagespConnector ceagespConnector,
            SeasonalityEngine seasonalityEngine,
            PrecoMercadoRepository precoMercadoRepository,
            ProdutoRepository produtoRepository
    ) {
        this.etlRunRepository = etlRunRepository;
        this.etlRawRepository = etlRawRepository;
        this.marketUploadRepository = marketUploadRepository;
        this.auditLogRepository = auditLogRepository;
        this.prohortConnector = prohortConnector;
        this.bcbIpcaConnector = bcbIpcaConnector;
        this.sidraConnector = sidraConnector;
        this.ceagespConnector = ceagespConnector;
        this.seasonalityEngine = seasonalityEngine;
        this.precoMercadoRepository = precoMercadoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Map<String, Object> runAll(Long autorUsuarioId) {
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(runSource("PROHORT", autorUsuarioId, () -> prohortConnector.fetchDailyPrices()));
        results.add(runSource("BCB", autorUsuarioId, bcbIpcaConnector::sync));
        results.add(runSource("SIDRA", autorUsuarioId, sidraConnector::fetchPamSample));
        results.add(runSource("CEAGESP", autorUsuarioId, ceagespConnector::fetchHomeProbe));
        int indices = seasonalityEngine.recalculateAll();
        results.add(Map.of("fonte", "SEASONALITY_ENGINE", "indices", indices, "status", "SUCCESS"));
        return Map.of("runs", results);
    }

    private Map<String, Object> runSource(String fonte, Long autor, java.util.function.Supplier<Map<String, Object>> supplier) {
        EtlRun run = new EtlRun();
        run.setFonte(fonte);
        run.setStatus("RUNNING");
        run.setAutorUsuarioId(autor);
        run = etlRunRepository.save(run);
        try {
            Map<String, Object> payload = supplier.get();
            EtlRaw raw = new EtlRaw();
            raw.setEtlRunId(run.getId());
            raw.setFonte(fonte);
            raw.setPayload(payload);
            etlRawRepository.save(raw);
            boolean failed = payload.containsKey("error");
            run.setStatus(failed ? "PARTIAL" : "SUCCESS");
            run.setMensagem(String.valueOf(payload.getOrDefault("error", payload.getOrDefault("note", "ok"))));
            run.setRegistros(payload.get("registros") instanceof Number n ? n.intValue() : 1);
            run.setFinalizadoEm(Instant.now());
            Map<String, Object> out = new LinkedHashMap<>(payload);
            out.put("etlRunId", run.getId());
            out.put("status", run.getStatus());
            return out;
        } catch (Exception e) {
            run.setStatus("FAILED");
            run.setMensagem(e.getMessage());
            run.setFinalizadoEm(Instant.now());
            return Map.of("fonte", fonte, "status", "FAILED", "error", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<EtlRun> recentRuns() {
        return etlRunRepository.findTop50ByOrderByIniciadoEmDesc();
    }

    @Transactional
    public MarketUpload uploadMarketCsv(MultipartFile file, String tipo) {
        Long userId = SecurityUtils.currentUserId();
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_TYPE", "Upload de mercado aceita CSV neste endpoint");
        }
        int lines = 0;
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> all = reader.readAll();
            if (all.size() < 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY", "Arquivo sem dados");
            }
            String[] header = all.getFirst();
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < header.length; i++) idx.put(header[i].trim().toLowerCase(), i);
            require(idx, "produto", "data", "preco", "praca_id");
            for (int i = 1; i < all.size(); i++) {
                String[] row = all.get(i);
                String produtoNome = row[idx.get("produto")].trim();
                Produto produto = produtoRepository.findByAtivoTrue().stream()
                        .filter(p -> p.getNomeCanonico().equalsIgnoreCase(produtoNome))
                        .findFirst()
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "PRODUCT", "Produto não encontrado: " + produtoNome));
                PrecoMercado pm = new PrecoMercado();
                pm.setProdutoId(produto.getId());
                pm.setPracaId(Long.valueOf(row[idx.get("praca_id")].trim()));
                pm.setDataRef(LocalDate.parse(row[idx.get("data")].trim()));
                pm.setPrecoMedio(new BigDecimal(row[idx.get("preco")].trim().replace(",", ".")));
                if (idx.containsKey("volume")) {
                    pm.setVolumeKg(new BigDecimal(row[idx.get("volume")].trim().replace(",", ".")));
                }
                pm.setFonte("UPLOAD_ADMIN");
                precoMercadoRepository.save(pm);
                lines++;
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARSE", e.getMessage());
        }

        MarketUpload upload = new MarketUpload();
        upload.setUsuarioId(userId);
        upload.setTipo(tipo == null ? "PRECO_MERCADO" : tipo);
        upload.setArquivoNome(file.getOriginalFilename());
        upload.setQtdLinhas(lines);
        upload.setStatus("VALIDADO");
        upload.setMensagem("Importado e validado (RN-20)");
        marketUploadRepository.save(upload);

        AuditLog log = new AuditLog();
        log.setUsuarioId(userId);
        log.setAcao("MARKET_UPLOAD");
        log.setDetalhe(file.getOriginalFilename() + " linhas=" + lines);
        auditLogRepository.save(log);

        seasonalityEngine.recalculateAll();
        return upload;
    }

    private void require(Map<String, Integer> idx, String... cols) {
        for (String c : cols) {
            if (!idx.containsKey(c)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "COLUMNS", "Coluna obrigatória ausente: " + c);
            }
        }
    }
}
