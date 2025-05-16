package vn.tnteco.common.core.http;

import io.reactivex.rxjava3.core.Single;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IRxHttpService {

    Single<Optional<String>> get(String url, HttpHeaders headers);

    <R> Single<Optional<R>> get(String url, HttpHeaders headers, ParameterizedTypeReference<R> responseType);

    <R> Single<ResponseEntity<R>> getEntity(String url, HttpHeaders headers, ParameterizedTypeReference<R> responseType);

    <B> Single<Optional<String>> post(String url, B body, HttpHeaders headers);

    <B, R> Single<Optional<R>> post(String url, B body, HttpHeaders headers, ParameterizedTypeReference<R> responseType);

    <B, R> Single<ResponseEntity<R>> postEntity(String url, B body, HttpHeaders headers, ParameterizedTypeReference<R> responseType);

}