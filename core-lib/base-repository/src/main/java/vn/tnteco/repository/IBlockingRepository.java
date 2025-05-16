package vn.tnteco.repository;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.UniqueKey;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.repository.builder.UpdateField;
import vn.tnteco.repository.data.UpdatePojo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IBlockingRepository<P, ID> {

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     */
    List<P> getBlockingByIds(Collection<ID> ids);

    P getBlockingById(ID id);

    P getOneBlockingByCondition(Condition condition);

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    List<P> getAllBlocking();

    List<P> getAllBlockingByIds(Collection<ID> ids);


    List<P> getBlockingByCondition(Condition... conditions);

    List<P> getActiveBlockingByIds(Collection<ID> ids);

    List<P> getActiveBlockingByPageable(Pageable pageable, Condition condition);

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    Long countBlockingByCondition(Condition... conditions);

    Optional<BigDecimal> sumFieldByCondition(Field<? extends Number> field, Condition... conditions);

    /**
     * Returns map instances of the type.
     *
     * @return all entities
     */
    Map<ID, P> getMapBlocking();

    Map<ID, P> getMapBlockingById(Collection<ID> ids);

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param pojo must not be {@literal null}.
     * @return the saved entity will never be {@literal null}.
     */
    P insertBlocking(P pojo);

    P insertBlocking(P pojo, DSLContext dslContext);

    void insertBlocking(Collection<P> pojos);

    void insertBlocking(Collection<P> pojos, DSLContext context);

    /**
     * Saves entity with key.
     *
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    // Duplicate key ignore
    void insertBlockingIgnoreOnDuplicateKey(P pojo);

    void insertBlockingIgnoreOnDuplicateKey(Collection<P> pojos);

    void insertBlockingIgnoreOnDuplicateKey(Collection<P> pojos, DSLContext context);

    // Duplicate key update
    void insertBlockingUpdateOnDuplicateKey(P pojo);

    void insertBlockingUpdateOnDuplicateKey(Collection<P> pojos);

    void insertBlockingUpdateOnDuplicateKey(Collection<P> pojos, DSLContext context);

    // Duplicate config key ignore
    P insertReturnBlockingIgnoreOnConfigKey(P pojo, UniqueKey<?> uniqueKey);

    void insertBlockingIgnoreOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey);

    // Duplicate config key update
    P insertReturnBlockingUpdateOnConfigKey(P pojo, UniqueKey<?> uniqueKey);

    void insertBlockingUpdateOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey);

    void insertBlockingUpdateOnConfigKey(P pojo, UniqueKey<?> uniqueKey, DSLContext context);

    void insertBlockingUpdateOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey, DSLContext context);

    /**
     * Update the entity with the given id.aa
     *
     * @param id must not be {@literal null}.
     */
    void updateBlocking(ID id, P pojo, DSLContext context);

    P updateReturningBlocking(ID id, UpdateField updateField);

    Integer updateBlocking(ID id, UpdateField updateField);

    Integer updateBlocking(ID id, UpdateField updateField, DSLContext context);

    void updateBlocking(List<UpdatePojo<ID>> updatePojos);

    Integer updateBlockingByCondition(Condition condition, UpdateField updateField);

    Integer updateBlockingByCondition(Condition condition, UpdateField updateField, DSLContext context);

}
