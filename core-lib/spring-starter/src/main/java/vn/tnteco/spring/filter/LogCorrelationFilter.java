package vn.tnteco.spring.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.tnteco.common.config.properties.ApplicationProperties;
import vn.tnteco.common.data.constant.TrackingContextEnum;
import vn.tnteco.common.utils.StringUtils;

@Log4j2
@Order(1)
@Configuration
@RequiredArgsConstructor
public class LogCorrelationFilter extends OncePerRequestFilter {

    private final ApplicationProperties appProperties;

    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        long time = System.currentTimeMillis();
        this.generateCorrelationIdIfNotExists(request.getHeader(TrackingContextEnum.CORRELATION_ID.getKey()));
        response.setHeader(TrackingContextEnum.CORRELATION_ID.getKey(), ThreadContext.get(TrackingContextEnum.CORRELATION_ID.getKey()));
        filterChain.doFilter(request, response);
        log.info("{}: {} ms ", request.getRequestURI(), System.currentTimeMillis() - time);
        ThreadContext.clearAll();
    }


    private void generateCorrelationIdIfNotExists(String xCorrelationId) {
        String correlationId = org.apache.commons.lang3.StringUtils.isEmpty(xCorrelationId)
                ? StringUtils.genCorrelationId(this.appProperties.getApplicationShortName()) : xCorrelationId;
        ThreadContext.put(TrackingContextEnum.CORRELATION_ID.getKey(), correlationId);
    }
}
