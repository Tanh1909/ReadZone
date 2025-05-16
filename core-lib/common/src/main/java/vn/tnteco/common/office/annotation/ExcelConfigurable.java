package vn.tnteco.common.office.annotation;

import vn.tnteco.common.office.excel.CellStyleCreator;
import vn.tnteco.common.office.excel.impl.CellStyleCreatorDefaultImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelConfigurable {

    int sheetIndex();

    int headerIndex();

    int startRow();

    int[] placeholderRowIndexes() default {};

    Class<? extends CellStyleCreator> cellStyleCreator() default CellStyleCreatorDefaultImpl.class;
}
