package vn.tnteco.common.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class JsonMapper {

    private static ObjectMapper objectMapper;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return getObjectMapper();
    }

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) JsonMapper.resetJsonConfig();
        return objectMapper;
    }

    private static void resetJsonConfig() {
        objectMapper = new ObjectMapper();
        objectMapper
                .registerModule(new JavaTimeModule())
                .registerModule(jsonModule())
                .setPropertyNamingStrategy(LOWER_CAMEL_CASE)
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static SimpleModule jsonModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(JSON.class, new JSONSerializer());
        simpleModule.addDeserializer(JSON.class, new JSONDeserializer());
        simpleModule.addSerializer(JSONB.class, new JSONBSerializer());
        simpleModule.addDeserializer(JSONB.class, new JSONBDeserializer());
        return simpleModule;
    }
}