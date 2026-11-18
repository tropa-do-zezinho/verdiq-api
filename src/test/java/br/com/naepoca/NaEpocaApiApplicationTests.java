package br.com.naepoca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ORACLE_SID", matches = ".+")
class NaEpocaApiApplicationTests {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        String host = System.getenv().getOrDefault("ORACLE_HOSTNAME", "localhost");
        String port = System.getenv().getOrDefault("ORACLE_PORT", "1521");
        String sid = System.getenv().getOrDefault("ORACLE_SID", "ORCL");
        String user = System.getenv().getOrDefault("ORACLE_USERNAME", "verdiq");
        String pass = System.getenv().getOrDefault("ORACLE_PASSWORD", "verdiq");
        registry.add("spring.datasource.url", () -> "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid);
        registry.add("spring.datasource.username", () -> user);
        registry.add("spring.datasource.password", () -> pass);
        registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
        registry.add("spring.docker.compose.enabled", () -> "false");
        registry.add("naepoca.etl.cron", () -> "0 0 0 1 1 *");
    }

    @Test
    void contextLoads() {
    }
}
