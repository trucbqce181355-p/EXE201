package com.group1.customer_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class VNPayConfig {
    @Value("${vnpay.tmnCode:YOUR_TMN_CODE}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret:YOUR_HASH_SECRET}")
    private String vnp_HashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl:http://localhost:5173/payment/vnpay/return}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.version:2.1.0}")
    private String vnp_Version;

    @Value("${vnpay.command:pay}")
    private String vnp_Command;
}
