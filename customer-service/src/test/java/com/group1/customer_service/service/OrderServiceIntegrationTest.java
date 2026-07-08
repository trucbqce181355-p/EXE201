package com.group1.customer_service.service;

import com.group1.customer_service.dto.request.CreateOrderItemRequest;
import com.group1.customer_service.dto.request.CreateOrderRequest;
import com.group1.customer_service.dto.response.OrderDetailResponse;
import com.group1.customer_service.entity.Customer;
import com.group1.customer_service.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testCreateOrder() {
        Customer customer = customerRepository.findAll().stream().findFirst().orElseGet(() -> {
            Customer c = new Customer();
            c.setCustomerCode("TEST-CUST");
            c.setUserId(1L);
            return customerRepository.save(c);
        });

        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentMethod("CASH");
        request.setAddress("123 Test Street");
        request.setReceiverName("Test Receiver");
        request.setPhone("123456789");
        request.setDiscountAmount(BigDecimal.ZERO);

        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProduct("Coffee");
        item.setQuantity(1);
        item.setPrice(BigDecimal.valueOf(50000));
        request.setItems(List.of(item));

        try {
            OrderDetailResponse response = orderService.createOrder(customer.getCustomerId(), request);
            System.out.println("TEST ORDER CREATED SUCCESSFULLY: " + response.getOrderId());
        } catch (Exception e) {
            System.err.println("TEST ORDER FAILED WITH EXCEPTION:");
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetOrders() {
        Customer customer = customerRepository.findAll().stream().findFirst().orElseGet(() -> {
            Customer c = new Customer();
            c.setCustomerCode("TEST-CUST-GET");
            c.setUserId(2L);
            return customerRepository.save(c);
        });

        try {
            var response = orderService.getOrders(customer.getCustomerId(), 1, 10, null, null, null);
            System.out.println("TEST GET ORDERS COMPLETED SUCCESSFULLY. Count: " + response.getData().size());
        } catch (Exception e) {
            System.err.println("TEST GET ORDERS FAILED WITH EXCEPTION:");
            e.printStackTrace();
            throw e;
        }
    }
}
