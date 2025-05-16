package vn.tnteco.spring.model;

import lombok.Data;
import lombok.experimental.Accessors;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;

import java.util.List;


@Data
@Accessors(chain = true)
public class ListAndSummaryResponse<L, S> {
    private Integer perPage;
    private Long total;
    private Long to;
    private Long from;
    private Integer currentPage;
    private Integer nextPage;
    private List<L> data;
    private S summary;

    public Integer getNextPage() {
        if (total == null || perPage == null || currentPage == null) {
            return null;
        }
        return Math.max(currentPage * perPage, 0) >= total ? null : currentPage + 1;
    }


    public ListAndSummaryResponse() {

    }

    public ListAndSummaryResponse<L, S> setPageInfo(SearchRequest searchRequest) {
        this.currentPage = searchRequest.getPage();
        this.perPage = searchRequest.getPageSize();
        this.total = searchRequest.getTotal();
        return this;
    }

    public ListAndSummaryResponse<L, S> setPageInfo(Pageable searchRequest) {
        this.currentPage = searchRequest.getPage();
        this.perPage = searchRequest.getPageSize();
        this.total = searchRequest.getTotal();
        return this;
    }

    public ListAndSummaryResponse(Pageable pageable, List<L> result, S summary) {
        this.currentPage = pageable.getPage();
        this.perPage = pageable.getPageSize();
        this.total = pageable.getTotal();
        this.data = result;
        this.summary = summary;
    }
}
