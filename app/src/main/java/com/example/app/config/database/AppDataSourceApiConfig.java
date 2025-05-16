package com.example.app.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties("spring.datasource.postgres.app")
public class AppDataSourceApiConfig extends HikariConfig {

    @Bean(name = "appDataSource")
    @ConditionalOnMissingBean(name = "appDataSource")
    public HikariDataSource appDataSource() {
        System.out.println("Postgres: " + this.getJdbcUrl() + ", username: " + this.getUsername());
        return new HikariDataSource(this);
    }
    
}