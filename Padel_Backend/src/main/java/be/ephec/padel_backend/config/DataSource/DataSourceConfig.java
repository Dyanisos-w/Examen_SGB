package be.ephec.padel_backend.config.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jdbc.DataSourceBuilder;

@Configuration
public class DataSourceConfig {

    // USER
    @Bean(name = "userDataSource")
    @ConfigurationProperties(prefix = "app.datasource.user")
    public DataSource userDataSource() {
        return DataSourceBuilder.create().build();
    }

    // ADMIN
    @Bean(name = "adminDataSource")
    @ConfigurationProperties(prefix = "app.datasource.admin")
    public DataSource adminDataSource() {
        return DataSourceBuilder.create().build();
    }

    // ROUTING
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

    // 🔥 CRITIQUE POUR SPRING BOOT
    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("routingDataSource") DataSource routingDataSource) {
        return routingDataSource;
    }
}