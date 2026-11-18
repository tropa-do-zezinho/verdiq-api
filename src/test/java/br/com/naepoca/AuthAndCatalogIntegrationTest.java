package br.com.naepoca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "ORACLE_SID", matches = ".+")
class AuthAndCatalogIntegrationTest {

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

    @Autowired
    MockMvc mockMvc;

    @Test
    void publicSeasonsAndLogin() throws Exception {
        mockMvc.perform(get("/v1/seasons/now"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emSafra").isArray());

        mockMvc.perform(get("/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").exists());

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"marta@naepoca.local","senha":"Senha@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.papel").value("COMERCIANTE"));
    }

    @Test
    void merchantIsolationOnDashboard() throws Exception {
        String token = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"marta@naepoca.local","senha":"Senha@123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String access = token.replaceAll("(?s).*\"accessToken\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/v1/merchant/dashboard")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receita").exists());

        mockMvc.perform(get("/v1/merchant/dashboard"))
                .andExpect(status().is4xxClientError());
    }
}
