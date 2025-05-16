package vn.tnteco.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.jooq.JSON;

import java.io.IOException;

public class JSONDeserializer extends JsonDeserializer<JSON> {

    @Override
    public JSON deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return JSON.jsonOrNull(p.getValueAsString());
    }
}
