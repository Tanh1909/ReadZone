package com.example.app.config.database;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Setter;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jooq.JooqExceptionTranslator;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@org.springframework.context.annotation.Configuration
public class AppDSLContextApiConfig {

    @Setter(onMethod_ = {@Autowired(required = false), @Qualifier("appDataSource")})
    private HikariDataSource appDataSource;

    @Bean(name = "appDslContext")
    public DSLContext appDslContext() {
        return new DefaultDSLContext(this.configuration());
    }

    @Bean("appConnectionProvider")
    public DataSourceConnectionProvider appConnectionProvider() {
        return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(appDataSource));
    }

    public DefaultConfiguration configuration() {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        Settings settings = new Settings().withRenderSchema(false);
        jooqConfiguration.setSettings(settings);
        jooqConfiguration.set(SQLDialect.POSTGRES);
        jooqConfiguration.set(this.appConnectionProvider());
        jooqConfiguration.set(new DefaultExecuteListenerProvider(new JooqExceptionTranslator()));
        return jooqConfiguration;
    }

}
