package com.group1.customer_service.service;

import com.group1.customer_service.client.AuthClient;
import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.dto.response.AuthAccountResponse;
import com.group1.customer_service.dto.response.CustomerLookupResponse;
import com.group1.customer_service.dto.response.CustomerProfileResponse;
import com.group1.customer_service.entity.AddressOrder;
import com.group1.customer_service.entity.Customer;
import com.group1.customer_service.entity.Loyalty;
import com.group1.customer_service.entity.Order;
import com.group1.customer_service.entity.OrderItem;
import com.group1.customer_service.repository.AddressOrderRepository;
import com.group1.customer_service.repository.CustomerRepository;
import com.group1.customer_service.repository.OrderRepository;
import com.group1.customer_service.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private AuthClient authClient;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressOrderRepository addressOrderRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void getCustomerProfileBuildsComposedResponseWithOptionalSections() {
        Customer customer = buildCustomer();
        Loyalty loyalty = Loyalty.builder()
                .currentTier("GOLD")
                .currentPoints(245)
                .customer(customer)
                .build();
        customer.setLoyalty(loyalty);

        Order order = Order.builder()
                .orderId(500L)
                .orderNumber("ORD-500")
                .status("DELIVERED")
                .totalAmount(new BigDecimal("120000"))
                .createdAt(LocalDateTime.of(2026, 3, 20, 14, 30))
                .items(Set.of(
                        OrderItem.builder().orderItemId(1L).product("Latte").quantity(2).build(),
                        OrderItem.builder().orderItemId(2L).product("Cookie").quantity(1).build()
                ))
                .customer(customer)
                .build();

        AddressOrder address = AddressOrder.builder()
                .addressId(10L)
                .addressLine("123 Brew Street")
                .isDefault(true)
                .customer(customer)
                .build();

        AuthAccountResponse authAccount = new AuthAccountResponse(
                7L,
                "customer-user",
                "customer@example.com",
                "Customer User",
                "+84901234567",
                "https://example.com/avatar.jpg",
                "123 Brew Street",
                LocalDate.of(2000, 3, 20),
                "FEMALE",
                "ACTIVE",
                Set.of("ROLE_CUSTOMER"),
                null,
                LocalDateTime.of(2026, 3, 1, 8, 0),
                LocalDateTime.of(2026, 3, 20, 9, 0)
        );

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(authClient.getAccountById(anyString(), eq(7L))).thenReturn(new ApiResponse<>(true, "ok", authAccount));
        when(orderRepository.countByCustomer_CustomerId(1L)).thenReturn(2L);
        when(orderRepository.sumTotalAmountByCustomerId(1L)).thenReturn(new BigDecimal("240000"));
        when(orderRepository.findLastOrderDateByCustomerId(1L)).thenReturn(LocalDateTime.of(2026, 3, 20, 14, 30));
        when(orderRepository.findTop5ByCustomer_CustomerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));
        when(addressOrderRepository.findByCustomer_CustomerIdOrderByIsDefaultDescAddressIdAsc(1L)).thenReturn(List.of(address));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "customer-user", "customer@example.com", "Customer User"),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        CustomerProfileResponse response = customerService.getCustomerProfile(
                1L,
                "loyalty,orders,addresses",
                authentication,
                "Bearer test-token"
        );

        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getCustomerCode()).isEqualTo("CUS-20260301-0001");
        assertThat(response.getFullName()).isEqualTo("Customer User");
        assertThat(response.getSummary().getTotalOrders()).isEqualTo(2L);
        assertThat(response.getSummary().getTotalSpent()).isEqualByComparingTo("240000.00");
        assertThat(response.getSummary().getAverageOrderValue()).isEqualByComparingTo("120000.00");
        assertThat(response.getLoyalty().getCurrentTier()).isEqualTo("GOLD");
        assertThat(response.getOrders()).hasSize(1);
        assertThat(response.getOrders().get(0).getItemsCount()).isEqualTo(2);
        assertThat(response.getAddresses()).hasSize(1);
        assertThat(response.getAddresses().get(0).getAddressLine()).isEqualTo("123 Brew Street");
    }

    @Test
    void getCustomerLookupByUserIdRejectsDifferentUserWithoutReaderAuthority() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "customer-user", "customer@example.com", "Customer User"),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        ResponseStatusException exception = catchThrowableOfType(
                () -> customerService.getCustomerLookupByUserId(9L, authentication),
                ResponseStatusException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode().value()).isEqualTo(403);
        assertThat(exception.getReason()).isEqualTo("Forbidden");
        verify(customerRepository, never()).findByUserId(9L);
    }

    @Test
    void getCustomerLookupByUserIdAllowsReaderAuthority() {
        Customer customer = buildCustomer();
        customer.setCustomerCode("CUS-READY-0001");
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.of(customer));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(99L, "admin-user", "admin@example.com", "Admin User"),
                null,
                List.of(new SimpleGrantedAuthority("CUSTOMER:READ"))
        );

        CustomerLookupResponse response = customerService.getCustomerLookupByUserId(7L, authentication);

        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getCustomerCode()).isEqualTo("CUS-READY-0001");
        assertThat(response.getUserId()).isEqualTo(7L);
        verify(customerRepository).findByUserId(7L);
    }

    private Customer buildCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setCustomerCode(null);
        customer.setUserId(7L);
        return customer;
    }
}
