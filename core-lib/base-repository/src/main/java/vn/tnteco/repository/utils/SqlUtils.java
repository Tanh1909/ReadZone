package vn.tnteco.repository.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.jooq.impl.UpdatableRecordImpl;
import vn.tnteco.common.core.exception.DBException;
import vn.tnteco.common.core.json.JsonArray;
import vn.tnteco.common.core.json.JsonObject;
import vn.tnteco.common.core.model.filter.Filter;
import vn.tnteco.common.core.model.filter.Search;
import vn.tnteco.common.core.model.paging.Order;
import vn.tnteco.common.core.model.query.Operator;
import vn.tnteco.common.core.model.query.SearchOption;
import vn.tnteco.common.utils.StringUtils;
import vn.tnteco.common.utils.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.math.NumberUtils.createLong;
import static org.jooq.impl.DSL.trueCondition;

@UtilityClass
@SuppressWarnings({"java:S1452"})
public class SqlUtils {
    private final static String UPDATED_AT = "updated_at";
    private final static String CREATED_AT = "created_at";
    public static String ID_FIELD_NAME = "id";

    @SafeVarargs
    public static <T extends UpdatableRecordImpl<T>> Map<Field<?>, Object>
    recordToUpdateQueries(T recordUpdate, Object o, TableField<T, ?>... ignoreFields) {
        recordUpdate.from(o);
        Map<Field<?>, Object> values = new HashMap<>();
        for (Field<?> field : recordUpdate.fields()) {
            Object valueField = recordUpdate.getValue(field);
            if (valueField != null && Arrays.stream(ignoreFields).noneMatch(field::equals))
                values.put(field, valueField);
        }
        return values;
    }

    public static <T extends TableRecordImpl<T>> Map<Field<?>, Object>
    toInsertQueries(T newRecord, Object o) {
        newRecord.from(o);
        return getInsertQueries(newRecord);
    }

    public static <R extends Record> Map<Field<?>, Object>
    toInsertQueries(TableImpl<R> table, Object o) {
        R newRecord = table.newRecord();
        newRecord.from(o);
        return getInsertQueries(newRecord);
    }

    public static <R extends Record> Map<Field<?>, Object>
    toInsertQueriesUpdate (TableImpl<R> table, Object o) {
        R newRecord = table.newRecord();
        newRecord.from(o);
        return getInsertQueriesUpdate (newRecord);
    }

    private static <R extends Record> Map<Field<?>, Object> getInsertQueriesUpdate (R newRecord) {
        Map<Field<?>, Object> values = new HashMap<>();
        for (Field<?> field : newRecord.fields()) {
            if (field.getName().equalsIgnoreCase(ID_FIELD_NAME)) {
                continue;
            }
            if (UPDATED_AT.equals(field.getName())) {
                values.put(field, LocalDateTime.now());
            }
            Object valueField = newRecord.getValue(field);
            values.put(field, valueField);
        }
        return values;
    }

    private static <R extends Record> Map<Field<?>, Object> getInsertQueries(R newRecord) {
        Map<Field<?>, Object> values = new HashMap<>();
        for (Field<?> field : newRecord.fields()) {
            if (field.getName().equalsIgnoreCase(ID_FIELD_NAME)) {
                continue;
            }
            if (CREATED_AT.equals(field.getName())) {
                values.put(field, LocalDateTime.now());
            }
            if (UPDATED_AT.equals(field.getName())) {
                values.put(field, LocalDateTime.now());
            }
            Object valueField = newRecord.getValue(field);
            values.put(field, valueField);
        }
        return values;
    }

    public static <R extends TableRecordImpl<R>, P> Map<Field<?>, Object> recordToUpdateQueriesIgnoreFieldNull (R record, P o,
                                                                                                List<TableField<R, ?>> ignoreUpdatableFields) {
        record.from(o);
        Map<Field<?>, Object> values = new HashMap<>();
        for (Field<?> f : record.fields()) {
            if (record.getValue(f) != null) {
                if (ignoreUpdatableFields.stream().noneMatch(f::equals))
                    values.put(f, record.getValue(f));
            }
            if (UPDATED_AT.equals(f.getName())) {
                values.put(f, LocalDateTime.now());
            }
        }
        return values;
    }

    public static <T extends TableRecordImpl<T>> Map<Field<?>, Object>
    toInsertQueries(T newRecord, Object o, List<String> acceptFields) {
        newRecord.from(o);
        return getMapInsertQueries(newRecord, acceptFields);
    }


    public static <R extends Record> Map<Field<?>, Object>
    toInsertQueries(TableImpl<R> table, Object o, List<String> acceptFields) {
        R newRecord = table.newRecord();
        newRecord.from(o);
        return getMapInsertQueries(newRecord, acceptFields);
    }

    private static <R extends Record> Map<Field<?>, Object> getMapInsertQueries(R newRecord, List<String> acceptFields) {
        final HashSet<String> acceptFieldSet = new HashSet<>(acceptFields);
        Map<Field<?>, Object> values = new HashMap<>();
        for (Field<?> field : newRecord.fields()) {
            if (field.getName().equalsIgnoreCase(ID_FIELD_NAME)) {
                continue;
            }
            if (acceptFieldSet.contains(field.getName())) {
                values.put(field, newRecord.getValue(field));
            }
            if (UPDATED_AT.equals(field.getName())) {
                values.put(field, LocalDateTime.now());
            }
        }
        return values;
    }

