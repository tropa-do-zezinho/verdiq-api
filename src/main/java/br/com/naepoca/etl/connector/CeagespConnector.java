package br.com.naepoca.etl.connector;

import br.com.naepoca.config.NaEpocaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CeagespConnector {

    private static final Logger log = LoggerFactory.getLogger(CeagespConnector.class);

    private final RestClient restClient;
    private final NaEpocaProperties properties;

    public CeagespConnector(RestClient.Builder builder, NaEpocaProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    public Map<String, Object> fetchHomeProbe() {
        String url = properties.etl().ceagesp().baseUrl();
        try {
            String body = restClient.get().uri(url).retrieve().body(String.class);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("fonte", "CEAGESP");
            payload.put("reachable", body != null && !body.isBlank());
            payload.put("note", "Portal sem API estável; série histórica usa PROHORT/SEED. Upload admin (RF-48) complementa.");
            return payload;
        } catch (Exception e) {
            log.warn("Ceagesp probe failed: {}", e.getMessage());
            return Map.of("fonte", "CEAGESP", "error", e.getMessage(), "fallback", "SEED_KEEP_LAST");
        }
    }
}
