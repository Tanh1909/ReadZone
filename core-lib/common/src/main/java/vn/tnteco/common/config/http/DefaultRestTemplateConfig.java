package vn.tnteco.common.config.http;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import vn.tnteco.common.config.properties.DefaultHttpClientProperties;
import vn.tnteco.common.config.properties.DefaultHttpClientProperties.ClientType;

import javax.net.ssl.SSLContext;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Log4j2
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = {"http-client.default.enable"},
        havingValue = "true"
)
public class DefaultRestTemplateConfig {

    private final DefaultHttpClientProperties properties;

    @Primary
    @Bean("defaultRestTemplate")
    public RestTemplate defaultRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        restTemplate.setRequestFactory(this.defaultClientHttpRequestFactory());
        return restTemplate;
    }

    @Bean("defaultClientHttpRequestFactory")
    public ClientHttpRequestFactory defaultClientHttpRequestFactory() {
        if (ClientType.OK_HTTP.equals(properties.getType())) {
            log.info("init okHttp client factory");
            return this.okHttp3ClientHttpRequestFactory();
        } else if (ClientType.APACHE_HTTP_CLIENT.equals(properties.getType())) {
            log.info("init apache http client");
            return this.httpComponentsClientHttpRequestFactory();
        } else {
            throw new UnsupportedOperationException("Unsupported ClientType !!!");
        }
    }

    private ClientHttpRequestFactory okHttp3ClientHttpRequestFactory() {
        DefaultHttpClientProperties.Connection connection = properties.getConnection();
        DefaultHttpClientProperties.ConnectionPool connectionPool = properties.getConnectionPool();

        ConnectionPool connectionPoolConfig = new ConnectionPool(connectionPool.getMaxIdle(), connection.getKeepAlive(), SECONDS);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(connection.getConnectTimeout(), SECONDS)
                .readTimeout(connection.getResponseTimeout(), SECONDS)
                .writeTimeout(connection.getResponseTimeout(), SECONDS)
                .connectionPool(connectionPoolConfig)
                .build();
        return new OkHttp3ClientHttpRequestFactory(okHttpClient);
    }

    private HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
        DefaultHttpClientProperties.Connection connection = properties.getConnection();

        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setConnectionManager(this.defaultHttpPoolingConnectionManager())
                .setConnectionManagerShared(true)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(connection.getConnectTimeout(), TimeUnit.SECONDS)
                        .setResponseTimeout(connection.getResponseTimeout(), TimeUnit.SECONDS)
                        .setDefaultKeepAlive(connection.getKeepAlive(), TimeUnit.SECONDS)
                        .setConnectionRequestTimeout(10, TimeUnit.SECONDS)
                        .setCookieSpec(StandardCookieSpec.STRICT)
                        .build())
                .build();
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    private PoolingHttpClientConnectionManager defaultHttpPoolingConnectionManager() {
        PoolingHttpClientConnectionManager poolingConnection = new PoolingHttpClientConnectionManager(this.getSocketFactoryRegistry());
        poolingConnection.setMaxTotal(properties.getConnectionPool().getMaxTotal());
        poolingConnection.setDefaultMaxPerRoute(properties.getConnectionPool().getDefaultMaxPerRoute());
        return poolingConnection;
    }

    @SneakyThrows
    private Registry<ConnectionSocketFactory> getSocketFactoryRegistry() {
        SSLContextBuilder builder = SSLContexts.custom();
        builder.loadTrustMaterial(null, (chain, authType) -> true);
        SSLContext sslContext = builder.build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                (s, sslSession) -> s.equalsIgnoreCase(sslSession.getPeerHost()));
        return RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
    }

}
