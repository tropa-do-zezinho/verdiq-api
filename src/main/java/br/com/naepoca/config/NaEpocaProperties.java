package br.com.naepoca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naepoca")
public record NaEpocaProperties(
        Jwt jwt,
        WebPush webpush,
        Etl etl,
        Seed seed
) {
    public record Jwt(String secret, long accessTokenMinutes, long refreshTokenDays) {}
    public record WebPush(String subject, String publicKey, String privateKey) {}
    public record Etl(Prohort prohort, Bcb bcb, Sidra sidra, Ceagesp ceagesp, String cron) {
        public record Prohort(String baseUrl, String user, String password) {}
        public record Bcb(String baseUrl) {}
        public record Sidra(String baseUrl) {}
        public record Ceagesp(String baseUrl) {}
    }
    public record Seed(String demoPassword) {}
}
