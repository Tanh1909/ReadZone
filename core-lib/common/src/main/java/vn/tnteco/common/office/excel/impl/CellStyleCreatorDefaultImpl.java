package vn.tnteco.common.office.excel.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import vn.tnteco.common.office.annotation.ExcelCellStyle;
import vn.tnteco.common.office.excel.CellStyleCreator;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class CellStyleCreatorDefaultImpl extends CellStyleCreator {

    private final Map<String, CellStyle> styleCache = new HashMap<>();

    public CellStyleCreatorDefaultImpl(Workbook workbook) {
        super(workbook);
    }

    @Override
    public CellStyle create(ExcelCellStyle excelCellStyle) {
        // Tạo key duy nhất cho mỗi combination của style properties
        String styleKey = this.createStyleKey(excelCellStyle);
        log.trace("styleKey = {}", styleKey);
        // Kiểm tra cache trước khi tạo style mới
        return styleCache.computeIfAbsent(styleKey, k -> {
            log.trace("create new style for styleKey = {}", k);
            return this.createCellStyle(excelCellStyle);
        });
    }

}
