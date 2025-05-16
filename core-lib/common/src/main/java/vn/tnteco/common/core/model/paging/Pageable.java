package vn.tnteco.common.core.model.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import vn.tnteco.common.annotation.QuickSearchField;
import vn.tnteco.common.core.model.filter.Search;
import vn.tnteco.common.utils.ReflectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Pageable {

    public static final Integer DEFAULT_PAGE = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 1000;
    public static final Integer MAXIMUM_PAGE_SIZE = 1000;

    @Schema(title = "page tìm kiếm: bắt đấu từ 1")
    private int page;

    @Schema(title = "từ khóa tìm kiếm")
    private String keyword;

    @Schema(title = "số lượng item của 1 page")
    private int pageSize;

    @Schema(title = "Trường hợp muốn lấy từ offset")
    private Integer offset;

    @Schema(title = "Thời gian bắt đầu với trường hợp tìm kiếm/thống kê theo khoảng thời gian")
    private LocalDateTime fromDate;

    @Schema(title = "Thời gian kết thúc với trường hợp tìm kiếm/thống kê theo khoảng thời gian")
    private LocalDateTime toDate;

    private List<Order> sorts;

    private List<Search> fieldSearches;

    // System
    private Long total;

    public Pageable() {
        this.page = DEFAULT_PAGE;
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.offset = Math.max((page - 1) * pageSize, 0);
        this.total = 0L;
    }

    public Pageable(int page, int pageSize) {
        this.page = page > 0 ? page : DEFAULT_PAGE;
        this.pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        this.offset = Math.max((page - 1) * pageSize, 0);
        this.total = 0L;
    }

    public Integer getOffset() {
        if (offset == null || offset <= 0) {
            return Math.max((page - 1) * pageSize, 0);
        }
        return offset;
    }

    public String getKeyword() {
        if (keyword == null) return null;
        return keyword.trim();
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

    public void setSearches(Class<?> responseClass) {
        if (responseClass == null) return;
        List<QuickSearchField> quickSearchFields = ReflectUtils.getListAnnotationInFields(responseClass, QuickSearchField.class);
        List<Search> searches = new ArrayList<>();
        for (QuickSearchField quickSearchField : quickSearchFields) {
            searches.add(new Search(quickSearchField.columnName(), quickSearchField.searchOption()));
        }
        this.fieldSearches = searches;
    }

}
