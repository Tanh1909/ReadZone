package vn.tnteco.spring.data.response;

import lombok.Data;
import lombok.experimental.Accessors;
import vn.tnteco.common.annotation.QuickSearchField;
import vn.tnteco.common.core.model.query.SearchOption;

@Data
@Accessors(chain = true)
public class BasicResponse {

    private Integer id;

    @QuickSearchField(columnName = "code", searchOption = SearchOption.LIKE_IGNORE_CASE)
    private String code;

    @QuickSearchField(columnName = "name", searchOption = SearchOption.LIKE_IGNORE_CASE)
    private String name;
}
