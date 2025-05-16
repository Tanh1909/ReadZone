package vn.tnteco.common.core.model;

import lombok.Data;

import java.util.List;

@Data
public abstract class ExcelRow {
    private Integer row;
    private List<ExcelCell> cells;
}
