package vn.tnteco.spring.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.tnteco.common.config.properties.SecurityProperties;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.exception.BusinessException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.common.data.constant.HeaderConstant;
import vn.tnteco.common.service.JwtService;
import vn.tnteco.spring.model.DfResponse;

@Log4j2
@Order(3)
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final SecurityProperties securityProperties;

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            if (securityProperties.getApiWhitelist().stream().anyMatch(request.getRequestURI()::contains)) {
                log.debug("Api public: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            String serverToken = request.getHeader(HeaderConstant.SERVER_TOKEN);
            String userToken = request.getHeader(HeaderConstant.AUTHORIZATION);
            String apiKey = request.getHeader(HeaderConstant.API_KEY);
            if (StringUtils.isNotEmpty(apiKey)) {
                if (!apiKey.equals(securityProperties.getApiKey())) {
                    throw new ApiException(ErrorResponseBase.UNAUTHORIZED);
                }
                SimpleSecurityUser simpleSecurityUser = getSimpleSecurityUserFromHeader(request);
                SecurityContext.setSimpleSecurityUser(simpleSecurityUser);
            } else if (StringUtils.isNotEmpty(serverToken)) {
                if (!serverToken.equals(securityProperties.getServerKey())) {
                    throw new ApiException(ErrorResponseBase.UNAUTHORIZED);
                }
                SecurityContext.setSimpleSecurityUser(SimpleSecurityUser.initSystemAdmin());
            } else if (StringUtils.isNotEmpty(userToken) && userToken.startsWith("Bearer")) {
                boolean showLog = (request.getHeader(HeaderConstant.SHOW_LOG) != null) &&
                        Boolean.TRUE.equals(BooleanUtils.toBoolean(request.getHeader(HeaderConstant.SHOW_LOG)));
                String accessToken = userToken.substring(7);
                SimpleSecurityUser simpleSecurityUser = this.extractAuthentication(accessToken, showLog);
                SecurityContext.setAccessToken(accessToken);
                SecurityContext.setSimpleSecurityUser(simpleSecurityUser);
            }
            SimpleSecurityUser simpleSecurityUser = SecurityContext.getSimpleSecurityUser();
            if (simpleSecurityUser == null) {
                log.debug("simple security user is null");
                throw new ApiException(ErrorResponseBase.UNAUTHORIZED);
            }
            filterChain.doFilter(request, response);
        } catch (ApiException e) {
            log.error("AuthenticationFilter error", e);
            DfResponse.httpServletResponse(response, e.getResponseStatusCode());
        } catch (Exception e) {
            log.error("AuthenticationFilter error", e);
            DfResponse.httpServletResponse(response, ErrorResponseBase.UNAUTHORIZED);
        } finally {
            log.info("SecurityContext clear context");
            SecurityContext.clearContext();
        }
    }

    private SimpleSecurityUser getSimpleSecurityUserFromHeader(HttpServletRequest request) {
        int userId = NumberUtils.toInt(request.getHeader(HeaderConstant.USER_ID), 0);
        int orgId = NumberUtils.toInt(request.getHeader(HeaderConstant.ORG_ID), 0);
        return new SimpleSecurityUser()
                .setId(userId)
                .setOrgId(orgId);

    }

    private SimpleSecurityUser extractAuthentication(String token, boolean showLog) {
        try {
            SimpleSecurityUser user = jwtService.extractSecurityUser(token);
            if (user == null) {
                throw new BusinessException("SimpleSecurityUser is null");
            }
            user.setShowLog(showLog);
            return user;
        } catch (Exception e) {
            log.error("extractAuthentication ERROR: {}", e.getMessage(), e);
            throw new ApiException(ErrorResponseBase.UNAUTHORIZED);
        }
    }

}


