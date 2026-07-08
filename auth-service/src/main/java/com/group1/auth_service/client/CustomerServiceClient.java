package com.group1.auth_service.client;

import com.group1.auth_service.dto.request.CreateCustomerFromRegistrationRequest;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gọi customer-service qua Feign; map lỗi sang {@link ResponseStatusException} cho luồng đăng ký.
 */
@Service
public class CustomerServiceClient {

    private final CustomerServiceFeignClient feign;

    public CustomerServiceClient(CustomerServiceFeignClient feign) {
        this.feign = feign;
    }

    /**
     * Tạo bản ghi khách hàng sau đăng ký. Gọi bằng access token vừa cấp (cùng secret JWT với customer-service).
     */
    public void createCustomerForRegisteredUser(String accessToken, Long userId) {
        try {
            feign.createCustomerFromRegistration(
                    "Bearer " + accessToken,
                    new CreateCustomerFromRegistrationRequest(userId)
            );
        } catch (FeignException e) {
            String detail = e.contentUTF8();
            if (detail != null && detail.length() > 200) {
                detail = detail.substring(0, 200);
            }
            int status = e.status();
            if (status < 0) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Dịch vụ khách hàng không phản hồi"
                );
            }
            HttpStatus mapped = status >= 400 && status < 500
                    ? HttpStatus.BAD_REQUEST
                    : HttpStatus.BAD_GATEWAY;
            throw new ResponseStatusException(
                    mapped,
                    "Không tạo được hồ sơ khách hàng: HTTP " + status
                            + (detail != null && !detail.isBlank() ? " " + detail : "")
            );
        }
    }
}
