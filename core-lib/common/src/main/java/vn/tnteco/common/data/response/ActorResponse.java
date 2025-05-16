package vn.tnteco.common.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ActorResponse {
    private Integer id;
    private String fullName;
}
