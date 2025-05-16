package com.example.app.service.payment.factory;

import com.example.app.data.constant.CacheConstant;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.constant.PaymentMethod;
import com.example.app.data.request.PaymentRequest;
import com.example.app.data.response.PaymentResponse;
import com.example.app.data.tables.pojos.Order;
import com.example.app.repository.order.IRxOrderRepository;
import com.example.app.service.kafka.IPushKafkaService;
import io.reactivex.rxjava3.core.Single;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import vn.tnteco.cache.config.serializer.RedisSerializer;
import vn.tnteco.cache.store.external.IExternalCacheStore;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.UserPrincipal;

import java.time.Duration;

import static com.example.app.data.constant.AppErrorResponse.*;
import static vn.tnteco.common.data.constant.ErrorResponseBase.UNAUTHORIZED;

@Log4j2
public abstract class PaymentAbstract {

    @Setter(onMethod_ = {@Autowired})
    protected IRxOrderRepository orderRepository;

    @Setter(onMethod_ = {@Autowired})
    private StringRedisTemplate redisTemplate;

    @Setter(onMethod_ = {@Autowired})
    private RedisSerializer redisSerializer;

    @Setter(onMethod_ = {@Autowired})
    protected IExternalCacheStore externalCacheStore;

    @Setter(onMethod_ = {@Autowired})
    protected IPushKafkaService pushKafkaService;

    public abstract PaymentMethod getPaymentMethod();

    public Single<PaymentResponse> pay(PaymentRequest paymentRequest) {
        Integer orderId = paymentRequest.getOrderId();
        String lockPaymentKey = CacheConstant.getLockPaymentKey(orderId);
        return orderRepository.getById(orderId)
                .flatMap(orderOptional -> {
                    try {
                        boolean isLock = putObjectIfAbsent(lockPaymentKey, "lock", 30);
                        if (!isLock) {
                            log.warn("Payment for order {} is being processed by another request", orderId);
                            return Single.error(new ApiException(PAYMENT_REQUEST_IS_PROCESSING));
                        }
                        Order order = orderOptional.orElseThrow(() -> new ApiException(ORDER_NOT_FOUND));
                        if (!OrderStatusEnum.PENDING.value().equals(order.getStatus())
                                && !OrderStatusEnum.PAYMENT_PROCESSING.value().equals(order.getStatus())
                        ) {
                            return Single.error(new ApiException(ORDER_HAS_BEEN_PAID));
                        }
                        return handlePayment(order);
                    } catch (Exception e) {
                        log.error("error when payment for order {}", orderId, e);
                        return Single.error(e);
                    } finally {
                        redisTemplate.delete(lockPaymentKey);
                    }
                });
    }

    protected abstract Single<PaymentResponse> handlePayment(Order order);


    protected UserPrincipal getUserPrincipal() {
        UserPrincipal userPrincipal = SecurityContext.getUserPrincipal();
        if (userPrincipal == null) {
            throw new ApiException(UNAUTHORIZED);
        }
        return userPrincipal;
    }

    protected <T> boolean putObjectIfAbsent(String key, T value, long expireSeconds) {
        log.debug("RedisCache put: key = {}, value = {}, expire = {}", key, value != null ? value.toString() : null, expireSeconds);
        ValueOperations<String, String> ops = this.redisTemplate.opsForValue();
        return Boolean.TRUE.equals(ops.setIfAbsent(key, redisSerializer.serializer(value), Duration.ofSeconds(expireSeconds)));
    }


}
