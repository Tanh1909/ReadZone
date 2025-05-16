
package vn.tnteco.common.core.model.paging;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import vn.tnteco.common.utils.TimeUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class PageableParamParser {

    public PageableParamParser() {
    }

    public Class<Pageable> type() {
        return Pageable.class;
    }

    public static Pageable parser(Map<String, String[]> parameters) {
        return parser(parameters, new Pageable());
    }

    public static Pageable parser(Map<String, String[]> parameters, Pageable pageable) {
        int page = Pageable.DEFAULT_PAGE;
        if (parameters.containsKey("page") && isNotEmpty(parameters.get("page")[0])) {
            page = NumberUtils.toInt(parameters.get("page")[0], Pageable.DEFAULT_PAGE);
        }
        pageable.setPage(page);

        if (parameters.containsKey("from_date") && isNotEmpty(parameters.get("from_date")[0])) {
            pageable.setFromDate(TimeUtils.epochMilliToLocalDateTimeOrNow(Long.valueOf(parameters.get("from_date")[0])));
        } else if (parameters.containsKey("fromDate") && isNotEmpty(parameters.get("fromDate")[0])) {
            pageable.setFromDate(TimeUtils.epochMilliToLocalDateTimeOrNow(Long.valueOf(parameters.get("fromDate")[0])));
        }

        if (parameters.containsKey("to_date") && isNotEmpty(parameters.get("to_date")[0])) {
            pageable.setToDate(TimeUtils.epochMilliToLocalDateTimeOrNow(Long.valueOf(parameters.get("to_date")[0])));
        } else if (parameters.containsKey("toDate") && isNotEmpty(parameters.get("toDate")[0])) {
            pageable.setToDate(TimeUtils.epochMilliToLocalDateTimeOrNow(Long.valueOf(parameters.get("toDate")[0])));
        }

        String keyword = null;
        if (parameters.containsKey("keyword") && isNotEmpty(parameters.get("keyword")[0])) {
            keyword = parameters.get("keyword")[0];
        } else if (parameters.containsKey("query") && isNotEmpty(parameters.get("query")[0])) {
            keyword = parameters.get("query")[0];
        }
        pageable.setKeyword(keyword);

        int pageSize = Pageable.DEFAULT_PAGE_SIZE;
        if (parameters.containsKey("per_page") && isNotEmpty(parameters.get("per_page")[0])) {
            pageSize = NumberUtils.toInt(parameters.get("per_page")[0], Pageable.DEFAULT_PAGE_SIZE);
        } else if (parameters.containsKey("perPage") && isNotEmpty(parameters.get("perPage")[0])) {
            pageSize = NumberUtils.toInt(parameters.get("perPage")[0], Pageable.DEFAULT_PAGE_SIZE);
        } else if (parameters.containsKey("page_size") && isNotEmpty(parameters.get("page_size")[0])) {
            pageSize = NumberUtils.toInt(parameters.get("page_size")[0], Pageable.DEFAULT_PAGE_SIZE);
        } else if (parameters.containsKey("pageSize") && isNotEmpty(parameters.get("pageSize")[0])) {
            pageSize = NumberUtils.toInt(parameters.get("pageSize")[0], Pageable.DEFAULT_PAGE_SIZE);
        }
        pageSize = pageSize < 0 ? Pageable.MAXIMUM_PAGE_SIZE : pageSize;
        pageable.setPageSize(pageSize);

        String offset = null;
        if (parameters.containsKey("offset") && isNotEmpty(parameters.get("offset")[0])) {
            offset = parameters.get("offset")[0];
        }
        pageable.setOffset(NumberUtils.toInt(offset, (page - 1) * pageSize));

        if (parameters.containsKey("sort") && isNotEmpty(parameters.get("sort")[0])) {
            List<Order> orders = getOrder(parameters.get("sort"));
            if (CollectionUtils.isNotEmpty(orders)) {
                pageable.setSorts(orders);
            }
        } else if (parameters.containsKey("sort_by") && isNotEmpty(parameters.get("sort_by")[0])) {
            List<Order> orders = getOrderBy_(parameters.get("sort_by"));
            if (CollectionUtils.isNotEmpty(orders)) {
                pageable.setSorts(orders);
            }
        } else if (parameters.containsKey("sortBy") && isNotEmpty(parameters.get("sortBy")[0])) {
            List<Order> orders = getOrderBy_(parameters.get("sortBy"));
            if (CollectionUtils.isNotEmpty(orders)) {
                pageable.setSorts(orders);
            }
        }
        return pageable;
    }

    private static List<Order> getOrder(String[] orders) {
        return orders == null ?
                null :
                Stream.of(orders)
                        .filter(Objects::nonNull)
                        .map(PageableParamParser::getOrder)
                        .filter(Objects::nonNull)
                        .collect(toList());
    }

    private static List<Order> getOrderBy_(String[] orders) {
        return orders == null ?
                null :
                Stream.of(orders)
                        .filter(Objects::nonNull)
                        .map(PageableParamParser::getOrderBy_)
                        .collect(toList());
    }

    private static Order getOrder(String order) {
        String[] arr = order.split(",");
        if (arr.length == 1) {
            return new Order(arr[0], Order.Direction.DESC.name());
        } else if (arr.length != 2) {
            return null;
        } else {
            return new Order(arr[0], arr[1].toUpperCase());
        }
    }

    private static Order getOrderBy_(String order) {
        String[] arr = order.split("_");
        if (arr.length <= 1 || !(order.endsWith(Order.Direction.DESC.name()) || order.endsWith(Order.Direction.ASC.name()))) {
            return new Order(order, Order.Direction.DESC.name());
        } else {
            String direction = order.substring(order.lastIndexOf("_") + 1);
            String field = order.substring(0, order.lastIndexOf("_"));
            return new Order(field, direction);
        }
    }
}