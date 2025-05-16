package vn.tnteco.common.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthResponse {
    private String token;
    private Long expiredAt;
}
