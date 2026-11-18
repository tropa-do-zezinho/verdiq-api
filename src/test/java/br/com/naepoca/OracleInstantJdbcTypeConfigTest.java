package br.com.naepoca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression: Oracle TIMESTAMP sem TZ + Instant → ORA-18716 se Hibernate usar TIMESTAMP_UTC.
 */
class OracleInstantJdbcTypeConfigTest {

    @Test
    void preferredInstantJdbcTypeIsTimestamp() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        Properties props = yaml.getObject();
        assertNotNull(props);
        assertEquals(
                "TIMESTAMP",
                props.getProperty("spring.jpa.properties.hibernate.type.preferred_instant_jdbc_type"));
    }
}
