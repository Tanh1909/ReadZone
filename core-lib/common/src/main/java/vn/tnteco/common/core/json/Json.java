package vn.tnteco.common.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jooq.JSON;
import org.jooq.JSONB;
import vn.tnteco.common.config.jackson.*;
import vn.tnteco.common.core.exception.DecodeException;
import vn.tnteco.common.core.exception.EncodeException;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Json {

    /**
     * Decode a given JSON string to a POJO of the given class type.
     *
     * @param str   the JSON string.
     * @param clazz the class to map to.
     * @param <T>   the generic type.
     * @return an instance of T
     * @throws DecodeException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(String str, Class<T> clazz) {
        try {
            return JsonMapper.getObjectMapper().readValue(str, clazz);
        } catch (JsonProcessingException e) {
            log.error("decodeValue ERROR {}", e.getMessage(), e);
            throw new DecodeException("Failed to decode: " + e.getMessage());
        }
    }

    /**
     * Decode a given JSON string to a POJO of the given type.
     *
     * @param str  the JSON string.
     * @param type the type to map to.
     * @param <T>  the generic type.
     * @return an instance of T
     * @throws DecodeException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(String str, TypeReference<T> type) throws DecodeException {
        try {
            return JsonMapper.getObjectMapper().readValue(str, type);
        } catch (Exception e) {
            log.error("decodeValue ERROR {}", e.getMessage(), e);
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    /**
     * Decode a given JSON string to a POJO of the given type.
     *
     * @param jsonNode the JSON
     * @param type     the type to map to.
     * @param <T>      the generic type.
     * @return an instance of T
     * @throws DecodeException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(JsonNode jsonNode, TypeReference<T> type) throws DecodeException {
        try {
            return JsonMapper.getObjectMapper().convertValue(jsonNode, type);
        } catch (Exception e) {
            log.error("decodeValue ERROR {}", e.getMessage(), e);
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    /**
     * Decode a given JSON byte to a POJO of the given type.
     *
     * @param src  the JSON byte.
     * @param type the type to map to.
     * @param <T>  the generic type.
     * @return an instance of T
     * @throws DecodeException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(byte[] src, Class<T> type) throws DecodeException {
        try {
            return JsonMapper.getObjectMapper().readValue(src, type);
        } catch (Exception e) {
            log.error("decodeValue ERROR {}", e.getMessage(), e);
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    /**
     * Encode a POJO to JSON using the underlying Jackson mapper.
     *
     * @param obj a POJO
     * @return a String containing the JSON representation of the given POJO.
     * @throws EncodeException if a property cannot be encoded.
     */
    public static String encode(Object obj) throws EncodeException {
        try {
            return JsonMapper.getObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            log.error("encode ERROR {}", e.getMessage(), e);
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
        }
    }

    public static byte[] encodeAsByte(Object obj) throws EncodeException {
        try {
            return JsonMapper.getObjectMapper().writeValueAsBytes(obj);
        } catch (Exception e) {
            log.error("encode ERROR {}", e.getMessage(), e);
            throw new EncodeException("Failed to encode as byte: " + e.getMessage());
        }
    }


    public static String parserJsonLog(Object data) {
        try {
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(JSON.class, new JSONSerializer());
            simpleModule.addDeserializer(JSON.class, new JSONDeserializer());
            simpleModule.addSerializer(JSONB.class, new JSONBSerializer());
            simpleModule.addDeserializer(JSONB.class, new JSONBDeserializer());
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .registerModule(new JavaTimeModule())
                    .registerModule(simpleModule)
                    .setDateFormat(new StdDateFormat().withColonInTimeZone(true))
                    .setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("parserJsonLog ERROR", e);
            return "";
        }
    }

    public static JsonNode toJsonNode(String json) {
        try {
            return JsonMapper.getObjectMapper().readTree(json);
        } catch (JsonProcessingException e) {
            log.error("toJsonNode ERROR {}", e.getMessage());
            return null;
        }
    }

    public static <T> T createObject(Map<String, Object> objectMap, Class<T> classType) {
        try {
            return JsonMapper.getObjectMapper().convertValue(objectMap, classType);
        } catch (Exception e) {
            log.error("parser objectMap ERROR {}", e.getMessage());
            return null;
        }
    }

    public static <T> List<T> createObjects(List<Map<String, Object>> objectMaps, Class<T> classType) {
        return objectMaps.stream()
                .map(objectMap -> createObject(objectMap, classType))
                .filter(Objects::nonNull)
                .toList();
    }

    public static boolean findValueFromSingleNodeAndCompare(JsonNode data, String nodeName, String compareValue) {
        JsonNode parent = data.findParent(nodeName);
        if (parent.isMissingNode()) {
            return false;
        }
        return parent.get(nodeName).asText().equals(compareValue);
    }


    public static ObjectNode createObjectNode() {
        return JsonMapper.getObjectMapper().createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return JsonMapper.getObjectMapper().createArrayNode();
    }

    static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

}
