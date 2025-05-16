package vn.tnteco.spring.data.base;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserInfo {

    private Integer id;

    private String username;

}
