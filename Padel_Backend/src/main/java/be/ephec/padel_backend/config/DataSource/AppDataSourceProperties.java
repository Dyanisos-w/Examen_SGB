package be.ephec.padel_backend.config.DataSource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.datasource")
public class AppDataSourceProperties {

    private Entry user = new Entry();
    private Entry admin = new Entry();

    @Data
    public static class Entry {
        private String url;
        private String jdbcUrl;
        private String username;
        private String password;
        private String driverClassName;
    }
}
