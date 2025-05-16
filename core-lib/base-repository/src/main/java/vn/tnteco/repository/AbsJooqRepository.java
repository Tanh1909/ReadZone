package vn.tnteco.repository;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import vn.tnteco.common.core.exception.DBException;
import vn.tnteco.common.core.model.filter.Filter;
import vn.tnteco.common.core.model.paging.Order;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.common.core.template.RxFlowableTemplate;
import vn.tnteco.common.data.constant.MessageResponse;
import vn.tnteco.repository.builder.UpdateField;
import vn.tnteco.repository.data.UpdatePojo;
import vn.tnteco.repository.data.constant.FieldConstant;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.partition;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;
import static vn.tnteco.common.data.constant.MessageResponse.NOT_PERMISSION;
import static vn.tnteco.common.data.constant.MessageResponse.RESOURCE_NOT_FOUND;
import static vn.tnteco.repository.utils.SqlUtils.*;

@Log4j2
public abstract class AbsJooqRepository<R extends TableRecordImpl<R>, P, ID>
        implements IRxRepository<P, ID>, IBlockingRepository<P, ID> {

    protected abstract DSLContext getDslContext();

    protected Class<P> pojoClass;
    protected TableField<R, ID> fieldID;
    protected Field<LocalDateTime> deletedField;
    protected Field<Long> createdField;
    protected R record;

    protected List<TableField<R, ?>> getIgnoreUpdateFields() {
        return new ArrayList<>();
    }

    protected abstract TableImpl<R> getTable();

    @SneakyThrows
    @PostConstruct
    public void init() {
        log.info("init class {}", this.getClass().getSimpleName());
        this.pojoClass = ((Class<P>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1]);
        this.fieldID = (TableField<R, ID>) Arrays.stream(getTable().fields())
                .filter(field -> field.getName().equalsIgnoreCase(ID_FIELD_NAME))
                .findFirst()
                .orElse(null);
        this.record = ((Class<R>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0])
                .getDeclaredConstructor()
                .newInstance();
        this.createdField = getTable().field("created_by", Long.class);
        this.deletedField = getTable().field("deleted_at", LocalDateTime.class);
    }

    protected Condition filterActive() {
        if (deletedField != null) return deletedField.isNull();
        return DSL.trueCondition();
    }


//
// =============================================== Blocking ===================================================
//


//
// =============================== Return pojo =====================================
//


    @Override
    public List<P> getBlockingByIds(Collection<ID> ids) {
        return getDslContext()
                .select()
                .from(getTable())
                .where(fieldID.in(ids)
                        .and(filterActive()))
                .fetchInto(pojoClass);
    }

    @Override
    public P getBlockingById(ID id) {
        return getDslContext()
                .select()
                .from(getTable())
                .where(fieldID.eq(id).and(filterActive()))
                .fetchOneInto(pojoClass);
    }

    @Override
    public P getOneBlockingByCondition(Condition condition) {
        return getDslContext()
                .select()
                .from(getTable())
                .where(condition.and(filterActive()))
                .limit(1)
                .fetchOneInto(pojoClass);
    }

//
// ================ Return pojos =================
//

    @Override
    public List<P> getAllBlocking() {
        return getDslContext()
                .select()
                .from(getTable())
                .where(filterActive())
                .fetchInto(pojoClass);
    }

    @Override
    public List<P> getAllBlockingByIds(Collection<ID> ids) {
        return getDslContext()
                .select()
                .from(getTable())
                .where(fieldID.in(ids).and(filterActive()))
                .fetchInto(pojoClass);
    }

    @Override
    public List<P> getBlockingByCondition(Condition... conditions) {
        return getDslContext()
                .select()
                .from(getTable())
                .where(DSL.and(conditions).and(filterActive()))
                .fetchInto(pojoClass);
    }

    @Override
    public List<P> getActiveBlockingByIds(Collection<ID> ids) {
        return getDslContext()
                .select()
                .from(getTable())
                .where(filterActive().and(fieldID.in(ids)))
                .fetchInto(pojoClass);
    }

    @Override
    public List<P> getActiveBlockingByPageable(Pageable pageable, Condition condition) {
        return getDslContext().select()
                .from(getTable())
                .where(condition.and(filterActive()))
                .orderBy(toSortField(pageable.getSorts(), getTable().fields()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchInto(pojoClass);
    }

//
// ================ Returns the number of entities available =================
//

    @Override
    public Long countBlockingByCondition(Condition... conditions) {
        return getDslContext()
                .selectCount()
                .from(getTable())
                .where(DSL.and(conditions).and(filterActive()))
                .fetchOneInto(Long.class);
    }

    @Override
    public Optional<BigDecimal> sumFieldByCondition(Field<? extends Number> field, Condition... conditions) {
        return getDslContext()
                .select(DSL.sum(field))
                .from(getTable())
                .where(DSL.and(conditions).and(filterActive()))
                .fetchOptionalInto(BigDecimal.class);
    }

//
// ================ Returns map entities available =================
//

    @Override
    public Map<ID, P> getMapBlocking() {
        return getDslContext()
                .select()
                .from(getTable())
                .where(filterActive())
                .fetchMap(r -> r.get(fieldID), r -> r.into(pojoClass));
    }

    @Override
    public Map<ID, P> getMapBlockingById(Collection<ID> ids) {
        return getDslContext()
                .select()
                .from(getTable())
                .where(filterActive().and(fieldID.in(ids)))
                .fetchMap(r -> r.get(fieldID), r -> r.into(pojoClass));
    }

//
// ================ Insert entity =================
//

    @Override
    public P insertBlocking(P pojo) {
        return insertBlocking(pojo, getDslContext());
    }

    @Override
    public P insertBlocking(P pojo, DSLContext context) {
        return Optional.ofNullable(context
                        .insertInto(getTable())
                        .set(toInsertQueries(getTable(), pojo))
                        .returning()
                        .fetchOne())
                .map(r -> r.into(pojoClass))
                .orElse(null);
    }

    @Override
    public void insertBlocking(Collection<P> pojos) {
        if (pojos.isEmpty()) return;
        insertBlocking(pojos, getDslContext());
    }

    @Override
    public void insertBlocking(Collection<P> pojos, DSLContext context) {
        if (pojos.isEmpty()) return;
        final List<InsertSetMoreStep<R>> insertSetMoreSteps = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap))
                .toList();
        log.info("insert into: {} - size {}", getTable().getName(), pojos.size());
        context.batch(insertSetMoreSteps).execute();
    }

//
// ====================== Insert entity duplicate key ===================
//

    // Duplicate key ignore
    @Override
    public void insertBlockingIgnoreOnDuplicateKey(P pojo) {
        getDslContext().insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onDuplicateKeyIgnore()
                .execute();
    }

    @Override
    public void insertBlockingIgnoreOnDuplicateKey(Collection<P> pojos) {
        insertBlockingIgnoreOnDuplicateKey(pojos, getDslContext());
    }

    @Override
    public void insertBlockingIgnoreOnDuplicateKey(Collection<P> pojos, DSLContext context) {
        List<InsertReturningStep<R>> moreStepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> context
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onDuplicateKeyIgnore())
                .toList();
        context.batch(moreStepList).execute();
    }

    // Duplicate key update
    @Override
    public void insertBlockingUpdateOnDuplicateKey(P pojo) {
        getDslContext().insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onDuplicateKeyUpdate()
                .set(onDuplicateKeyUpdate(pojo))
                .execute();
    }

    @Override
    public void insertBlockingUpdateOnDuplicateKey(Collection<P> pojos) {
        if (pojos.isEmpty()) return;
        insertBlockingUpdateOnDuplicateKey(pojos, getDslContext());
    }

    @Override
    public void insertBlockingUpdateOnDuplicateKey(Collection<P> pojos, DSLContext context) {
        final List<InsertOnDuplicateSetMoreStep<R>> moreStepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> context
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onDuplicateKeyUpdate()
                        .set(fieldObjectMap))
                .toList();
        context.batch(moreStepList).execute();
    }

    // Duplicate config key ignore
    @Override
    public P insertReturnBlockingIgnoreOnConfigKey(P pojo, UniqueKey<?> uniqueKey) {
        return getDslContext()
                .insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                .doNothing()
                .returning()
                .fetchOneInto(pojoClass);
    }

    @Override
    public void insertBlockingIgnoreOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey) {
        List<InsertReturningStep<R>> moreStepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                        .doNothing())
                .toList();
        getDslContext().batch(moreStepList).execute();
    }


    // Duplicate config key update
    @Override
    public P insertReturnBlockingUpdateOnConfigKey(P pojo, UniqueKey<?> uniqueKey) {
        return ofNullable(getDslContext().insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                .doUpdate()
                .set(onDuplicateKeyUpdate(pojo))
                .returning()
                .fetchOne()
        ).map(r -> r.into(pojoClass)).orElse(null);
    }

    @Override
    public void insertBlockingUpdateOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey) {
        List<InsertOnDuplicateSetMoreStep<R>> moreStepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                        .doUpdate()
                        .set(fieldObjectMap))
                .toList();
        getDslContext().batch(moreStepList).execute();
    }

    @Override
    public void insertBlockingUpdateOnConfigKey(P pojo, UniqueKey<?> uniqueKey, DSLContext context) {
        context.insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                .doUpdate()
                .set(onDuplicateKeyUpdate(pojo))
                .execute();
    }

    @Override
    public void insertBlockingUpdateOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey, DSLContext context) {
        List<InsertOnDuplicateSetMoreStep<R>> moreStepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> context
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                        .doUpdate()
                        .set(fieldObjectMap))
                .toList();
        context.batch(moreStepList).execute();
    }

