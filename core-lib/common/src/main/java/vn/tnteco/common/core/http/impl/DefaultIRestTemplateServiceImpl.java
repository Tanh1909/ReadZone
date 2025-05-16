package vn.tnteco.common.core.http.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.tnteco.common.core.http.IHttpService;
import vn.tnteco.common.core.http.IRxHttpService;
import vn.tnteco.common.core.http.RestTemplateServiceBase;

@Log4j2
@Primary
@Component
@ConditionalOnProperty(
        value = {"http-client.default.enable"},
        havingValue = "true"
)
public class DefaultIRestTemplateServiceImpl extends RestTemplateServiceBase implements IHttpService, IRxHttpService {

    public DefaultIRestTemplateServiceImpl(@Qualifier("defaultRestTemplate") RestTemplate template) {
        super(template);
    }

}