package com.henriquecipriano.spring5_read_replica_postgresql.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
class DataSourceConfig {

    private static final String PRIMARY_DATABASE_PROPERTY_KEY_PREFIX = "spring.primary.datasource";
    private static final String REPLICA_DATABASE_PROPERTY_KEY_PREFIX = "spring.replica.datasource";

    @Bean
    @ConfigurationProperties(PRIMARY_DATABASE_PROPERTY_KEY_PREFIX)
    DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(PRIMARY_DATABASE_PROPERTY_KEY_PREFIX + ".hikari")
    DataSource primaryDataSource(final DataSourceProperties primaryDataSourceProperties) {
        return primaryDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(REPLICA_DATABASE_PROPERTY_KEY_PREFIX)
    DataSourceProperties replicaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(REPLICA_DATABASE_PROPERTY_KEY_PREFIX + ".hikari")
    DataSource replicaDataSource(final DataSourceProperties replicaDataSourceProperties) {
        return replicaDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    DataSource routingDataSource(final DataSource primaryDataSource, final DataSource replicaDataSource) {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();

        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("master", primaryDataSource);
        dataSources.put("replica", replicaDataSource);

        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);

        return routingDataSource;
    }

    @Bean
    DataSource dataSource(final DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
