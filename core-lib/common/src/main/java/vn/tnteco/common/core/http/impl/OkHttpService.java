package vn.tnteco.common.core.http.impl;

import io.reactivex.rxjava3.core.Single;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import vn.tnteco.common.core.json.JsonObject;
import vn.tnteco.common.core.template.RxTemplate;

import java.io.IOException;

@Log4j2
@Component
public class OkHttpService {
    private final OkHttpClient client;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public OkHttpService(@Qualifier("getClientIgnoreSSL") OkHttpClient getClientIgnoreSSL) {
        this.client = getClientIgnoreSSL;
    }

    @SneakyThrows
    public <P> Single<P> postJson(String url, String body, Class<P> rsClass) {
        return RxTemplate.rxSchedulerNewThread(() -> {
            RequestBody requestBody = RequestBody.create(JSON, body);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return new JsonObject(response.body().string()).mapTo(rsClass);
            } catch (IOException e) {
                log.error("[POST REQUEST ERROR: ]", e);
                return null;
            }
        });
    }

}
