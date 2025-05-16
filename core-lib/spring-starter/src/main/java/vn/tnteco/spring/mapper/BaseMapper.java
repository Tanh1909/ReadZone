package vn.tnteco.spring.mapper;

import org.mapstruct.MappingTarget;
import vn.tnteco.common.data.mapper.TimeMapper;
import vn.tnteco.spring.data.base.BaseResponse;
import vn.tnteco.spring.data.response.BasicResponse;

import java.util.List;


public abstract class BaseMapper<Rq, Rs extends BaseResponse, Pojo> implements TimeMapper {

    public abstract Pojo toPojo(Rq request);

    public abstract Rs toResponse(Pojo pojo);

    public abstract List<Rs> toResponses(List<Pojo> pojos);

    public abstract BasicResponse toBasicResponse(Pojo pojo);

    public abstract void updateToPojo(@MappingTarget Pojo pojo, Rq request);

}
