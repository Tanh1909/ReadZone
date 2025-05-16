package vn.tnteco.common.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jooq.JSONB;

import java.io.IOException;

public class JSONBSerializer extends JsonSerializer<JSONB> {
    @Override
    public void serialize(JSONB json, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(json.data());
    }
}
