package vn.tnteco.spring.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class ResultResponse<T> {
    private Integer perPage;
    private Long total;
    private Long to;
    private Long from;
    private Integer currentPage;
    private Integer nextPage;
    private List<T> data;

    public Integer getNextPage() {
        if (total == null || perPage == null || currentPage == null) {
            return null;
        }
        return Math.max(currentPage * perPage, 0) >= total ? null : currentPage + 1;
    }
}
