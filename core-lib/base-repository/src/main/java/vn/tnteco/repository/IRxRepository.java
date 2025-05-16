package vn.tnteco.repository;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.jooq.Condition;
import org.jooq.UniqueKey;
import vn.tnteco.common.core.model.filter.Filter;
import vn.tnteco.common.core.model.paging.Order;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.repository.builder.UpdateField;
import vn.tnteco.repository.data.UpdatePojo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IRxRepository<P, ID> {

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     */
    Single<Optional<P>> getById(ID id);

    Single<Optional<P>> getById(ID id, boolean withDeleted);

    Single<Optional<P>> getOne(Condition condition);

    Single<Optional<P>> getOneActiveByCondition(Condition condition);

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    Single<List<P>> getAll();

    Single<List<P>> getByIds(Collection<ID> ids);

    Single<List<P>> getByIds(Collection<ID> ids, boolean withDeleted);

    Single<List<P>> getByPageableAndIsActive(Pageable pageable);

    Single<List<P>> getActiveByPageable(Pageable pageable);

    Single<List<P>> getActiveByCondition(Condition condition);

    Single<List<P>> getActiveByCondition(Condition condition, List<Order> orders);

    Single<List<P>> getActiveByPageable(Pageable pageable, Long userId);

    Single<List<P>> getActiveByPageable(Pageable pageable, Condition condition);

    Single<List<P>> getActiveBySearchRequest(SearchRequest searchRequest);

    Single<List<P>> getActiveBySearchRequest(SearchRequest searchRequest, Condition condition);

    Single<List<P>> getWithOffsetLimit(int offset, int limit);

    /**
     * Returns map instances of the type.
     *
     * @return all entities
     */
    Single<Map<ID, P>> getMapById(Collection<ID> ids);

    Single<Map<ID, P>> getMapByCondition(Condition condition);

    Single<Boolean> existByCondition(Condition condition);

    Single<Boolean> existById(ID id);

    Single<Boolean> existAllByIds(Collection<ID> ids);

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    Single<Long> countActive();

    Single<Long> countActive(Long userId);

    Single<Long> countActive(Condition condition);

    Single<Long> countActive(List<Filter> filters, Condition condition);

    Single<Long> countBySearchRequest(SearchRequest searchRequest);

    Single<Long> countBySearchRequest(SearchRequest searchRequest, Condition condition);

    Single<Long> countByPageable(Pageable pageable);

    Single<Long> countByPageable(Pageable pageable, Condition condition);

    Single<Long> countByPageableAndIsActive(Pageable pageable);

    /**
     * Streaming all instances of the type.
     *
     * @return streaming entities
     */
    Flowable<List<P>> streamingPojos(Condition condition, int fetchSize);

    Flowable<List<P>> streamingPojos(Condition condition, List<Order> orders, int fetchSize);

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param pojo must not be {@literal null}.
     * @return the saved entity will never be {@literal null}.
     */
    Single<Integer> insert(P pojo);

    Single<Optional<P>> insertReturning(P pojo);

    /**
     * Saves all given entities.
     *
     * @param pojos must not be {@literal null}.
     * @return the saved entities will never be {@literal null}.
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    Single<List<Integer>> insert(Collection<P> pojos);


    /**
     * Saves entity with key.
     *
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    // Duplicate key ignore
    Single<Optional<P>> insertIgnoreOnDuplicateKey(P pojo);

    Single<List<Integer>> insertIgnoreOnDuplicateKey(Collection<P> pojos);

    // Duplicate key update
    Single<Optional<P>> insertUpdateOnDuplicateKey(P pojo);

    Single<List<Integer>> insertUpdateOnDuplicateKey(Collection<P> pojos);

    // Duplicate config key ignore
    Single<Optional<P>> insertIgnoreOnConfigKey(P pojo, UniqueKey<?> uniqueKey);

    Single<List<Integer>> insertIgnoreOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey);

    // Duplicate config key update
    Single<Optional<P>> insertUpdateOnConfigKey(P pojo, UniqueKey<?> uniqueKey);

    Single<List<Integer>> insertUpdateOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey);

    Single<Integer> updateIgnoreFieldNull (ID id, P pojo);
    /**
     * Update the entity with the given id.aa
     *
     * @param id must not be {@literal null}.
     */
    Single<Integer> update(ID id, P pojo);

    Single<Integer> update(ID id, P pojo, List<String> acceptFields);

    Single<Integer> update(ID id, UpdateField updateField);

    Single<Optional<P>> updateReturning(ID id, UpdateField updateField);

    Single<Optional<P>> updateReturning(ID id,P pojo);

    Single<Integer> update(P pojo, Condition condition);

    Single<Integer> update(P pojo, Condition... conditions);

    //chua review code
    Single<List<Integer>> update(List<UpdatePojo<ID>> updatePojos);


    /**
     * Deletes the entity with the given id.aa
     *
     * @param id must not be {@literal null}.
     */
    Single<Integer> deletedById(ID id);

    Single<Integer> deletedByIds(List<ID> ids);

    Single<Integer> deletedByIdWithCreatedBy(ID id, Long userId);

    Single<Integer> hardDeleteById(ID id);

    Single<Integer> hardDeleteByIds (List<ID> ids);

}
