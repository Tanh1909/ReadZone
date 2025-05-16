package vn.tnteco.common.core.model.paging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class Page<T> {
    private String key;
    private Long total;
    private Integer page;
    private Collection<T> items;
    private String maxId;
    private Boolean loadMoreAble;
    private Boolean preLoadAble;

    public Page() {
    }

    public Page(Long total, Integer page, Collection<T> items) {
        this.page = page;
        this.total = total;
        this.items = items;
    }

    public Page(Long total, Pageable pageable, Collection<T> items) {
        this.total = total;
        this.page = pageable.getPage();
        this.items = items;
        this.loadMoreAble = this.isLoadMoreAble(total, pageable.getPageSize(), pageable.getOffset());
    }

    public Page(Pageable pageable, Collection<T> items) {
        this.items = items;
        this.total = pageable.getTotal();
        this.page = pageable.getPage();
        this.loadMoreAble = this.isLoadMoreAble(pageable.getTotal(), pageable.getPageSize(), pageable.getOffset());
    }

    public Page(Long total, SearchRequest request, List<T> items) {
        this.total = total;
        this.page = request.getPage();
        this.items = items;
        this.loadMoreAble = this.isLoadMoreAble(total, request.getPageSize(), request.getOffset());
    }

    public Page(SearchRequest request, List<T> items) {
        this.total = request.getTotal();
        this.page = request.getPage();
        this.items = items;
        this.loadMoreAble = this.isLoadMoreAble(request.getTotal(), request.getPageSize(), request.getOffset());
    }

    private boolean isLoadMoreAble(Long total, Integer pageSize, Integer offset) {
        return total != null && (total > (pageSize + offset));
    }
}
