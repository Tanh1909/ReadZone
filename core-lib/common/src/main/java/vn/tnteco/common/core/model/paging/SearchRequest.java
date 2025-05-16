package vn.tnteco.common.core.model.paging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import vn.tnteco.common.core.model.filter.Filter;
import vn.tnteco.common.core.model.filter.Search;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class SearchRequest {

    @Schema(title = "page tìm kiếm: bắt đấu từ 1")
    private int page;

    @Schema(title = "từ khóa tìm kiếm")
    private String keyword;

    @Schema(title = "số lượng item của 1 page")
    private int pageSize = Pageable.MAXIMUM_PAGE_SIZE;

    @Schema(title = "Thời gian bắt đầu với trường hợp tìm kiếm/thống kê theo khoảng thời gian")
    private LocalDateTime fromDate;

    @Schema(title = "Thời gian kết thúc với trường hợp tìm kiếm/thống kê theo khoảng thời gian")
    private LocalDateTime toDate;

    private List<Order> sorts;

    private List<Filter> filters;

    // System
    private Long total;

    private List<Search> fieldsSearch;

    @JsonIgnore
    public Integer getOffset() {
        return Math.max((page - 1) * pageSize, 0);
    }

    public int getPageSize() {
        if (this.pageSize < 0) return Pageable.MAXIMUM_PAGE_SIZE;
        return pageSize;
    }

    public List<Order> getSorts() {
        if (sorts == null) {
            sorts = new ArrayList<>();
        }
        if (sorts.isEmpty()) {
            sorts.add(new Order()
                    .setProperty("id")
                    .setDirection(Order.Direction.DESC.name()));
        }
        return sorts;
    }

    public String getKeyword() {
        if (keyword == null) return null;
        return keyword.trim();
    }

    public List<Filter> getFilters() {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        return filters;
    }

    public SearchRequest addFilter(Filter filter) {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        filters.add(filter);
        return this;
    }

    public void addOrder(Order order) {
        if (this.sorts == null) {
            sorts = new ArrayList<>();
        }
        sorts.add(order);
    }

    public void addOrderDefaultIfEmpty(Order order) {
        if (CollectionUtils.isEmpty(sorts)) {
            sorts = List.of(order);
        }
    }

}