//
// =================== Update entity ===================
//

    @Override
    public void updateBlocking(ID id, P pojo, DSLContext context) {
        if (fieldID == null) throw new DBException("ID_MUST_NOT_BE_NULL");
        context.update(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .where(fieldID.eq(id))
                .execute();
    }

    @Override
    public P updateReturningBlocking(ID id, UpdateField updateField) {
        if (fieldID != null) {
            return getDslContext().update(getTable())
                    .set(updateField.getFieldValueMap())
                    .where(fieldID.eq(id))
                    .returning()
                    .fetchOneInto(pojoClass);
        }
        return null;
    }

    @Override
    public Integer updateBlocking(ID id, UpdateField updateField) {
        return updateBlocking(id, updateField, getDslContext());
    }

    @Override
    public Integer updateBlocking(ID id, UpdateField updateField, DSLContext context) {
        if (fieldID == null) throw new DBException("ID_MUST_NOT_BE_NULL");
        return context.update(getTable())
                .set(updateField.getFieldValueMap())
                .where(fieldID.eq(id))
                .execute();
    }

    @Override
    public void updateBlocking(List<UpdatePojo<ID>> updatePojos) {
        List<UpdateConditionStep<R>> updateSteps = updatePojos.stream().
                map(pojo -> getDslContext().update(getTable())
                        .set(pojo.getUpdateField().getFieldValueMap())
                        .where(fieldID.eq(pojo.getId()))).toList();
        getDslContext().batch(updateSteps).execute();
    }

    @Override
    public Integer updateBlockingByCondition(Condition condition,
                                             UpdateField updateField) {
        return updateBlockingByCondition(condition, updateField, getDslContext());
    }

    @Override
    public Integer updateBlockingByCondition(Condition condition, UpdateField updateField, DSLContext context) {
        return context.update(getTable())
                .set(updateField.getFieldValueMap())
                .where(condition)
                .execute();
    }


//
// =============================================== Non Blocking ===================================================
//

//
// ============== Return pojo ===============
//

    @Override
    public Single<Optional<P>> getById(ID id) {
        if (id == null || fieldID == null) return Single.just(Optional.empty());
        return getOne(fieldID.eq(id).and(filterActive()));
    }

    @Override
    public Single<Optional<P>> getById(ID id, boolean withDeleted) {
        if (fieldID == null) return Single.just(Optional.empty());
        return withDeleted ? getOne(fieldID.eq(id)) : getOne(fieldID.eq(id).and(filterActive()));
    }

    @Override
    public Single<Optional<P>> getOne(Condition condition) {
        return rxSchedulerIo(() -> Optional.ofNullable(this.getOneBlockingByCondition(condition)));
    }

    @Override
    public Single<Optional<P>> getOneActiveByCondition(Condition condition) {
        return getOneOptional(condition.and(filterActive()));
    }


//
// ============== Return pojos ===============
//

    @Override
    public Single<List<P>> getAll() {
        return rxSchedulerIo(() -> getDslContext()
                .select()
                .from(getTable())
                .where(filterActive())
                .fetchInto(pojoClass));
    }

    @Override
    public Single<List<P>> getByIds(Collection<ID> ids) {
        if (fieldID == null) return Single.just(emptyList());
        return getMapById(ids)
                .map(idMap -> ids.stream()
                        .distinct()
                        .map(id -> idMap.getOrDefault(id, null))
                        .filter(Objects::nonNull)
                        .toList());
    }

    @Override
    public Single<List<P>> getByIds(Collection<ID> ids, boolean withDeleted) {
        if (fieldID == null) return Single.just(emptyList());
        return withDeleted ? getList(fieldID.in(ids)) : getList(fieldID.in(ids).and(filterActive()));
    }

    @Override
    public Single<List<P>> getByPageableAndIsActive(Pageable pageable) {
        Field<?> isActiveField = getTable().field(FieldConstant.IS_ACTIVE);
        if (isActiveField == null) return getActiveByPageable(pageable);
        return getActiveByPageable(pageable, isActiveField.isTrue());
    }

    @Override
    public Single<List<P>> getActiveByPageable(Pageable pageable) {
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(filterActive()
                        .and(buildSearchQueries(getTable(), pageable.getKeyword(), pageable.getFieldSearches()))
                )
                .orderBy(toSortField(pageable.getSorts(), getTable().fields()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchInto(pojoClass));
    }

    @Override
    public Single<List<P>> getActiveByCondition(Condition condition) {
        return getActiveList(condition.and(filterActive()));
    }

    @Override
    public Single<List<P>> getActiveByCondition(Condition condition, List<Order> orders) {
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(filterActive().and(condition))
                .orderBy(toSortField(orders, getTable().fields()))
                .fetchInto(pojoClass));
    }

    @Override
    public Single<List<P>> getActiveByPageable(Pageable pageable, Long userId) {
        return rxSchedulerIo(() -> getDslContext()
                .select()
                .from(getTable())
                .where(filterActive().and(createdField.eq(userId)))
                .orderBy(toSortField(pageable.getSorts(), getTable().fields()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchInto(pojoClass));
    }

    @Override
    public Single<List<P>> getActiveByPageable(Pageable pageable, Condition condition) {
        return rxSchedulerIo(() -> getActiveBlockingByPageable(pageable, condition));
    }

    @Override
    public Single<List<P>> getActiveBySearchRequest(SearchRequest searchRequest) {
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(filterActive()
                        .and(buildFilterQueries(getTable(), searchRequest.getFilters()))
                        .and(buildSearchQueries(getTable(), searchRequest.getKeyword(), searchRequest.getFieldsSearch())))
                .orderBy(toSortField(searchRequest.getSorts(), getTable().fields()))
                .offset(searchRequest.getOffset())
                .limit(searchRequest.getPageSize())
                .fetchInto(pojoClass));
    }

    @Override
    public Single<List<P>> getActiveBySearchRequest(SearchRequest searchRequest, Condition condition) {
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(filterActive()
                        .and(condition)
                        .and(buildFilterQueries(getTable(), searchRequest.getFilters()))
                        .and(buildSearchQueries(getTable(), searchRequest.getKeyword(), searchRequest.getFieldsSearch())))
                .orderBy(toSortField(searchRequest.getSorts(), getTable().fields()))
                .offset(searchRequest.getOffset())
                .limit(searchRequest.getPageSize())
                .fetchInto(pojoClass));
    }

    @Override
    public Single<List<P>> getWithOffsetLimit(int offset, int limit) {
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(filterActive())
                .offset(offset)
                .limit(limit)
                .fetchInto(pojoClass));
    }

