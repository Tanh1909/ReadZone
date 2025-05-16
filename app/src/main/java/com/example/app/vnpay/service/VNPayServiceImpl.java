package com.example.app.vnpay.service;

import com.example.app.vnpay.config.properties.VNPayConfigProperties;
import com.example.app.vnpay.data.request.VNPayParamsRequest;
import com.example.app.vnpay.utils.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static com.example.app.vnpay.data.constant.VNPayParamsConstant.*;

@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnBean(VNPayConfigProperties.class)
public class VNPayServiceImpl implements IVNPayService {

    private static final String VNPAY_DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    private final VNPayConfigProperties properties;

    @Override
    public String createPaymentUrl(VNPayParamsRequest vNPayParamsRequest) {
        Map<String, String> params = new HashMap<>(getMapConfig(properties));
        params.put(VNP_AMOUNT, vNPayParamsRequest.getAmount());
        params.put(VNP_IP_ADDR, vNPayParamsRequest.getIpAddr());
        params.put(VNP_ORDER_INFO, vNPayParamsRequest.getOrderInfo());
        params.put(VNP_TXN_REF, vNPayParamsRequest.getTxnRef());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(VNPAY_DATE_TIME_FORMAT);
        LocalDateTime createDate = LocalDateTime.now();
        LocalDateTime expireDate = createDate.plusSeconds(properties.getExpirationDate());
        params.put(VNP_CREATE_DATE, formatter.format(createDate));
        params.put(VNP_EXPIRE_DATE, formatter.format(expireDate));

        String paymentUrl = Objects.requireNonNull(properties.getPaymentUrl());
        String queryIgnoreSecureHash = buildQueryIgnoreSecureHash(params);
        String secureHash = generateSecureHash(queryIgnoreSecureHash);
        return paymentUrl
                + "?" + queryIgnoreSecureHash
                + "&" + VNP_SECURE_HASH + "=" + secureHash;
    }

    @Override
    public boolean validateParamsCallBack(Map<String, String> callBackParams) {
        String secureHash = callBackParams.remove(VNP_SECURE_HASH);
        if (StringUtils.isEmpty(secureHash)) {
            log.error("secure hash is empty");
            return false;
        }
        String queryIgnoreSecureHash = buildQueryIgnoreSecureHash(callBackParams);
        if (!secureHash.equals(generateSecureHash(queryIgnoreSecureHash))) {
            log.error("secure hash does not match");
            return false;
        }
        return true;
    }

    private String buildQueryIgnoreSecureHash(Map<String, String> params) {
        StringJoiner stringJoiner = new StringJoiner("&");
        params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getKey() != null)
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String key = entry.getKey();
                    String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    stringJoiner.add(key + "=" + value);
                });
        return stringJoiner.toString();
    }

    private Map<String, String> getMapConfig(VNPayConfigProperties properties) {
        Map<String, String> map = new HashMap<>();
        map.put(VNP_VERSION, properties.getVersion());
        map.put(VNP_COMMAND, properties.getCommand());
        map.put(VNP_TMN_CODE, properties.getTmnCode());
        map.put(VNP_CURRENT_CODE, properties.getCurrentCode());
        map.put(VNP_ORDER_TYPE, properties.getOrderType());
        map.put(VNP_LOCALE, properties.getLocale());
        map.put(VNP_RETURN_URL, properties.getReturnUrl());
        return map;
    }

    private String generateSecureHash(String query) {
        return HashUtils.hmacSHA512(properties.getHashSecret(), query);
    }

    @Override
    public Long getExpirationDate() {
        return properties.getExpirationDate();
    }
}
