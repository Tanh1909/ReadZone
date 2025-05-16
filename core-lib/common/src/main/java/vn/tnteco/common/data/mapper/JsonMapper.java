package vn.tnteco.common.data.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.mapstruct.Mapper;
import vn.tnteco.common.core.json.Json;

@Mapper(componentModel = "spring")
public interface JsonMapper {

    Logger log = org.apache.logging.log4j.LogManager.getLogger(JsonMapper.class);

    default JsonNode mapToJsonNode(JSON json) {
        if (ObjectUtils.isEmpty(json) || StringUtils.isEmpty(json.data())) {
            log.debug("jooq json is null or empty data");
            return null;
        }
        return Json.toJsonNode(json.data());
    }

    default JsonNode mapToJsonNode(JSONB jsonb) {
        if (ObjectUtils.isEmpty(jsonb) || StringUtils.isEmpty(jsonb.data())) {
            log.debug("jooq jsonb is null or empty data");
            return null;
        }
        return Json.toJsonNode(jsonb.data());
    }

    default JSONB toJSONB(JsonNode jsonNode) {
        if (jsonNode == null) {
            log.debug("jsonNode is null or empty data");
            return null;
        }
        return JSONB.jsonbOrNull(jsonNode.toString());
    }

}
