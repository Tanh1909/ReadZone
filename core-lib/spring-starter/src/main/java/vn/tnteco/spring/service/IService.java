package vn.tnteco.spring.service;


import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.UserPrincipal;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.spring.data.base.BaseResponse;

import java.util.List;

public interface IService<Rq, Rs extends BaseResponse, Id> {
    Single<Rs> insert(Rq request, UserPrincipal userPrincipal);

    Single<Rs> update(Id id, Rq request, UserPrincipal userPrincipal);

    Single<Rs> getById(Id id, UserPrincipal userPrincipal);

    Single<List<Rs>> getByIds(List<Id> ids, UserPrincipal userPrincipal);

    Single<Page<Rs>> search(SearchRequest searchRequest, UserPrincipal userPrincipal);

    Single<Page<Rs>> select(Pageable pageable, UserPrincipal userPrincipal);

    Single<String> delete(Id id, UserPrincipal userPrincipal);
}