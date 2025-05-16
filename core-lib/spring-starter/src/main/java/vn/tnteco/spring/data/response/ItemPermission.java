package vn.tnteco.spring.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ItemPermission {
    private String action;
    private Boolean isPermit;
}
