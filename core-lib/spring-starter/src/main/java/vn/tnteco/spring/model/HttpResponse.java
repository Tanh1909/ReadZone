package vn.tnteco.spring.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Data
@Accessors(chain = true)
public class HttpResponse<T> {

    private HttpStatus httpStatus;

    private T response;

}
