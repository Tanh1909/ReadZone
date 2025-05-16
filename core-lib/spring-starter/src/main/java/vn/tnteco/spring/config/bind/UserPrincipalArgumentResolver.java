package vn.tnteco.spring.config.bind;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.core.model.UserPrincipal;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.spring.config.bind.annotation.UserPrincipalRequest;

public class UserPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(UserPrincipalRequest.class) != null;
    }

    @Override
    public UserPrincipal resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) {
        UserPrincipalRequest userPrincipalRequestAnnotation = methodParameter.getParameterAnnotation(UserPrincipalRequest.class);

        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setMethod(request.getMethod());
        userPrincipal.setUri(request.getRequestURI());
        if (request.getHeader("show_log") != null)
            userPrincipal.setShowLog(request.getHeader("show_log").equalsIgnoreCase("true"));
        if (request.getHeader("User-Agent") != null)
            userPrincipal.setAgentInfo(request.getHeader("User-Agent"));

        SimpleSecurityUser simpleSecurityUser = SecurityContext.getSimpleSecurityUser();
        if (userPrincipalRequestAnnotation != null && userPrincipalRequestAnnotation.userInfoRequired()
                && simpleSecurityUser == null) {
            throw new ApiException(ErrorResponseBase.UNAUTHORIZED);
        }
        userPrincipal.setUserInfo(simpleSecurityUser);
        return userPrincipal;
    }
}
