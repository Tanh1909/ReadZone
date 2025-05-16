package vn.tnteco.repository.mapper;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.TableImpl;
import vn.tnteco.common.core.json.JsonObject;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.springframework.util.CollectionUtils.isEmpty;

public class SingleRecordMapper<R extends Record, E> implements RecordMapper<R, E> {

    private Class<E> clazz;
    private Map<String, List<Field<?>>> prefixMapper;
    private R record;

    public SingleRecordMapper(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public E map(R record) {
        this.record = record;
        prefixMapper = Stream.of(record.fields()).collect(groupingBy(this::extractFieldPrefix));

        Map<String, Object> finalMap = extractByLayer(clazz);
        return new JsonObject(finalMap).mapTo(clazz);
    }

    private Map<String, Object> extractWrapperToMap(List<Field<?>> fields) {
        return fields
                .stream()
                .filter(f -> record.getValue(f) != null)
                .collect(toMap(this::extractFieldName, f -> record.getValue(f)));
    }

    private Map<String, Object> extractByLayer(Class<?> clazz) {

        String prefixName = snakeCaseClassName(clazz);
        List<Field<?>> objFields = prefixMapper.get(prefixName);
//        if (objFields == null) return null;

        // distinct wrapper, list, and pojo
        List<List<java.lang.reflect.Field>> grouped = groupFields(clazz.getDeclaredFields());
        Map<String, Object> map = new HashMap<>();
        // wrapper class
        if (!isEmpty(grouped.get(0))) {
            List<String> wrappersFieldNames = grouped.get(0).stream().map(this::snakeCaseFieldName).collect(toList());
            List<Field<?>> wrapperFields = objFields
                    .stream()
                    .filter(f -> wrappersFieldNames.contains(snakeCaseFieldName(f)))
                    .collect(toList());
            map = new HashMap<>(extractWrapperToMap(wrapperFields));
        }

        // list
        if (!isEmpty(grouped.get(1))) {
            Map<String, List<Map<String, Object>>> listMap = grouped.get(1).stream()
                    .map(f -> {
                        ParameterizedType genericType = (ParameterizedType) f.getGenericType();
                        Class genericClass = (Class) genericType.getActualTypeArguments()[0];
                        return Pair.of(snakeCaseFieldName(f), asList(extractByLayer(genericClass)));
                    }) // cast to generic type
                    .filter(pair -> pair.getRight() != null)
                    .collect(toMap(Pair::getLeft, Pair::getRight));
            map.putAll(listMap);
        }

        // pojo
        if (!isEmpty(grouped.get(2))) {
            Map<String, Map<String, Object>> pojoMap = grouped.get(2)
                    .stream()
                    .map(f -> Pair.of(snakeCaseFieldName(f), extractByLayer(f.getType())))
                    .filter(pair -> pair.getRight() != null)
                    .collect(toMap(Pair::getLeft, Pair::getRight));

            map.putAll(pojoMap);
        }

        return map;

    }

    private String extractFieldPrefix(Field<?> field) {
        try {
            java.lang.reflect.Field tableField = field.getClass().getDeclaredField("table");
            tableField.setAccessible(true);
            TableImpl table = (TableImpl) tableField.get(field);
            return table.getName();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<List<java.lang.reflect.Field>> groupFields(java.lang.reflect.Field[] fields) {
        Map<Boolean, List<java.lang.reflect.Field>> grouped = Stream.of(fields).collect(partitioningBy(this::isSingleField));
        grouped.get(true);

        Map<Boolean, List<java.lang.reflect.Field>> isLists = grouped.get(false).stream().collect(partitioningBy(this::isListField));
        // wrapper fields, list fields, pojo fields
        return asList(grouped.get(true), isLists.get(true), isLists.get(false));
    }

    private boolean isSingleField(java.lang.reflect.Field field) {
        return field.getType().getName().startsWith("java") && !List.class.isAssignableFrom(field.getType());
    }

    private boolean isListField(java.lang.reflect.Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    private String snakeCaseFieldName(java.lang.reflect.Field field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }

    private String snakeCaseFieldName(Field<?> field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }

    private String snakeCaseClassName(Class clazz) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName());
    }

    private String extractFieldName(Field<?> field) {
        return field.getName();
    }

}
