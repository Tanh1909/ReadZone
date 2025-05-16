package vn.tnteco.spring.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.tnteco.common.config.properties.SecurityProperties;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.common.data.constant.HeaderConstant;
import vn.tnteco.spring.config.authentication.ICheckPermissionService;
import vn.tnteco.spring.model.DfResponse;

@Log4j2
@Order(4)
@Component
@RequiredArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    @Setter(onMethod_ = {@Autowired(required = false)})
    private ICheckPermissionService checkPermissionService;

    @Setter(onMethod_ = {@Autowired})
    private SecurityProperties securityProperties;


    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            if (securityProperties.getApiWhitelist().stream().anyMatch(request.getRequestURI()::contains)) {
                log.debug("Api public: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            if (StringUtils.isNotEmpty(request.getHeader(HeaderConstant.API_KEY))) {
                filterChain.doFilter(request, response);
                return;
            }

            boolean isPermission = checkPermissionService.isPermission(request, response);
            if (!isPermission) {
                log.debug("Api: {} does not have permission", request.getRequestURI());
                throw new ApiException(ErrorResponseBase.NOT_PERMISSION);
            }

            filterChain.doFilter(request, response);
        } catch (ApiException e) {
            log.error("AuthorizationFilter error", e);
            DfResponse.httpServletResponse(response, e.getResponseStatusCode());
        } catch (Exception e) {
            log.error("AuthorizationFilter error", e);
            DfResponse.httpServletResponse(response, ErrorResponseBase.INTERNAL_GENERAL_SERVER_ERROR);
        }
    }
}


