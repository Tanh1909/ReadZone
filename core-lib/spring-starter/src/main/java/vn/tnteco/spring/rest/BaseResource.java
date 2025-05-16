package vn.tnteco.spring.rest;

import io.reactivex.rxjava3.core.Single;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.common.data.response.DeleteResponse;
import vn.tnteco.spring.data.base.BaseResponse;
import vn.tnteco.spring.model.DfResponse;
import vn.tnteco.spring.service.IBaseService;

import java.util.Set;

public abstract class BaseResource<Rq, Rs extends BaseResponse, ID, SERVICE extends IBaseService<Rq, Rs, ID>> implements BaseOperations<Rq, Rs, ID> {

    @Setter(onMethod_ = @Autowired)
    protected SERVICE service;

    @Override
    public Single<DfResponse<Page<Object>>> select(Pageable pageable) {
        return service.select(pageable).map(DfResponse::ok);
    }

    @Override
    public Single<DfResponse<Page<Rs>>> search(SearchRequest searchRequest) {
        return service.search(searchRequest).map(DfResponse::ok);
    }

    @Override
    public Single<DfResponse<String>> create(Rq request) {
        return service.create(request).map(DfResponse::ok);
    }

    @Override
    public Single<DfResponse<String>> update(ID id, Rq request) {
        return service.update(id, request).map(DfResponse::ok);
    }

    @Override
    public Single<DfResponse<String>> delete(ID id) {
        return service.delete(id).map(DfResponse::ok);
    }

    @Override
    public Single<DfResponse<DeleteResponse>> multiDelete(Set<ID> ids) {
        return service.multiDelete(ids).map(DfResponse::ok);
    }
}
