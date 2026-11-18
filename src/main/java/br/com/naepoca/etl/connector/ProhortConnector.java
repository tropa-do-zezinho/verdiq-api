package br.com.naepoca.etl.connector;

import br.com.naepoca.config.NaEpocaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProhortConnector {

    private static final Logger log = LoggerFactory.getLogger(ProhortConnector.class);

    private final RestClient restClient;
    private final NaEpocaProperties properties;

    public ProhortConnector(RestClient.Builder builder, NaEpocaProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    /**
     * O host antigo {@code pentahoportaldeinformacoes.conab.gov.br} tem certificado
     * só para {@code www.conab.gov.br} (certificate_unknown / SAN) e hoje redireciona
     * para gov.br. Usamos {@code portaldeinformacoes.conab.gov.br} (SAN correto).
     * A API CDA/Pentaho pública parece ter sido substituída pelo SPA do portal —
     * se a resposta for HTML, mantemos o seed (RN-05) e o upload admin (RF-48).
     */
    public Map<String, Object> fetchDailyPrices() {
        var prohort = properties.etl().prohort();
        String auth = Base64.getEncoder().encodeToString(
                (prohort.user() + ":" + prohort.password()).getBytes(StandardCharsets.UTF_8));
        String url = prohort.baseUrl()
                + "/pentaho/plugin/cda/api/doQuery"
                + "?path=/home/PROHORT/precoDia.cda&dataAccessId=precos";
        try {
            String body = restClient.get()
                    .uri(url)
                    .header("Authorization", "Basic " + auth)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(String.class);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("fonte", "PROHORT");
            payload.put("url", url);

            if (body == null || body.isBlank()) {
                payload.put("error", "Resposta vazia da Conab");
                payload.put("fallback", "SEED_KEEP_LAST");
                return payload;
            }
            String trimmed = body.stripLeading();
            if (trimmed.startsWith("<!doctype") || trimmed.startsWith("<html") || trimmed.startsWith("<!DOCTYPE")) {
                payload.put("error", "Portal Conab devolveu HTML (SPA), não JSON CDA. API Pentaho pública indisponível.");
                payload.put("fallback", "SEED_KEEP_LAST");
                payload.put("hint", "Use seed + POST /v1/admin/market-uploads (RF-48) para preços.");
                return payload;
            }

            payload.put("raw", body.substring(0, Math.min(body.length(), 8000)));
            payload.put("registros", 1);
            return payload;
        } catch (Exception e) {
            log.warn("Prohort fetch failed: {}", e.getMessage());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("fonte", "PROHORT");
            payload.put("error", e.getMessage());
            payload.put("fallback", "SEED_KEEP_LAST");
            payload.put("hint", "Host antigo falha TLS (SAN). Prefira portaldeinformacoes.conab.gov.br ou upload CSV.");
            return payload;
        }
    }
}
