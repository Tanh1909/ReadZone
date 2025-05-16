package vn.tnteco.common.data.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PermissionDto {
    private Integer id;
    private Integer permissionId;
    private String uri;
    private String method;
    private String code;
    private String action;
    private Integer menuId;
}
