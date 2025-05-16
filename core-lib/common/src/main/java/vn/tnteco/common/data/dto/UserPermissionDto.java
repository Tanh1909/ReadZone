package vn.tnteco.common.data.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserPermissionDto {
    private Integer userId;
    private List<PermissionDto> permissions;
}
