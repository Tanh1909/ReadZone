package vn.tnteco.common.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExcelCell {
    private Integer cell;
    private Object value;
}
