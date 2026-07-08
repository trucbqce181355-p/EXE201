package com.group1.customer_service.controller;

import com.group1.customer_service.dto.request.CreateAddressOrderRequest;
import com.group1.customer_service.dto.response.ApiResponse;
import com.group1.customer_service.service.AddressOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address-order")
public class AddressOrderController {

    private final AddressOrderService addressOrderService;

    public AddressOrderController(AddressOrderService addressOrderService) {
        this.addressOrderService = addressOrderService;
    }

    @PutMapping("/{customerId}/address/{id}/default")
    public ResponseEntity<ApiResponse<Void>> updateAddressOrder(
            @PathVariable("id") Long addressId,
            @PathVariable("customerId") Long customerId,
            @RequestHeader(value = "Authorization", required = false) String header) {

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    "Authorization header.",
                    null
            ));
        }

        try {
            addressOrderService.setDefaultAddress(customerId, addressId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "The default address has been successfully updated.",
                    null
            ));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ApiResponse<>(
                    false,
                    ex.getReason(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    "Error updating address: " + e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/{customerId}/address")
    public ResponseEntity<ApiResponse<Object>> addAddressOrder(
            @PathVariable("customerId") Long customerId,
            @Valid @RequestBody CreateAddressOrderRequest request,
            HttpServletRequest httpRequest) {

        String header = httpRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, "Missing authentication token.", null
            ));
        }

        try {
            addressOrderService.addCustomerAddress(customerId, request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Address saved successfully.", null
            ));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ApiResponse<>(
                    false, ex.getReason(), null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, "Error creating address: " + e.getMessage(), null
            ));
        }
    }

    @DeleteMapping("/{customerId}/address/{addressId}")
    public ResponseEntity<ApiResponse<Object>> deleteAddressOrder(
            @PathVariable("customerId") Long customerId,
            @PathVariable("addressId") Long addressId,
            HttpServletRequest httpRequest) {

        String header = httpRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, "Missing authentication token.", null
            ));
        }

        try {
            addressOrderService.deleteCustomerAddress(customerId, addressId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Address deleted successfully.", null
            ));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ApiResponse<>(
                    false, ex.getReason(), null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, "Error deleting address: " + e.getMessage(), null
            ));
        }
    }
}
