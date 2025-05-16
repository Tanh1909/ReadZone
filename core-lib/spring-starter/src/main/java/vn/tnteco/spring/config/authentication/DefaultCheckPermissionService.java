package vn.tnteco.spring.config.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import vn.tnteco.common.config.properties.SecurityProperties;
import vn.tnteco.common.core.http.IHttpService;

@Log4j2
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "ICheckPermissionService")
public class DefaultCheckPermissionService implements ICheckPermissionService {

    private final IHttpService httpService;

    private final SecurityProperties securityProperties;

    @Override
    public boolean isPermission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return true;
    }

}
