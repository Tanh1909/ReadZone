package vn.tnteco.common.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.data.response.AuthResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static vn.tnteco.common.config.jackson.JsonMapper.getObjectMapper;
import static vn.tnteco.common.core.json.JsonObject.mapFrom;

@Log4j2
@Service
public class JwtService {

    @Value("${app.security.jwt.secret-key:SmartIndustry@2024}")
    private String secretKey;

    @Value("${app.security.jwt.expired-in:100000}")
    private Long expiresIn;

    public SimpleSecurityUser extractSecurityUser(String token) {
        if (token == null || StringUtils.isBlank(token)) return null;
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            String userString = (String) body.get("user");
            return getObjectMapper().readValue(userString, SimpleSecurityUser.class);
        } catch (Exception e) {
            log.error("extractSecurityUser ERROR: {}", e.getMessage(), e);
            return null;
        }
    }

    public String generateJwt(SimpleSecurityUser securityUser) {
        Map<String, Object> claims = new HashMap<>();
        putAuthentication(claims, securityUser);
        return Jwts.builder()
                .setSubject(String.valueOf(securityUser.getId()))
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiresIn))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public AuthResponse generateAuthResponse(SimpleSecurityUser securityUser) {
        Map<String, Object> claims = new HashMap<>();
        putAuthentication(claims, securityUser);
        AuthResponse response = new AuthResponse()
                .setExpiredAt(System.currentTimeMillis() + expiresIn);
        String token = Jwts.builder()
                .addClaims(claims)
                .setSubject(String.valueOf(securityUser.getId()))
                .setExpiration(new Date(response.getExpiredAt()))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return response.setToken(token);
    }

    private void putAuthentication(Map<String, Object> claims, SimpleSecurityUser securityUser) {
        claims.put("user", mapFrom(securityUser).encode());
    }
}
