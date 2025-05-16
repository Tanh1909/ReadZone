package vn.tnteco.repository.builder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import java.util.Collection;

public class ConditionBuilder {

    private Condition condition = DSL.noCondition();

    public static ConditionBuilder create() {
        return new ConditionBuilder();
    }

    public Condition build() {
        return this.condition;
    }

    // Method for "equal" condition with null/empty check
    public <R extends Record, T> ConditionBuilder andEqIfNotEmpty(TableField<R, T> tableField, T data) {
        if (data instanceof String dataStr) {
            if (StringUtils.isNotEmpty(dataStr)) {
                this.condition = this.condition.and(tableField.eq(data));
            }
        } else {
            if (ObjectUtils.isNotEmpty(data)) {
                this.condition = this.condition.and(tableField.eq(data));
            }
        }
        return this;
    }

    // Method for "between" condition with null/empty check
    public <R extends Record, T> ConditionBuilder andBetweenIfNotEmpty(TableField<R, T> tableField, T fromData, T toData) {
        if (fromData instanceof String fromStr && toData instanceof String toStr) {
            if (StringUtils.isNotEmpty(fromStr) && StringUtils.isNotEmpty(toStr)) {
                this.condition = this.condition.and(tableField.between(fromData, toData));
            }
        } else {
            if (ObjectUtils.isNotEmpty(fromData) && ObjectUtils.isNotEmpty(toData)) {
                this.condition = this.condition.and(tableField.between(fromData, toData));
            }
        }
        return this;
    }

    // Method for "greater than" condition with null/empty check
    public <R extends Record, T> ConditionBuilder andGtIfNotEmpty(TableField<R, T> tableField, T data) {
        if (data instanceof String dataStr) {
            if (StringUtils.isNotEmpty(dataStr)) {
                this.condition = this.condition.and(tableField.gt(data));
            }
        } else {
            if (ObjectUtils.isNotEmpty(data)) {
                this.condition = this.condition.and(tableField.gt(data));
            }
        }
        return this;
    }
    public <R extends Record, T> ConditionBuilder orGtIfNotEmpty(TableField<R, T> tableField, T data) {
        if (data instanceof String dataStr) {
            if (StringUtils.isNotEmpty(dataStr)) {
                this.condition = this.condition.or(tableField.gt(data));
            }
        } else {
            if (ObjectUtils.isNotEmpty(data)) {
                this.condition = this.condition.or(tableField.gt(data));
            }
        }
        return this;
    }

    // Method for "greater than or equal" condition with null/empty check
    public <R extends Record, T> ConditionBuilder andGteIfNotEmpty(TableField<R, T> tableField, T data) {
        if (data instanceof String dataStr) {
            if (StringUtils.isNotEmpty(dataStr)) {
                this.condition = this.condition.and(tableField.greaterOrEqual(data));
            }
        } else {
            if (ObjectUtils.isNotEmpty(data)) {
                this.condition = this.condition.and(tableField.greaterOrEqual(data));
            }
        }
        return this;
    }

    // Method for "less than" condition with null/empty check
    public <R extends Record, T> ConditionBuilder andLtIfNotEmpty(TableField<R, T> tableField, T data) {
        if (data instanceof String dataStr) {
            if (StringUtils.isNotEmpty(dataStr)) {
                this.condition = this.condition.and(tableField.lt(data));
            }
        } else {
            if (ObjectUtils.isNotEmpty(data)) {
                this.condition = this.condition.and(tableField.lt(data));
            }
        }
        return this;
    }

    public <R extends Record, T> ConditionBuilder orLtIfNotEmpty(TableField<R, T> tableField, T data) {
        if (data instanceof String dataStr) {
            if (StringUtils.isNotEmpty(dataStr)) {
                this.condition = this.condition.or(tableField.lt(data));
            }
        } else {
            if (ObjectUtils.isNotEmpty(data)) {
                this.condition = this.condition.or(tableField.lt(data));
            }
        }
        return this;
    }

    // Method for "less than or equal" condition with null/empty check
    public <R extends Record, T> ConditionBuilder andLteIfNotEmpty(TableField<R, T> tableField, T data) {
        if (data instanceof String dataStr) {
            if (StringUtils.isNotEmpty(dataStr)) {
                this.condition = this.condition.and(tableField.lessOrEqual(data));
            }
        } else {
            if (ObjectUtils.isNotEmpty(data)) {
                this.condition = this.condition.and(tableField.lessOrEqual(data));
            }
        }
        return this;
    }

    // Method for "in" condition
    public <R extends Record, T> ConditionBuilder andInIfNotEmpty(TableField<R, T> tableField, Collection<T> data) {
        if (CollectionUtils.isNotEmpty(data)) {
            this.condition = this.condition.and(tableField.in(data));
        }
        return this;
    }
    public <R extends Record, T> ConditionBuilder andInIfNotEmpty(TableField<R, T> tableField, T[] data) {
        if (ArrayUtils.isNotEmpty(data)) {
            this.condition = this.condition.and(tableField.in(data));
        }
        return this;
    }

    public ConditionBuilder and(Condition condition) {
        if (condition != null) {
            this.condition = this.condition.and(condition);
        }
        return this;
    }

    public ConditionBuilder or(Condition condition) {
        if (condition != null) {
            this.condition = this.condition.or(condition);
        }
        return this;
    }
}
