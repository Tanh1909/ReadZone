package vn.tnteco.repository.builder;

import org.jooq.EnumType;
import org.jooq.Field;

import java.util.HashMap;
import java.util.Map;

public class UpdateField {
    public UpdateField() {
    }

    public UpdateField(Field<?> field, EnumType enumType) {
        this.add(field, enumType);
    }

    public UpdateField(Field<?> field, Object value) {
        this.add(field, value);
    }

    private Map<Field<?>, Object> fieldValueMap;

    public UpdateField add(Field<?> field, Object value) {
        getFieldValueMap().put(field, value);
        return this;
    }

    public UpdateField add(Field<?> field, EnumType value) {
        getFieldValueMap().put(field, value.getLiteral());
        return this;
    }

    public Map<Field<?>, Object> getFieldValueMap() {
        if (fieldValueMap == null) fieldValueMap = new HashMap<>();
        return fieldValueMap;
    }
}
