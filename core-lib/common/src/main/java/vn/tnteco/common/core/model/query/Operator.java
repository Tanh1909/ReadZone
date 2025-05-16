package vn.tnteco.common.core.model.query;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public enum Operator {

    INCLUDE("include"),
    IN("in"),
    NIN("nin"),
    EQUAL("eq"),
    LIKE("like"),
    NOT_EQUAL("ne"),
    GREATER_THAN("gt"),
    LESS_THAN("lt"),
    GREATER_THAN_OR_EQUAL("gte"),
    LESS_THAN_OR_EQUAL("lte"),
    IS_NULL("isNull"),
    NONE("none");

    private final String operator;

    public String operator() {
        return operator;
    }

    private static final Map<String, Operator> mappingOperator = new HashMap<>();

    static {
        for (Operator operatorEnum : Operator.values()) {
            mappingOperator.put(operatorEnum.operator, operatorEnum);
        }
    }

    public static Operator from(String operator) {
        return mappingOperator.getOrDefault(operator, NONE);
    }

    public static List<String> operatorFilterListValue() {
        return Arrays.asList(Operator.IN.operator(), Operator.NIN.operator());
    }

}
