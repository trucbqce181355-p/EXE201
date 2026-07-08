package com.group1.customer_service.service;

import com.group1.customer_service.client.AuthClient;
import com.group1.customer_service.dto.request.CreateCustomerFromRegistrationRequest;

import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.dto.response.AuthAccountResponse;
import com.group1.customer_service.dto.response.CustomerLookupResponse;
import com.group1.customer_service.dto.response.CustomerProfileAddressResponse;
import com.group1.customer_service.dto.response.CustomerProfileLoyaltyResponse;
import com.group1.customer_service.dto.response.CustomerProfileOrderResponse;
import com.group1.customer_service.dto.response.CustomerProfileResponse;
import com.group1.customer_service.dto.response.CustomerProfileSummaryResponse;
import com.group1.customer_service.entity.AddressOrder;

import com.group1.customer_service.entity.Customer;
import com.group1.customer_service.entity.Order;
import com.group1.customer_service.repository.AddressOrderRepository;
import com.group1.customer_service.repository.CustomerRepository;

import com.group1.customer_service.repository.OrderRepository;
import com.group1.customer_service.security.AuthenticatedUser;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private static final String CUSTOMER_NOT_FOUND_MESSAGE = "Customer not found";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";


    private final AuthClient authClient;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final AddressOrderRepository addressOrderRepository;



    @Transactional
    public CustomerLookupResponse createCustomerFromRegistration(
            CreateCustomerFromRegistrationRequest request,
            Authentication authentication) {
        Long userId = request.getUserId();
        AuthenticatedUser actor = getAuthenticatedUser(authentication);
        if (!Objects.equals(actor.getUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE);
        }

        return customerRepository.findByUserId(userId)
                .map(c -> new CustomerLookupResponse(
                        c.getCustomerId(),
                        resolveCustomerCode(c, null),
                        c.getUserId()))
                .orElseGet(() -> {
                    Customer customer = Customer.builder()
                            .userId(userId)
                            .customerCode("CUS-U" + userId)
                            .build();
                    Customer saved = customerRepository.save(customer);
                    return new CustomerLookupResponse(
                            saved.getCustomerId(),
                            resolveCustomerCode(saved, null),
                            saved.getUserId());
                });
    }

    @Transactional(readOnly = true)

    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<com.group1.customer_service.dto.CustomerSimpleDTO> getSimpleList(String query, String authorizationHeader) {
        List<Customer> all = customerRepository.findAll();
        String q = query == null ? "" : query.trim().toLowerCase();

        // Batch fetch basic user info from auth-service
        List<Long> userIds = all.stream()
                .map(Customer::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, com.group1.customer_service.dto.response.AuthUserBasicDTO> basicById = new java.util.HashMap<>();
        try {
            if (!userIds.isEmpty()) {
                List<com.group1.customer_service.dto.response.AuthUserBasicDTO> basics =
                        authClient.getUsersBasic(authorizationHeader, userIds);
                if (basics != null) {
                    for (var b : basics) {
                        if (b != null && b.getId() != null) {
                            basicById.put(b.getId(), b);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // best-effort: nếu auth-service không phản hồi, vẫn trả danh sách cơ bản
        }

        return all.stream()
                .filter(c -> {
                    if (q.isEmpty()) return true;
                    String code = c.getCustomerCode() == null ? "" : c.getCustomerCode().toLowerCase();
                    String idStr = c.getCustomerId() == null ? "" : String.valueOf(c.getCustomerId());
                    String userIdStr = c.getUserId() == null ? "" : String.valueOf(c.getUserId());
                    var basic = c.getUserId() == null ? null : basicById.get(c.getUserId());
                    String fullName = basic == null || basic.getFullName() == null ? "" : basic.getFullName().toLowerCase();
                    String email = basic == null || basic.getEmail() == null ? "" : basic.getEmail().toLowerCase();
                    return code.contains(q) || idStr.contains(q) || userIdStr.contains(q)
                            || fullName.contains(q) || email.contains(q);
                })
                .map(c -> {
                    var basic = c.getUserId() == null ? null : basicById.get(c.getUserId());
                    String fullName = basic == null ? null : basic.getFullName();
                    String email = basic == null ? null : basic.getEmail();
                    return new com.group1.customer_service.dto.CustomerSimpleDTO(
                            c.getCustomerId(),
                            c.getCustomerCode(),
                            c.getUserId(),
                            fullName,
                            email
                    );
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public CustomerLookupResponse getCustomerLookupByUserId(Long userId, Authentication authentication) {
        AuthenticatedUser actor = getAuthenticatedUser(authentication);
        if (!Objects.equals(actor.getUserId(), userId) && !canReadCustomers(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE);
        }

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CUSTOMER_NOT_FOUND_MESSAGE));

        return new CustomerLookupResponse(
                customer.getCustomerId(),
                resolveCustomerCode(customer, null),
                customer.getUserId()
        );
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerProfile(
            Long customerId,
            String include,
            Authentication authentication,
            String authorizationHeader) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CUSTOMER_NOT_FOUND_MESSAGE));

        AuthenticatedUser actor = getAuthenticatedUser(authentication);
        if (!Objects.equals(actor.getUserId(), customer.getUserId()) && !canReadCustomers(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE);
        }

        AuthAccountResponse authProfile = loadAuthProfile(customer, authorizationHeader);
        Set<String> includes = parseIncludes(include);

        long totalOrders = orderRepository.countByCustomer_CustomerId(customerId);
        BigDecimal totalSpent = defaultCurrency(orderRepository.sumTotalAmountByCustomerId(customerId));
        BigDecimal averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        LocalDateTime lastOrderDate = orderRepository.findLastOrderDateByCustomerId(customerId);

        CustomerProfileResponse.CustomerProfileResponseBuilder builder = CustomerProfileResponse.builder()
                .customerId(customer.getCustomerId())
                .customerCode(resolveCustomerCode(customer, authProfile))
                .fullName(authProfile.getFullName())
                .avatarUrl(authProfile.getAvatarUrl())
                .email(authProfile.getEmail())
                .phone(authProfile.getPhone())
                .address(authProfile.getAddress())
                .dateOfBirth(authProfile.getDateOfBirth())
                .gender(authProfile.getGender())
                .status(authProfile.getStatus())
                .createdAt(authProfile.getCreatedAt())
                .summary(CustomerProfileSummaryResponse.builder()
                        .totalOrders(totalOrders)
                        .totalSpent(totalSpent)
                        .averageOrderValue(averageOrderValue)
                        .lastOrderDate(lastOrderDate)
                        .build());

        if (includes.contains("loyalty") && customer.getLoyalty() != null) {
            builder.loyalty(CustomerProfileLoyaltyResponse.builder()
                    .currentTier(customer.getLoyalty().getCurrentTier())
                    .currentPoints(customer.getLoyalty().getCurrentPoints())
                    .build());
        }

        if (includes.contains("orders")) {
            builder.orders(orderRepository.findTop5ByCustomer_CustomerIdOrderByCreatedAtDesc(customerId).stream()
                    .map(this::toCustomerProfileOrderResponse)
                    .toList());
        }

        if (includes.contains("addresses")) {
            builder.addresses(addressOrderRepository.findByCustomer_CustomerIdOrderByIsDefaultDescAddressIdAsc(customerId).stream()
                    .map(this::toCustomerProfileAddressResponse)
                    .toList());
        }

        return builder.build();
    }

    private AuthAccountResponse loadAuthProfile(Customer customer, String authorizationHeader) {
        try {
            ApiResponse<AuthAccountResponse> response = authClient.getAccountById(authorizationHeader, customer.getUserId());
            if (response == null || response.getData() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, CUSTOMER_NOT_FOUND_MESSAGE);
            }
            return response.getData();
        } catch (FeignException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CUSTOMER_NOT_FOUND_MESSAGE);
        } catch (FeignException.Forbidden ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_MESSAGE);
        } catch (FeignException.Unauthorized ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        } catch (FeignException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load customer profile");
        }
    }

    private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return authenticatedUser;
    }

    private boolean canReadCustomers(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return authorities.contains("CUSTOMER:READ")
                || authorities.contains("CUSTOMER_READ")
                || authorities.contains("USER_VIEW")
                || authorities.contains("ROLE_ADMIN")
                || authorities.contains("ROLE_MANAGER");
    }

    private Set<String> parseIncludes(String include) {
        if (!StringUtils.hasText(include)) {
            return Set.of();
        }

        return Arrays.stream(include.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private BigDecimal defaultCurrency(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private CustomerProfileOrderResponse toCustomerProfileOrderResponse(Order order) {
        return CustomerProfileOrderResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .itemsCount(order.getItems() == null ? 0 : order.getItems().size())
                .build();
    }

    private CustomerProfileAddressResponse toCustomerProfileAddressResponse(AddressOrder address) {
        return CustomerProfileAddressResponse.builder()
                .addressId(address.getAddressId())
                .addressLine(address.getAddressLine())
                .isDefault(address.getIsDefault())
                .build();
    }

    private String resolveCustomerCode(Customer customer, AuthAccountResponse authProfile) {
        if (StringUtils.hasText(customer.getCustomerCode())) {
            return customer.getCustomerCode().trim();
        }

        LocalDate codeDate = authProfile != null && authProfile.getCreatedAt() != null
                ? authProfile.getCreatedAt().toLocalDate()
                : LocalDate.now();

        return "CUS-"
                + codeDate.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + String.format("%04d", customer.getCustomerId() % 10000);
    }
    // service
    public Customer getByUserId(Long userId) {
        return customerRepository.findByUserId(userId).orElse(null);

    }

    public Long getCustomerIdByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .map(Customer::getCustomerId)
                .orElseGet(() -> {
                    System.out.println("[CustomerService] Customer not found for userId " + userId + ", auto-creating...");
                    Customer customer = new Customer();
                    customer.setUserId(userId);
                    customer.setCustomerCode("CUS-" + System.currentTimeMillis() + "-" + userId);
                    Customer saved = customerRepository.save(customer);
                    return saved.getCustomerId();
                });
    }


    @Transactional
    public Customer getOrCreateByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setUserId(userId);
                    customer.setCustomerCode("CUST_" + userId);
                    return customerRepository.save(customer);
                });
    }

    @Transactional
    public void createIfNotExists(Long userId) {
        boolean exists = customerRepository.existsByUserId(userId);
        if (!exists) {
            Customer customer = new Customer();
            customer.setUserId(userId);
            customer.setCustomerCode("CUST_" + userId);
            customerRepository.save(customer);
        }
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        customerRepository.findByUserId(userId).ifPresent(customer -> {
            System.out.println(" Deleting customer with userId = " + userId);
            customerRepository.delete(customer);
        });

    }

    public Customer createCustomer(Long userId) {

        // ❗ tránh tạo trùng
        if (customerRepository.existsByUserId(userId)) {
            throw new RuntimeException("Customer already exists for userId: " + userId);
        }

        Customer customer = new Customer();
        customer.setUserId(userId);

        // tạo code (optional)
        customer.setCustomerCode("CUS" + System.currentTimeMillis());

        return customerRepository.save(customer);
    }
}

