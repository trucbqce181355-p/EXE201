package com.group1.customer_service.controller;

import com.group1.customer_service.dto.request.CreateCustomerFromRegistrationRequest;

import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.dto.response.CustomerLookupResponse;
import com.group1.customer_service.dto.response.CustomerProfileResponse;
import com.group1.customer_service.dto.LoyaltyResponseDTO;
import com.group1.customer_service.dto.RedeemRequestDTO;
import com.group1.customer_service.entity.Customer;
import com.group1.customer_service.repository.CustomerRepository;
import com.group1.customer_service.security.JwtUtil;
import com.group1.customer_service.service.CustomerService;
import com.group1.customer_service.service.LoyaltyService;
import com.group1.customer_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.group1.customer_service.dto.TierHistoryCreateRequest;
import java.util.Map;
import com.group1.customer_service.entity.Tier;
import com.group1.customer_service.entity.TierChangeReason;
import com.group1.customer_service.dto.CustomerSimpleDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping({"/customer", "/api/customer", "/customers", "/api/customers"})
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final LoyaltyService loyaltyService;
    private final CustomerRepository customerRepository;


    public CustomerController(CustomerService customerService, JwtUtil jwtUtil, OrderService orderService,
            LoyaltyService loyaltyService, CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.orderService = orderService;
        this.loyaltyService = loyaltyService;
        this.customerRepository = customerRepository;
    }

    /**
     * Gọi từ auth-service sau đăng ký, kèm Bearer access token (userId trong token phải khớp body).
     */
    @PostMapping("/from-registration")
    public ResponseEntity<ApiResponse<CustomerLookupResponse>> createCustomerFromRegistration(
            @Valid @RequestBody CreateCustomerFromRegistrationRequest request,
            Authentication authentication) {
        CustomerLookupResponse data = customerService.createCustomerFromRegistration(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Customer profile created successfully.",
                data
        ));
    }

    @PostMapping("/orders/{id}/apply-discount")
    public ResponseEntity<?> applyDiscount(
            @PathVariable Long id,
            @RequestParam BigDecimal amount
    ) {
        orderService.applyDiscount(id, amount);
        return ResponseEntity.ok("Discount applied");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getCustomerProfile(
            @PathVariable("id") Long customerId,
            @RequestParam(required = false) String include,
            @RequestHeader("Authorization") String authorizationHeader,
            Authentication authentication) {
        CustomerProfileResponse response = customerService.getCustomerProfile(
                customerId,
                include,
                authentication,
                authorizationHeader
        );

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Customer profile loaded successfully.",
                response
        ));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponse<CustomerLookupResponse>> getCustomerByUserId(
            @PathVariable("userId") Long userId,
            Authentication authentication) {
        CustomerLookupResponse response = customerService.getCustomerLookupByUserId(userId, authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Customer lookup loaded successfully.",
                response
        ));
    }

    @PostMapping("/all")
    public ResponseEntity<?> register() {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.getAll());
    }
    // GET ALL CUSTOMERS

    @GetMapping("/all")
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAll());
    }

    // LIGHTWEIGHT CUSTOMER LIST (flat objects, no nested relations)
    @GetMapping("/simple")
    public ResponseEntity<List<CustomerSimpleDTO>> getSimpleCustomers(
            @RequestParam(name = "q", required = false) String query,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(customerService.getSimpleList(query, authorizationHeader));
    }






    // GET CUSTOMER BY USER ID
    @GetMapping("/find-id/{userId}")
    public ResponseEntity<?> getCustomerId(@PathVariable Long userId) {
        try {
            Long customerId = customerService.getCustomerIdByUserId(userId);

            return ResponseEntity.ok(Map.of("id", customerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }



    // CREATE CUSTOMER IF NOT EXISTS

    @PostMapping("/create-if-not-exists")
    public ResponseEntity<?> createIfNotExists(@RequestParam Long userId) {
        customerService.createIfNotExists(userId);
        return ResponseEntity.ok("Customer created or already exists");
    }

    // DELETE CUSTOMER BY USER ID

    @DeleteMapping("/delete-by-user")
    public ResponseEntity<?> deleteByUser(@RequestParam Long userId) {
        try {
            customerService.deleteByUserId(userId);
            return ResponseEntity.ok("Customer deleted successfully");
        } catch (Exception e) {
            // Không throw nếu customer không tồn tại
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cannot delete customer: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteByUserId(@PathVariable Long userId) {
        customerService.deleteByUserId(userId);
        return ResponseEntity.ok("Customer deleted");
    }

    @GetMapping("/{id}/loyalty")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoyaltyResponseDTO> getLoyalty(@PathVariable Long id) {
        System.out.println("vao loyalty");
        return ResponseEntity.ok(loyaltyService.getLoyalty(id));
    }

    // GET POINTS HISTORY
    @GetMapping("/{id}/loyalty/points-history")
    public ResponseEntity<?> getPointsHistory(
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        System.out.println("fillter type: " + type);
        return ResponseEntity.ok(
                loyaltyService.getPointsHistory(id, type, from, to, page, size));
    }

    // GET TIER HISTORY
    @GetMapping("/{id}/loyalty/tier-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTierHistory(@PathVariable Long id) {
        return ResponseEntity.ok(loyaltyService.getTierHistory(id));
    }

    // CREATE TIER HISTORY (to be called from other services)
    @PostMapping("/{id}/loyalty/tier-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createTierHistory(
            @PathVariable Long id,
            @RequestBody TierHistoryCreateRequest request
    ) {
        loyaltyService.createTierHistory(
                id,
                Tier.valueOf(request.getFromTier()),
                Tier.valueOf(request.getToTier()),
                TierChangeReason.valueOf(request.getReason())
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // SYNC TIER (no history) - used to ensure denormalized fields are updated
    @PutMapping("/{id}/loyalty/tier-sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> syncTier(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String tier = body.get("tier");
        loyaltyService.syncTier(id, Tier.valueOf(tier));
        return ResponseEntity.ok().build();
    }

    // EARN POINTS
    @PostMapping("/{id}/earn")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> earnPoints(
            @PathVariable Long id,
            @RequestParam int points) {

        loyaltyService.earnPoints(id, points);
        return ResponseEntity.ok(java.util.Map.of("message", "Earn success"));
    }

    // REDEEM POINTS
    @PostMapping("/{id}/redeem")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> redeemPoints(
            @PathVariable Long id,
            @RequestBody RedeemRequestDTO request) {

        loyaltyService.redeemPoints(id, request.getPoints());
        return ResponseEntity.ok(java.util.Map.of("message", "Redeem success"));
    }

    @PostMapping
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, Object> req) {
        Long userId = Long.valueOf(req.get("userId").toString());
        System.out.println("Creating customer for userId: " + userId);
        Customer c = new Customer();
        c.setUserId(userId);

        return ResponseEntity.ok(customerRepository.save(c));
    }

}