    public static <R extends Record> Pair<List<Field<?>>, List<Param<?>>>
    toSelectInsertQueries(TableImpl<R> table, Object o) {
        R newRecord = table.newRecord();
        newRecord.from(o);
        List<Field<?>> fields  = new ArrayList<>();
        List<Param<?>> values = new ArrayList<>();
        for (Field<?> field : newRecord.fields()) {
            if (field.getName().equalsIgnoreCase(ID_FIELD_NAME)) {
                continue;
            }
            Object valueField = newRecord.getValue(field);
            fields.add(field);
            values.add(DSL.val(valueField));
        }
        return Pair.of(fields, values);
    }

    public static List<SortField<Object>> toSortField(List<Order> orderProperties, Field<?>[] fields) {
        if (isEmpty(orderProperties)) return new ArrayList<>();
        Set<String> fieldNames = Arrays.stream(fields)
                .map(Field::getName)
                .collect(Collectors.toSet());
        return orderProperties
                .stream()
                .filter(order -> fieldNames.contains(order.getProperty()))
                .map(order -> {
                    if (order.getDirection().equals(Order.Direction.ASC.name()))
                        return DSL.field(order.getProperty()).asc();
                    else return DSL.field(order.getProperty()).desc();
                })
                .toList();
    }

    public static <R extends Record> Condition buildFilterQueries(Table<R> table, List<Filter> fieldFilters) {
        if (isEmpty(fieldFilters)) return DSL.noCondition();
        Condition condition = DSL.noCondition();
        for (Filter fieldFilter : fieldFilters) {
            Field field = table.field(StringUtils.toSnakeCase(fieldFilter.getName()));
            if (field != null) {
                Object valueByClass = castValueByClass(fieldFilter.getOperation(), fieldFilter.getValue(), field.getType());
                if (valueByClass != null) {
                    condition = condition.and(buildFilterCondition(fieldFilter.getOperation(), field, valueByClass));
                } else {
                    condition = condition.and(field.isNull());
                }
            }
        }
        return condition;
    }

    public static <R extends Record> Condition buildSearchQueries(Table<R> table, String keyword, List<Search> fieldSearches) {
        if (CollectionUtils.isEmpty(fieldSearches)) return DSL.noCondition();
        if (isEmpty(keyword)) return DSL.noCondition();
        Condition condition = DSL.noCondition();
        for (Search fieldSearch : fieldSearches) {
            Field<?> field = table.field(fieldSearch.getName());
            if (field == null || field.getType() != String.class) {
                throw new DBException("Search field " + fieldSearch.getName() + " invalid !!!");
            }
            Condition searchCondition = buildSearchCondition(keyword, fieldSearch.getOperation(), (Field<String>) field);
            condition = condition.or(searchCondition);
        }
        return condition;
    }

    private static Condition buildSearchCondition(String keyword, SearchOption option, Field<String> stringField) {
        if (option == null) {
            throw new DBException("Search option invalid");
        }
        return switch (option) {
            case EQUAL -> stringField.like(keyword);
            case LIKE -> stringField.like(String.format("%%%s%%", keyword));
            case LIKE_REGEX -> stringField.likeRegex(keyword);
            case LIKE_IGNORE_CASE -> stringField.likeIgnoreCase(String.format("%%%s%%", keyword));
            case LIKE_IGNORE_ACCENT -> throw new UnsupportedOperationException("Unsupported operation LIKE_IGNORE_ACCENT");
            case LIKE_IGNORE_CASE_AND_ACCENT -> throw new UnsupportedOperationException("Unsupported operation LIKE_IGNORE_CASE_AND_ACCENT");
        };
    }

    private static Condition buildFilterCondition(String operation, Field<Object> field, Object value) {
        var operator = Operator.from(operation);
        return switch (operator) {
            case IN -> field.in(value);
            case NIN -> field.notIn(value);
            case EQUAL -> field.eq(value);
            case LIKE -> field.likeRegex((String) value);
            case NOT_EQUAL -> field.ne(value);
            case GREATER_THAN -> field.gt(value);
            case LESS_THAN -> field.lt(value);
            case GREATER_THAN_OR_EQUAL -> field.greaterOrEqual(value);
            case LESS_THAN_OR_EQUAL -> field.lessOrEqual(value);
            case IS_NULL -> field.isNull();
            default -> trueCondition();
        };
    }

    private static <V> Object castValueByClass(String operation, Object value, Class<V> classValue) {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(operation) && Operator.operatorFilterListValue().contains(operation)) {
            final JsonArray array = new JsonArray(value.toString());
            return array.stream()
                    .map(object -> castValueByClass(null, object, classValue))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        try {
            if (classValue.getSimpleName().equalsIgnoreCase(Integer.class.getSimpleName()))
                return value;
            if (classValue.getSimpleName().equalsIgnoreCase(Long.class.getSimpleName()))
                return value;
            if (classValue.getSimpleName().equalsIgnoreCase(Double.class.getSimpleName()))
                return value;
            if (classValue.getSimpleName().equalsIgnoreCase(Boolean.class.getSimpleName()))
                return value;
            if (classValue.getSimpleName().equalsIgnoreCase(BigDecimal.class.getSimpleName()))
                return new BigDecimal(value.toString());
            if (classValue.getSimpleName().equalsIgnoreCase(LocalDateTime.class.getSimpleName())) {
                return TimeUtils.epochMilliToLocalDateTimeOrNow(createLong(value.toString()));
            }
            if (classValue.getSimpleName().equalsIgnoreCase(Timestamp.class.getSimpleName()))
                return new Timestamp(createLong(value.toString()));
            if (String.class.isAssignableFrom(classValue)) return value;
            return JsonObject.mapFrom(value).mapTo(classValue);
        } catch (Exception e) {
            return null;
        }
    }

}
