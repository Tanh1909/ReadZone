package vn.tnteco.common.core.http;

import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import vn.tnteco.common.core.exception.HttpClientTimeout;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.common.core.template.RxTemplate;

import java.util.Optional;

import static org.springframework.http.HttpMethod.*;

@Log4j2
@AllArgsConstructor
public abstract class RestTemplateServiceBase {

    private final RestTemplate template;

//
// =============================================== Nonblocking ===================================================
//

    public Single<Optional<String>> get(String url, HttpHeaders headers) {
        return RxTemplate.rxSchedulerNewThread(() -> {
            ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<>() {
            };
            return Optional.ofNullable(this.executeRequest(url, GET, headers, responseType).getBody());
        });
    }

    public <R> Single<Optional<R>> get(String url, HttpHeaders headers, ParameterizedTypeReference<R> responseType) {
        return RxTemplate.rxSchedulerNewThread(() -> {
            R response = this.executeRequest(url, GET, headers, responseType).getBody();
            return Optional.ofNullable(response);
        });
    }

    public <R> Single<ResponseEntity<R>> getEntity(String url, HttpHeaders headers, ParameterizedTypeReference<R> responseType) {
        return RxTemplate.rxSchedulerNewThread(() -> this.executeRequest(url, GET, headers, responseType));
    }

    public <B> Single<Optional<String>> post(String url, B body, HttpHeaders headers) {
        return RxTemplate.rxSchedulerNewThread(() -> {
            ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<>() {
            };
            return Optional.ofNullable(this.executeRequest(url, POST, body, headers, responseType).getBody());
        });
    }

    public <B, R> Single<Optional<R>> post(String url, B body, HttpHeaders headers,
                                           ParameterizedTypeReference<R> responseType) {
        return RxTemplate.rxSchedulerNewThread(() -> {
            R response = this.executeRequest(url, POST, body, headers, responseType).getBody();
            return Optional.ofNullable(response);
        });
    }

    public <B, R> Single<ResponseEntity<R>> postEntity(String url, B body, HttpHeaders headers,
                                                       ParameterizedTypeReference<R> responseType) {
        return RxTemplate.rxSchedulerNewThread(() -> this.executeRequest(url, POST, body, headers, responseType));
    }

//
// =============================================== Blocking ===================================================
//

    public String getBlocking(String url, HttpHeaders headers) {
        ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<>() {
        };
        return this.executeRequest(url, GET, headers, responseType).getBody();
    }

    public <R> R getBlocking(String url, HttpHeaders headers, ParameterizedTypeReference<R> responseType) {
        return this.executeRequest(url, GET, headers, responseType).getBody();
    }

    public <R> ResponseEntity<R> getEntityBlocking(String url, HttpHeaders headers,
                                                   ParameterizedTypeReference<R> responseType) {
        return this.executeRequest(url, GET, headers, responseType);
    }

    public <B> String postBlocking(String url, B body, HttpHeaders headers) {
        ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<>() {
        };
        return this.executeRequest(url, POST, body, headers, responseType).getBody();
    }

    public <B, R> R postBlocking(String url, B body, HttpHeaders headers, ParameterizedTypeReference<R> responseType) {
        return this.executeRequest(url, POST, body, headers, responseType).getBody();
    }

    public <B, R> ResponseEntity<R> postEntityBlocking(String url, B body, HttpHeaders headers, ParameterizedTypeReference<R> responseType) {
        return this.executeRequest(url, POST, body, headers, responseType);
    }

    private <R> ResponseEntity<R> executeRequest(String url, HttpMethod method, HttpHeaders headers,
                                                    ParameterizedTypeReference<R> responseType) {
        return this.executeRequest(url, method, null, headers, responseType);
    }

    private <B, R> ResponseEntity<R> executeRequest(String url, HttpMethod method, B body,
                                                    HttpHeaders headers, ParameterizedTypeReference<R> responseType) {
        HttpEntity<?> httpEntity;
        if (POST.equals(method) || PUT.equals(method) || PATCH.equals(method) || DELETE.equals(method)) {
            httpEntity = new HttpEntity<>(body, headers);
        } else {
            httpEntity = new HttpEntity<>(headers);
        }
        try {
            log.debug("Call api [{}]-[{}] \n\tBody: {} \n\tHeaders: {}", method, url, Json.parserJsonLog(body), headers.toString());
            return template.exchange(url, method, httpEntity, responseType);
        } catch (ResourceAccessException e) {
            log.error("Call api timeout [{}]-[{}]", method, url);
            throw new HttpClientTimeout(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Call api error [{}]-[{}]: {}", method, url, e.getMessage());
            throw e;
        }
    }

}