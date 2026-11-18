package br.com.naepoca.etl.connector;

import br.com.naepoca.config.NaEpocaProperties;
import br.com.naepoca.market.domain.IpcaMensal;
import br.com.naepoca.market.repo.IpcaMensalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BcbIpcaConnector {

    private static final Logger log = LoggerFactory.getLogger(BcbIpcaConnector.class);
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;
    private final NaEpocaProperties properties;
    private final IpcaMensalRepository ipcaMensalRepository;

    public BcbIpcaConnector(RestClient.Builder builder, NaEpocaProperties properties, IpcaMensalRepository ipcaMensalRepository) {
        this.restClient = builder.build();
        this.properties = properties;
        this.ipcaMensalRepository = ipcaMensalRepository;
    }

    public Map<String, Object> sync() {
        String url = properties.etl().bcb().baseUrl() + "?formato=json";
        int saved = 0;
        try {
            List<Map<String, String>> rows = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (rows != null) {
                for (Map<String, String> row : rows) {
                    LocalDate data = LocalDate.parse(row.get("data"), BR);
                    String anoMes = "%04d-%02d".formatted(data.getYear(), data.getMonthValue());
                    BigDecimal valor = new BigDecimal(row.get("valor").replace(",", "."));
                    IpcaMensal entity = ipcaMensalRepository.findByAnoMes(anoMes).orElseGet(IpcaMensal::new);
                    entity.setAnoMes(anoMes);
                    // acumula aproximado: se seed já tem índice absoluto, só atualiza se vier da API como variação
                    // BCB SGS 433 é variação mensal; convertemos para índice acumulado simples
                    if (entity.getId() == null) {
                        BigDecimal prev = ipcaMensalRepository.findAllByOrderByAnoMesAsc().stream()
                                .reduce((a, b) -> b)
                                .map(IpcaMensal::getIndice)
                                .orElse(BigDecimal.valueOf(100));
                        entity.setIndice(prev.multiply(BigDecimal.ONE.add(valor.divide(BigDecimal.valueOf(100)))));
                    } else {
                        continue;
                    }
                    entity.setFonte("BCB_SGS_433");
                    ipcaMensalRepository.save(entity);
                    saved++;
                }
            }
            return Map.of("fonte", "BCB", "registros", saved, "status", "SUCCESS");
        } catch (Exception e) {
            log.warn("BCB IPCA sync failed: {}", e.getMessage());
            return Map.of("fonte", "BCB", "status", "FAILED", "error", e.getMessage(), "fallback", "SEED_KEEP_LAST");
        }
    }
}
