package vn.tnteco.common.utils.paging;

import vn.tnteco.common.core.model.paging.Order;
import vn.tnteco.common.core.model.paging.Pageable;

import java.util.List;

public class PageableQueryUtil {
    public static Pageable toPageable(Integer limit) {
        Pageable pageable = new Pageable();
        pageable.setPage(1);
        pageable.setPageSize(limit);
        return pageable;
    }

    public static Pageable maxPageSize(List<Order> sort) {
        Pageable newPageable = new Pageable();
        newPageable.setPage(1);
        newPageable.setPageSize(Integer.MAX_VALUE - 1);
        newPageable.setSorts(sort);
        return newPageable;
    }
}