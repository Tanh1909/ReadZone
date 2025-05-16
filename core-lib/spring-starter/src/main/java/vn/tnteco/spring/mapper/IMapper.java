package vn.tnteco.spring.mapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.JSONB;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Named;
import vn.tnteco.common.core.model.UserPrincipal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static vn.tnteco.common.utils.TimeUtils.parseToLocalDateTime;

public interface IMapper<Rq, Rs, Pojo> {
    @Named("toPojo")
    public Pojo toPojo(Rq request, @Context UserPrincipal userPrincipal);

    @IterableMapping(qualifiedByName = "toPojo")
    public List<Pojo> toListPojo(List<Rq> requestList, @Context UserPrincipal userPrincipal);

    Rs toResponse(Pojo pojo);

    @Named("toPojo")
    Rs toResponse(Pojo pojo, @Context UserPrincipal userPrincipal);

    @IterableMapping(qualifiedByName = "toPojo")
    public List<Rs> toResponses(List<Pojo> pojos, @Context UserPrincipal userPrincipal);

    default LocalDateTime mapToLocalDateTime(String input) {
        return parseToLocalDateTime(input);
    }

    default LocalDate mapToLocalDate(String input) {
        LocalDateTime localDateTime = parseToLocalDateTime(input);
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toLocalDate();
    }
    default Long stringToLong(String input) {
        if (StringUtils.isEmpty(input) || !NumberUtils.isDigits(input)) return null;
        return NumberUtils.createLong(input);
    }

    default String map(JSONB jsonb) {
        if (jsonb == null) return null;
        return jsonb.data();
    }
}