//
// ============== Return map pojos ===============
//

    @Override
    public Single<Map<ID, P>> getMapById(Collection<ID> ids) {
        if (fieldID == null) return Single.just(emptyMap());
        return rxSchedulerIo(() -> getDslContext()
                .select()
                .from(getTable())
                .where(fieldID.in(ids).and(filterActive()))
                .fetchMap(r -> r.get(fieldID), r -> r.into(pojoClass)));
    }

    @Override
    public Single<Map<ID, P>> getMapByCondition(Condition condition) {
        if (fieldID == null) return Single.just(emptyMap());
        return rxSchedulerIo(() -> getDslContext()
                .select()
                .from(getTable())
                .where(condition.and(filterActive()))
                .fetchMap(r -> r.get(fieldID), r -> r.into(pojoClass)));
    }

    @Override
    public Single<Boolean> existByCondition(Condition condition) {
        return rxSchedulerIo(() -> getDslContext()
                .fetchExists(getTable(), filterActive()
                        .and(condition)));
    }

    @Override
    public Single<Boolean> existById(ID id) {
        return rxSchedulerIo(() -> getDslContext()
                .fetchExists(getTable(), filterActive()
                        .and(fieldID.eq(id))));
    }

    @Override
    public Single<Boolean> existAllByIds(Collection<ID> ids) {
        if (CollectionUtils.isEmpty(ids)) return Single.just(true);
        Long size = (long) ids.size();
        return rxSchedulerIo(() -> {
            Long fetchIds = getDslContext()
                    .selectCount()
                    .from(getTable())
                    .where(fieldID.in(ids)
                            .and(filterActive()))
                    .fetchOneInto(Long.class);
            return size.equals(fetchIds);
        });
    }

    //
