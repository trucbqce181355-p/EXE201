package com.group1.customer_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.group1.customer_service.dto.response.CustomerLookupResponse;
import com.group1.customer_service.dto.response.CustomerProfileResponse;
import com.group1.customer_service.exception.GlobalExceptionHandler;
import com.group1.customer_service.repository.CustomerRepository;
import com.group1.customer_service.security.JwtUtil;
import com.group1.customer_service.service.CustomerService;
import com.group1.customer_service.service.LoyaltyService;
import com.group1.customer_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OrderService orderService;

    @Mock
    private LoyaltyService loyaltyService;

    @Mock
    private CustomerRepository customerRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        CustomerController customerController = new CustomerController(
                customerService,
                jwtUtil,
                orderService,
                loyaltyService,
                customerRepository
        );

        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createCustomerFromRegistrationReturnsCreatedEnvelope() throws Exception {
        CustomerLookupResponse response = new CustomerLookupResponse(1L, "CUS-U7", 7L);
        when(customerService.createCustomerFromRegistration(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/customers/from-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", 7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer profile created successfully."))
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.userId").value(7));
    }

    @Test
    void getCustomerProfileReturnsProfileEnvelope() throws Exception {
        CustomerProfileResponse response = CustomerProfileResponse.builder()
                .customerId(1L)
                .customerCode("CUS-20260301-0001")
                .fullName("Story Customer")
                .email("story.customer@example.com")
                .status("ACTIVE")
                .createdAt(LocalDateTime.of(2026, 3, 11, 10, 0))
                .build();
        when(customerService.getCustomerProfile(eq(1L), eq("loyalty"), any(), eq("Bearer test-token")))
                .thenReturn(response);

        mockMvc.perform(get("/api/customers/1")
                        .param("include", "loyalty")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer profile loaded successfully."))
                .andExpect(jsonPath("$.data.customer_id").value(1))
                .andExpect(jsonPath("$.data.customer_code").value("CUS-20260301-0001"))
                .andExpect(jsonPath("$.data.full_name").value("Story Customer"));
    }

    @Test
    void getOrdersRejectsMissingBearerHeader() throws Exception {
        mockMvc.perform(get("/api/customers/1/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("\"Unauthorized\""));

        verifyNoInteractions(orderService);
    }
}
