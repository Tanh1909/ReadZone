package com.example.app.vnpay.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfigProperties {

    private String tmnCode;

    private String hashSecret;

    private String paymentUrl;

    private String version;

    private String command;

    private String orderType;

    private String returnUrl;

    private String currentCode;

    private String locale;

    private Long expirationDate;

}
