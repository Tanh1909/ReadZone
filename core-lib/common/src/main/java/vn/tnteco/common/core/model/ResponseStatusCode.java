package vn.tnteco.common.core.model;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseStatusCode {

    private String code;

    private HttpStatus httpStatus;

}