// ============== Return the number of entities available ===============
//

    @Override
    public Single<Long> countActive() {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(filterActive())
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countActive(Long userId) {
        Condition condition = createdField != null ?
                filterActive().and(createdField.eq(userId)) : filterActive();
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(condition)
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countActive(Condition condition) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(filterActive().and(condition))
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countActive(List<Filter> filters, Condition condition) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(filterActive().and(condition)
                        .and(buildFilterQueries(getTable(), filters)))
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countBySearchRequest(SearchRequest searchRequest) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(filterActive()
                        .and(buildFilterQueries(getTable(), searchRequest.getFilters()))
                        .and(buildSearchQueries(getTable(), searchRequest.getKeyword(), searchRequest.getFieldsSearch())))
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countBySearchRequest(SearchRequest searchRequest, Condition condition) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(filterActive()
                        .and(condition)
                        .and(buildFilterQueries(getTable(), searchRequest.getFilters()))
                        .and(buildSearchQueries(getTable(), searchRequest.getKeyword(), searchRequest.getFieldsSearch())))
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countByPageable(Pageable pageable) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(filterActive()
                        .and(buildSearchQueries(getTable(), pageable.getKeyword(), pageable.getFieldSearches())))
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countByPageable(Pageable pageable, Condition condition) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(getTable())
                .where(filterActive().and(condition))
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countByPageableAndIsActive(Pageable pageable) {
        Field<?> isActiveField = getTable().field(FieldConstant.IS_ACTIVE);
        if (isActiveField == null) return countByPageable(pageable);
        return countByPageable(pageable, isActiveField.isTrue());
    }

    //
// ============== Streaming pojos ===============
//

    public Flowable<List<P>> streamingPojos(Condition condition, int fetchSize) {
        Query query = this.getDslContext()
                .select()
                .from(getTable())
                .where(this.filterActive().and(condition));
        return streamingPojos(query, fetchSize);
    }

    public Flowable<List<P>> streamingPojos(Condition condition, List<Order> orders, int fetchSize) {
        Query query = this.getDslContext()
                .select()
                .from(getTable())
                .where(this.filterActive().and(condition))
                .orderBy(toSortField(orders, getTable().fields()));
        return this.streamingPojos(query, fetchSize);
    }

    private Flowable<List<P>> streamingPojos(Query query, int fetchSize) {
        return RxFlowableTemplate.create(emitter -> this.getDslContext().connection(connection -> {
            boolean autoCommitOld = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(query.getSQL())) {
                IntStream.range(0, query.getBindValues().size())
                        .forEach(index -> {
                            try {
                                preparedStatement.setObject(index + 1, query.getBindValues().get(index));
                            } catch (SQLException e) {
                                emitter.onError(new DBException("Error setting parameter at index " + (index + 1), e));
                            }
                        });
                preparedStatement.setFetchSize(fetchSize);
                ResultSet resultSet = preparedStatement.executeQuery();
                Cursor<Record> cursor = this.getDslContext().fetchLazy(resultSet);
                int totalFetchRecord = 0;
                while (cursor.hasNext()) {
                    List<P> dataList = cursor.fetchNext(fetchSize).into(pojoClass);
                    log.trace("fetch {} record", dataList.size());
                    emitter.onNext(dataList);
                    totalFetchRecord += dataList.size();
                }
                log.debug("fetch total {} record", totalFetchRecord);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(new DBException("Error executing streaming query", e));
            } finally {
                connection.setAutoCommit(autoCommitOld);
            }
        }));
    }

//
// ============== Insert entity ===============
//

    @Override
    public Single<Integer> insert(P pojo) {
        return rxSchedulerIo(() -> getDslContext()
                .insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .execute());
    }

    protected Single<Integer> insert(P pojo, DSLContext context) {
        return rxSchedulerIo(() -> context
                .insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .execute());
    }

    @Override
    public Single<Optional<P>> insertReturning(P pojo) {
        return rxSchedulerIo(() -> ofNullable(getDslContext().insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .returning()
                .fetchOne())
                .map(r -> r.into(pojoClass)));
    }

    protected Optional<P> insertReturning(P pojo, DSLContext context) {
        return ofNullable(context.insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .returning()
                .fetchOne())
                .map(r -> r.into(pojoClass));
    }

    @Override
    public Single<List<Integer>> insert(Collection<P> pojos) {
        final List<InsertSetMoreStep<R>> insertSetMoreSteps = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap))
                .toList();

        return rxSchedulerIo(() -> partition(insertSetMoreSteps, 100)
                .stream()
                .flatMap(lists -> Arrays.stream(getDslContext().batch(lists).execute()).boxed())
                .toList());
    }

