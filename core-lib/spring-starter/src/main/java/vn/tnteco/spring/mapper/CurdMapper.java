package vn.tnteco.spring.mapper;

import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Named;
import vn.tnteco.common.core.model.UserPrincipal;
import vn.tnteco.common.utils.TimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static vn.tnteco.common.utils.TimeUtils.parseToLocalDateTime;

public abstract class CurdMapper<Rq, Rs, Po> {

    @Named("toResponseNoContext")
    public abstract Rs toResponse(Po p);

    @Named("toResponse")
    public abstract Rs toResponse(Po p, @Context UserPrincipal userPrincipal);

    @IterableMapping(qualifiedByName = "toResponseNoContext")
    public abstract List<Rs> toResponses(List<Po> list);

    @IterableMapping(qualifiedByName = "toResponse")
    public abstract List<Rs> toResponses(List<Po> list, @Context UserPrincipal userPrincipal);

    @Named("toPojo")
    public abstract Po toPojo(Rq rq, @Context UserPrincipal userPrincipal);

    @IterableMapping(qualifiedByName = "toPojo")
    public abstract List<Po> toPojos(List<Rq> list, @Context UserPrincipal userPrincipal);

    protected LocalDateTime mapToLocalDateTime(String input) {
        return parseToLocalDateTime(input);
    }

    protected Long mapToLong(LocalDateTime time) {
        return TimeUtils.localDateTimeToEpochMilli(time);
    }

    protected Long mapToLong(LocalDate time) {
        return TimeUtils.localDateToEpochMilli(time);
    }
}
