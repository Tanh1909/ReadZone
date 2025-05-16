package vn.tnteco.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.jooq.JSONB;

import java.io.IOException;

public class JSONBDeserializer extends JsonDeserializer<JSONB> {

    @Override
    public JSONB deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return JSONB.jsonbOrNull(p.getValueAsString());
    }
}
