package vn.tnteco.spring.config.authentication;

import vn.tnteco.common.core.model.SimpleSecurityUser;

public interface IJwtParserService {
    SimpleSecurityUser parserUser(String token);
}