//
// ============== Insert entity duplicate key ===============
//

    // Duplicate key ignore
    @Override
    public Single<Optional<P>> insertIgnoreOnDuplicateKey(P pojo) {
        return rxSchedulerIo(() -> ofNullable(getDslContext()
                .insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onDuplicateKeyIgnore()
                .returning()
                .fetchOne())
                .map(r -> r.into(pojoClass)));
    }

    @Override
    public Single<List<Integer>> insertIgnoreOnDuplicateKey(Collection<P> pojos) {
        final List<InsertReturningStep<R>> stepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onDuplicateKeyIgnore())
                .toList();

        return rxSchedulerIo(() -> partition(stepList, 100)
                .stream()
                .flatMap(lists -> {
                    log.info("[INSERT-MANY] class: {}, size: {}",
                            pojos.getClass().getSimpleName(), lists.size());
                    return Arrays.stream(getDslContext().batch(lists).execute()).boxed();
                })
                .toList());
    }

    // Duplicate key update
    @Override
    public Single<Optional<P>> insertUpdateOnDuplicateKey(P pojo) {
        return rxSchedulerIo(() -> ofNullable(getDslContext().insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onDuplicateKeyUpdate()
                .set(onDuplicateKeyUpdate(pojo))
                .returning()
                .fetchOne())
                .map(r -> r.into(pojoClass)));
    }

    @Override
    public Single<List<Integer>> insertUpdateOnDuplicateKey(Collection<P> pojos) {
        final List<InsertOnDuplicateSetMoreStep<R>> moreStepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onDuplicateKeyUpdate()
                        .set(fieldObjectMap))
                .toList();

        return rxSchedulerIo(() -> partition(moreStepList, 100)
                .stream()
                .flatMap(lists -> Arrays.stream(getDslContext().batch(lists).execute()).boxed())
                .toList());
    }

    // Duplicate config key ignore
    @Override
    public Single<Optional<P>> insertIgnoreOnConfigKey(P pojo, UniqueKey<?> uniqueKey) {
        return rxSchedulerIo(() -> ofNullable(this.insertReturnBlockingIgnoreOnConfigKey(pojo, uniqueKey)));
    }

    @Override
    public Single<List<Integer>> insertIgnoreOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey) {
        final List<InsertReturningStep<R>> stepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                        .doNothing())
                .toList();

        return rxSchedulerIo(() -> partition(stepList, 100)
                .stream()
                .flatMap(lists -> {
                    log.info("[INSERT-MANY] class: {}, size: {}", pojos.getClass().getSimpleName(), lists.size());
                    return Arrays.stream(getDslContext().batch(lists).execute()).boxed();
                })
                .toList());
    }

    // Duplicate config key update
    @Override
    public Single<Optional<P>> insertUpdateOnConfigKey(P pojo, UniqueKey<?> uniqueKey) {
        return rxSchedulerIo(() -> ofNullable(getDslContext()
                .insertInto(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                .doUpdate()
                .set(onDuplicateKeyUpdate(pojo))
                .returning()
                .fetchOne())
                .map(r -> r.into(pojoClass)));
    }

    public Single<List<Integer>> insertUpdateOnConfigKey(Collection<P> pojos, UniqueKey<?> uniqueKey) {
        final List<InsertOnDuplicateSetMoreStep<R>> moreStepList = pojos.stream()
                .map(p -> toInsertQueries(getTable(), p))
                .map(fieldObjectMap -> getDslContext()
                        .insertInto(getTable())
                        .set(fieldObjectMap)
                        .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                        .doUpdate()
                        .set(fieldObjectMap))
                .toList();

        return rxSchedulerIo(() -> partition(moreStepList, 100)
                .stream()
                .flatMap(lists -> Arrays.stream(getDslContext().batch(lists).execute()).boxed())
                .toList());
    }

    //
// ============== Update entity ===============
//
    @Override
    public Single<Integer> updateIgnoreFieldNull(ID id, P pojo) {
        if (fieldID != null)
            return rxSchedulerIo(() -> getDslContext().update(getTable())
                    .set(recordToUpdateQueriesIgnoreFieldNull(record, pojo, getIgnoreUpdateFields()))
                    .where(fieldID.eq(id))
                    .execute());
        return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
    }


    @Override
    public Single<Integer> update(ID id, P pojo) {
        if (fieldID != null)
            return rxSchedulerIo(() -> getDslContext().update(getTable())
                    .set(toInsertQueriesUpdate(getTable(), pojo))
                    .where(fieldID.eq(id))
                    .execute());
        return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
    }

    @Override
    public Single<Integer> update(ID id, P pojo, List<String> acceptFields) {
        if (fieldID != null)
            return rxSchedulerIo(() -> getDslContext().update(getTable())
                    .set(toInsertQueries(getTable(), pojo, acceptFields))
                    .where(fieldID.eq(id))
                    .execute());
        return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
    }

    @Override
    public Single<Integer> update(ID id, UpdateField updateField) {
        if (fieldID != null)
            return rxSchedulerIo(() -> getDslContext().update(getTable())
                    .set(updateField.getFieldValueMap())
                    .where(fieldID.eq(id))
                    .execute());
        return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
    }

    @Override
    public Single<Optional<P>> updateReturning(ID id, UpdateField updateField) {
        if (fieldID != null)
            return rxSchedulerIo(() -> getDslContext().update(getTable())
                    .set(updateField.getFieldValueMap())
                    .where(fieldID.eq(id))
                    .returning()
                    .fetchOptionalInto(pojoClass));
        return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
    }

    @Override
    public Single<Optional<P>> updateReturning(ID id, P pojo) {
        return rxSchedulerIo(() -> getDslContext()
                .update(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .where(fieldID.eq(id).and(filterActive()))
                .returning()
                .fetchOptionalInto(pojoClass)
        );
    }

    @Override
    public Single<Integer> update(P pojo, Condition condition) {
        return rxSchedulerIo(() -> getDslContext().update(getTable())
                .set(toInsertQueriesUpdate(getTable(), pojo))
                .where(condition)
                .execute());
    }

    @Override
    public Single<Integer> update(P pojo, Condition... conditions) {
        return rxSchedulerIo(() -> getDslContext().update(getTable())
                .set(toInsertQueriesUpdate(getTable(), pojo))
                .where(conditions)
                .execute());
    }

    @Override
    public Single<List<Integer>> update(List<UpdatePojo<ID>> updatePojos) {
        return rxSchedulerIo(() -> {
            List<UpdateConditionStep<R>> updateSteps = updatePojos.stream().
                    map(pojo -> getDslContext().update(getTable())
                            .set(pojo.getUpdateField().getFieldValueMap())
                            .where(fieldID.eq(pojo.getId()))).toList();
            return Arrays.stream(getDslContext().batch(updateSteps)
                    .execute()).boxed().toList();
        });
    }

    //
// ============== Delete entity ===============
//

    @Override
    public Single<Integer> deletedById(ID id) {
        if (fieldID != null) {
            if (deletedField != null)
                return rxSchedulerIo(() -> getDslContext()
                        .update(getTable())
                        .set(deletedField, now().toLocalDateTime())
                        .where(fieldID.eq(id))
                        .execute());
            return rxSchedulerIo(() -> getDslContext()
                    .deleteFrom(getTable())
                    .where(fieldID.eq(id))
                    .execute());
        }
        return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
    }

    @Override
    public Single<Integer> deletedByIds(List<ID> ids) {
        if (ids.isEmpty()) return Single.just(1);
        if (fieldID != null) {
            if (deletedField != null)
                return rxSchedulerIo(() -> getDslContext().update(getTable())
                        .set(deletedField, now().toLocalDateTime())
                        .where(fieldID.in(ids))
                        .execute());
            return rxSchedulerIo(() -> getDslContext().delete(getTable())
                    .where(fieldID.in(ids))
                    .execute());
        }
        return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
    }

    @Override
    public Single<Integer> deletedByIdWithCreatedBy(ID id, Long userId) {
        if (createdField == null) return Single.error(new DBException(MessageResponse.CREATED_MUST_NOT_BE_NULL));
        if (fieldID == null) return Single.error(new DBException(MessageResponse.ID_MUST_NOT_BE_NULL));
        return checkCreatedBy(id, userId)
                .flatMap(isPermit -> {
                    if (Boolean.FALSE.equals(isPermit)) return Single.error(new DBException(NOT_PERMISSION));
                    if (deletedField != null)
                        return rxSchedulerIo(() -> getDslContext().update(getTable())
                                .set(deletedField, now().toLocalDateTime())
                                .where(fieldID.eq(id))
                                .execute());
                    return rxSchedulerIo(() -> getDslContext().delete(getTable())
                            .where(fieldID.eq(id))
                            .execute());
                });
    }

    @Override
    public Single<Integer> hardDeleteById(ID id) {
        if (fieldID == null) return Single.error(new DBException("ID_MUST_NOT_BE_NULL"));
        return rxSchedulerIo(() -> getDslContext().delete(getTable())
                .where(fieldID.eq(id))
                .execute());
    }

    @Override
    public Single<Integer> hardDeleteByIds(List<ID> ids) {
        return rxSchedulerIo(() -> getDslContext().delete(getTable())
                .where(fieldID.in(ids))
                .execute());
    }

    private Single<Boolean> checkCreatedBy(ID id, Long userId) {
        return rxSchedulerIo(() -> {
            Record fetch = getDslContext().select()
                    .from(getTable())
                    .where(fieldID.eq(id))
                    .limit(1)
                    .fetchOptional()
                    .orElse(null);
            if (fetch == null) throw new DBException(RESOURCE_NOT_FOUND);
            Long originCreatedBy = fetch.get(createdField, Long.class);
            return Objects.equals(originCreatedBy, userId);
        });
    }

//
// ================================== Common =======================================
//

    protected Single<Integer> update(Condition condition, Map<Field<?>, Object> values) {
        return rxSchedulerIo(() -> getDslContext().update(getTable())
                .set(values)
                .where(condition)
                .execute());
    }

    protected <T> Single<Integer> update(Condition condition, TableField<R, T> tableField, T value) {
        return rxSchedulerIo(() -> getDslContext().update(getTable())
                .set(tableField, value)
                .where(condition)
                .execute());
    }

    protected Single<Optional<P>> getOneOptional(OrderField<?> orderField, Condition... conditions) {
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(conditions)
                .orderBy(orderField)
                .limit(1)
                .fetchOptionalInto(pojoClass));
    }

    protected Single<Optional<P>> getOneOptional(Condition condition) {
        return rxSchedulerIo(() -> ofNullable(getDslContext().select()
                .from(getTable())
                .where(condition)
                .limit(1)
                .fetchOneInto(pojoClass)));
    }

    protected Single<Optional<P>> getOneOptional(Condition... conditions) {
        return rxSchedulerIo(() -> ofNullable(getDslContext().select()
                .from(getTable())
                .where(conditions)
                .limit(1)
                .fetchOneInto(pojoClass)));
    }

    protected Single<List<P>> getList(Condition... conditions) {
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(conditions)
                .fetchInto(pojoClass));
    }

    protected Single<List<P>> getActiveList(Condition... conditions) {
        List<Condition> conditionArrayList = Arrays.stream(conditions)
                .collect(toCollection(ArrayList::new));
        conditionArrayList.add(filterActive());
        return rxSchedulerIo(() -> getDslContext().select()
                .from(getTable())
                .where(conditionArrayList)
                .fetchInto(pojoClass));
    }


    @SuppressWarnings({"java:S1452"})
    protected Map<Field<?>, Object> onDuplicateKeyUpdate(P p) {
        return toInsertQueries(getTable(), p);
    }

    public Single<List<Integer>> insertOnConflictKeyUpdate(Collection<P> pojos) {
        return rxSchedulerIo(() -> partition((List<P>) pojos, 1000)
                .stream()
                .map(ps -> ps.stream()
                        .map(p -> toInsertQueries(getTable(), p))
                        .map(fieldObjectMap -> getDslContext()
                                .insertInto(getTable())
                                .set(fieldObjectMap)
                                .onConflict(fieldID)
                                .doUpdate()
                                .set(fieldObjectMap))
                        .toList())
                .flatMap(lists -> {
                    log.info("[START-INSERT-DATA, size: ]" + lists.size());
                    return Arrays.stream(getDslContext().batch(lists).execute()).boxed();
                })
                .toList());
    }

    public Single<List<Integer>> insertOnConflictKeyUpdate(Collection<P> pojos, UniqueKey<?> uniqueKey) {
        return rxSchedulerIo(() -> partition((List<P>) pojos, 1000)
                .stream()
                .map(ps -> ps.stream()
                        .map(p -> toInsertQueries(getTable(), p))
                        .map(fieldObjectMap -> getDslContext()
                                .insertInto(getTable())
                                .set(fieldObjectMap)
                                .onConflictOnConstraint((UniqueKey<R>) uniqueKey)
                                .doUpdate()
                                .set(fieldObjectMap))
                        .toList())
                .flatMap(lists -> {
                    log.info("[START-INSERT-DATA, size: ]" + lists.size());
                    return Arrays.stream(getDslContext().batch(lists).execute()).boxed();
                })
                .toList());
    }

    public Single<Integer> insertOnConflictKeyUpdate(P pojo) {
        Map<Field<?>, Object> fieldObjectMap = toInsertQueries(getTable(), pojo);
        return rxSchedulerIo(() -> getDslContext()
                .insertInto(getTable())
                .set(fieldObjectMap)
                .onConflict(fieldID)
                .doUpdate()
                .set(fieldObjectMap)
                .execute());
    }

    public Integer updateBlocking(P pojo, Condition condition) {
        return getDslContext().update(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .where(condition)
                .execute();
    }

    @SneakyThrows
    public Integer updateBlocking(ID id, P pojo) {
        if (fieldID == null) throw new DBException("ID_MUST_NOT_BE_NULL");
        return getDslContext().update(getTable())
                .set(toInsertQueries(getTable(), pojo))
                .where(fieldID.eq(id))
                .execute();
    }

}
