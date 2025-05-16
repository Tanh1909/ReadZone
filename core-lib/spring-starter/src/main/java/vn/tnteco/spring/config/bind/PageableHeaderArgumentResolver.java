package vn.tnteco.spring.config.bind;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import vn.tnteco.common.core.json.JsonObject;
import vn.tnteco.common.core.model.paging.Pageable;
import vn.tnteco.common.core.model.paging.PageableParamParser;
import vn.tnteco.spring.config.bind.annotation.PageableRequest;

import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class PageableHeaderArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(PageableRequest.class) != null;
    }

    @Override
    public Pageable resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) {

        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        try {
            Map<String, Object> data = request.getParameterMap().entrySet()
                    .stream()
                    .filter(stringEntry -> stringEntry.getValue().length == 1)
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> entry.getValue()[0]));
            Pageable pageable = (Pageable) new JsonObject(data).mapTo(methodParameter.getParameterType());
            return PageableParamParser.parser(request.getParameterMap(), pageable == null ? new Pageable() : pageable);
        } catch (Exception e) {
            log.error("fail to resolve argument: ", e);
            return PageableParamParser.parser(request.getParameterMap());
        }
    }
}
