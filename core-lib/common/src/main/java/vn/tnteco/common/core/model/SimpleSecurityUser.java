package vn.tnteco.common.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleSecurityUser {

    private Integer id;
    private String username;
    private Integer orgId;
    private List<String> roles;
    private boolean isShowLog = false;
    private Long issuedAt;

    public boolean isSystemAdmin() {
        return "system_admin".equals(username);
    }

    public static SimpleSecurityUser initSystemAdmin() {
        return new SimpleSecurityUser()
                .setId(0)
                .setUsername("system_admin");
    }

    public Integer getOrgId() {
        return isSystemAdmin() ? Integer.valueOf(0) : orgId;
    }
}