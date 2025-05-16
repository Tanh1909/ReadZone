package vn.tnteco.spring.model;

import lombok.Data;
import lombok.experimental.Accessors;
import vn.tnteco.common.config.locale.Translator;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.common.data.constant.ErrorResponseBase;

import java.util.List;

@Data
@Accessors(chain = true)
public class DfListResponse<T> {

    private String code;
    private String message;
    private ResultResponse<T> result;

    public DfListResponse(String code, Pageable pageable, List<T> result) {
        this.setCode(code);
        this.result = new ResultResponse<T>()
                .setCurrentPage(pageable.getPage())
                .setPerPage(pageable.getPageSize())
                .setTotal(pageable.getTotal())
                .setData(result);
    }

    public DfListResponse(Pageable pageable, List<T> result) {
        this.setCode(ErrorResponseBase.SUCCESS.getCode());
        this.result = new ResultResponse<T>()
                .setCurrentPage(pageable.getPage())
                .setPerPage(pageable.getPageSize())
                .setTotal(pageable.getTotal())
                .setData(result);
    }

    public DfListResponse(SearchRequest searchRequest, List<T> result) {
        this.setCode(ErrorResponseBase.SUCCESS.getCode());
        this.result = new ResultResponse<T>()
                .setCurrentPage(searchRequest.getPage())
                .setPerPage(searchRequest.getPageSize())
                .setTotal(searchRequest.getTotal() )
                .setData(result);
    }

    public void setCode(String code) {
        this.code = code;
        this.message = Translator.toLocale(code);
    }

    public void setCode(String code, String[] params) {
        this.code = code;
        this.message = Translator.toLocale(code, params);
    }
}
