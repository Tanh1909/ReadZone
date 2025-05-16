package vn.tnteco.spring.service;

import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.common.data.response.DeleteResponse;
import vn.tnteco.spring.data.base.BaseResponse;

import java.util.Set;

public interface IBaseService<Rq, Rs extends BaseResponse, ID> {

    Single<Page<Rs>> search(SearchRequest searchRequest);

    Single<Page<Object>> select(Pageable pageable);

    Single<String> create(Rq request);

    Single<String> update(ID id, Rq request);

    Single<String> delete(ID id);

    Single<DeleteResponse> multiDelete(Set<ID> ids);

}
