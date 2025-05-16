package vn.tnteco.common.annotation;

import vn.tnteco.common.core.model.query.SearchOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuickSearchField {

    String columnName() default "";

    SearchOption searchOption() default SearchOption.LIKE;

}
