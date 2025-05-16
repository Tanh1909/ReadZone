package vn.tnteco.common.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import vn.tnteco.common.core.model.paging.Page;

import java.util.Collections;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageUtils {

    public static final Page<?> EMPTY_PAGE = new Page<>(0L, 0, Collections.emptyList());

    public static <T> Page<T> emptyPage() {
        return (Page<T>) EMPTY_PAGE;
    }
}
