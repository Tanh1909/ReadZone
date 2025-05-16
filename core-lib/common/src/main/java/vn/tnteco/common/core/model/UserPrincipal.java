package vn.tnteco.common.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserPrincipal {

    private SimpleSecurityUser userInfo;
    private String ip;
    private String uri;
    private String method;
    private String clientId;
    private String agentInfo;
    private Boolean showLog;
    private Object clientInfo;

    public SimpleSecurityUser getUserInfo() {
        return userInfo == null ? new SimpleSecurityUser() : userInfo;
    }

}
