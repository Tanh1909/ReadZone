package vn.tnteco.repository;

import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
@ComponentScan({"vn.tnteco"})
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        JooqAutoConfiguration.class
})
public class RepositoryStarterConfig {
}
