package com.group1.auth_service.client;

import com.group1.auth_service.dto.request.CreateCustomerFromRegistrationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "customer-service", url = "${app.customer-service.base-url:http://localhost:8082}")
public interface CustomerServiceFeignClient {

    @PostMapping(value = "/api/customers/from-registration", consumes = "application/json")
    void createCustomerFromRegistration(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CreateCustomerFromRegistrationRequest body);
}
