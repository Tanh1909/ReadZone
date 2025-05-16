package vn.tnteco.common.core.model.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import vn.tnteco.common.core.model.query.SearchOption;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Search {

    private String name;

    private SearchOption operation = SearchOption.LIKE;

}
