package vn.tnteco.spring.data.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse {

    private Long createdAt;

    private Integer createdBy;

    private UserInfo creator;

    private Integer updatedBy;

    private Long updatedAt;

    private UserInfo updater;

}
