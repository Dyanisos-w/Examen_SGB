package be.ephec.padel_backend.config.DataSource;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final AppDataSourceProperties datasourceProperties;

    @Bean(name = "userDataSource")
    public DataSource userDataSource() {
        AppDataSourceProperties.Entry user = datasourceProperties.getUser();
        String url = user.getJdbcUrl() != null ? user.getJdbcUrl() : user.getUrl();
        return DataSourceBuilder.create()
                .url(url)
                .username(user.getUsername())
                .password(user.getPassword())
                .driverClassName(user.getDriverClassName())
                .build();
    }

    @Bean(name = "adminDataSource")
    public DataSource adminDataSource() {
        AppDataSourceProperties.Entry admin = datasourceProperties.getAdmin();
        String url = admin.getJdbcUrl() != null ? admin.getJdbcUrl() : admin.getUrl();
        return DataSourceBuilder.create()
                .url(url)
                .username(admin.getUsername())
                .password(admin.getPassword())
                .driverClassName(admin.getDriverClassName())
                .build();
    }

    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("userDataSource") DataSource userDataSource,
            @Qualifier("adminDataSource") DataSource adminDataSource
    ) {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.USER, userDataSource);
        targetDataSources.put(DataSourceType.ADMIN, adminDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(userDataSource);

        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("routingDataSource") DataSource routingDataSource) {
        return routingDataSource;
    }
}
