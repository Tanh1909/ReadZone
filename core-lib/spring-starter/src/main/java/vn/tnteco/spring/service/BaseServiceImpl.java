package vn.tnteco.spring.service;

import io.reactivex.rxjava3.core.Single;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.common.data.response.DeleteResponse;
import vn.tnteco.repository.IRxRepository;
import vn.tnteco.repository.data.audit.Auditable;
import vn.tnteco.spring.data.base.BaseResponse;
import vn.tnteco.spring.data.base.UserInfo;
import vn.tnteco.spring.data.response.BasicResponse;
import vn.tnteco.spring.mapper.BaseMapper;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vn.tnteco.common.data.constant.ErrorResponseBase.UNAUTHORIZED;
import static vn.tnteco.common.data.constant.MessageResponse.SUCCESS;

public abstract class BaseServiceImpl<Rq, Rs extends BaseResponse, Pojo, ID,
        Repo extends IRxRepository<Pojo, ID>,
        Mp extends BaseMapper<Rq, Rs, Pojo>> implements IBaseService<Rq, Rs, ID> {

    @Setter(onMethod_ = {@Autowired})
    protected Repo repository;

    @Setter(onMethod_ = {@Autowired})
    protected Mp mapper;

    private Class<Rs> response;

    @PostConstruct
    public void init() {
        this.response = (Class<Rs>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
    }

    protected Set<Integer> getUserIds(Collection<Pojo> pojos) {
        Set<Integer> userIds = new HashSet<>();
        for (Pojo pojo : pojos) {
            if (pojo instanceof Auditable auditable) {
                CollectionUtils.addIgnoreNull(userIds, auditable.getCreatedBy());
                CollectionUtils.addIgnoreNull(userIds, auditable.getUpdatedBy());
            }
        }
        return userIds;
    }

    protected Set<Integer> getUserIdsFromRs(Collection<Rs> responses) {
        Set<Integer> userIds = new HashSet<>();
        for (Rs response : responses) {
            CollectionUtils.addIgnoreNull(userIds, response.getCreatedBy());
            CollectionUtils.addIgnoreNull(userIds, response.getUpdatedBy());
        }
        return userIds;
    }

    protected SimpleSecurityUser getSimpleSecurityUser() {
        SimpleSecurityUser simpleSecurityUser = SecurityContext.getSimpleSecurityUser();
        if (simpleSecurityUser == null) {
            throw new ApiException(UNAUTHORIZED);
        }
        return simpleSecurityUser;
    }

    @Override
    public Single<Page<Rs>> search(SearchRequest searchRequest) {
        return Single.zip(
                repository.getActiveBySearchRequest(searchRequest),
                repository.countBySearchRequest(searchRequest),
                Pair::of
        ).flatMap(pair -> {
            List<Pojo> pojos = pair.getLeft();
            Long total = pair.getRight();
            List<Rs> responses = mapper.toResponses(pojos);
            Set<Integer> userIds = getUserIdsFromRs(responses);
            return getUserInfo(userIds)
                    .map(userInfos -> {
                        Map<Integer, UserInfo> mapIdOfUserInfo = getMapIdOfUserInfo(userInfos);
                        for (Rs response : responses) {
                            response.setCreator(mapIdOfUserInfo.get(response.getCreatedBy()));
                            response.setUpdater(mapIdOfUserInfo.get(response.getUpdatedBy()));
                        }
                        return responses;
                    }).flatMap(result -> toPageResponse(result, total, searchRequest));
        });
    }

    public Single<Page<Rs>> toPageResponse(List<Rs> searchResponses, Long total, SearchRequest searchRequest) {
        return Single.just(new Page<>(total, searchRequest, searchResponses));
    }

    protected Map<Integer, UserInfo> getMapIdOfUserInfo(List<UserInfo> userInfos) {
        return userInfos.stream()
                .collect(Collectors.toMap(UserInfo::getId, Function.identity()));
    }

    protected Single<List<UserInfo>> getUserInfo(Collection<Integer> userIds) {
        return Single.just(Collections.emptyList());
    }

    @Override
    public Single<Page<Object>> select(Pageable pageable) {
        Class<?> selectResponseType = this.setSelectSearch();
        pageable.setSearches(selectResponseType);
        return Single.zip(
                repository.getByPageableAndIsActive(pageable),
                repository.countByPageableAndIsActive(pageable),
                (searchResponse, total) -> {
                    Function<Pojo, Object> selectMapper = getSelectMapper();
                    List<Object> result = new ArrayList<>();
                    for (Pojo pojo : searchResponse) {
                        Object response = selectMapper.apply(pojo);
                        result.add(response);
                    }
                    return new Page<>(total, pageable, result);
                });
    }

    protected Class<?> setSelectSearch() {
        return BasicResponse.class;
    }

    protected Function<Pojo, Object> getSelectMapper() {
        return pojo -> mapper.toBasicResponse(pojo);
    }

    @Override
    public Single<String> create(Rq request) {
        return validateCreate(request)
                .flatMap(isValid -> {
                    if (!isValid) return Single.error(new ApiException(ErrorResponseBase.BUSINESS_ERROR));
                    Pojo pojo = mapper.toPojo(request);
                    setCreatedValue(pojo);
                    return repository.insert(pojo)
                            .map(integer -> SUCCESS);
                });
    }

    protected Single<Boolean> validateCreate(Rq request) {
        return Single.just(true);
    }

    protected void setCreatedValue(Pojo pojo) {
    }

    @Override
    public Single<String> update(ID id, Rq request) {
        return validateUpdate(id, request)
                .flatMap(pojo -> {
                    mapper.updateToPojo(pojo, request);
                    setUpdatedValue(pojo);
                    return repository.update(id, pojo)
                            .map(integer -> SUCCESS);
                });
    }

    protected Single<Pojo> validateUpdate(ID id, Rq request) {
        return Single.just(mapper.toPojo(request));
    }

    protected void setUpdatedValue(Pojo pojo) {
    }

    @Override
    public Single<String> delete(ID id) {
        return validateDelete(id)
                .flatMap(isValid -> {
                    if (!isValid) return Single.error(new ApiException(ErrorResponseBase.BUSINESS_ERROR));
                    return repository.deletedById(id)
                            .map(integer -> SUCCESS);
                });
    }

    protected Single<Boolean> validateDelete(ID id) {
        return Single.just(true);
    }


    @Override
    public Single<DeleteResponse> multiDelete(Set<ID> ids) {
        return getValidDeleteIds(ids)
                .flatMap(validIds -> {
                    List<ID> failedDeleteIds = this.getFailedDeleteIds(ids, validIds);
                    return repository.deletedByIds(validIds)
                            .map(integer -> new DeleteResponse()
                                    .setTotalRequested(ids.size())
                                    .setTotalDeleted(validIds.size())
                                    .setFailedIds(failedDeleteIds)
                            );
                });
    }

    private List<ID> getFailedDeleteIds(Collection<ID> ids, List<ID> validSiteIds) {
        List<ID> failDeleteIds = new ArrayList<>();
        for (ID id : ids) {
            if (!validSiteIds.contains(id)) {
                failDeleteIds.add(id);
            }
        }
        return failDeleteIds;
    }


    protected Single<List<ID>> getValidDeleteIds(Set<ID> ids) {
        return Single.just(ids.stream().toList());
    }

}
