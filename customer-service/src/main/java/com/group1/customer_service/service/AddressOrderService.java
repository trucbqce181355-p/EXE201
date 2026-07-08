package com.group1.customer_service.service;

import com.group1.customer_service.dto.request.CreateAddressOrderRequest;
import com.group1.customer_service.entity.AddressOrder;
import com.group1.customer_service.entity.Customer;
import com.group1.customer_service.repository.AddressOrderRepository;
import com.group1.customer_service.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AddressOrderService {
    private static final int MAX_ADDRESSES_PER_CUSTOMER = 5;

    private final AddressOrderRepository addressOrderRepository;
    private final CustomerRepository customerRepository;

    public AddressOrderService(AddressOrderRepository addressOrderRepository, CustomerRepository customerRepository) {
        this.addressOrderRepository = addressOrderRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void setDefaultAddress(Long customerId, Long addressId) {
        AddressOrder targetAddress = addressOrderRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        if (!targetAddress.getCustomer().getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not allowed to change this address");
        }

        addressOrderRepository.resetDefaultAddressForCustomer(customerId);

        targetAddress.setIsDefault(true);
        addressOrderRepository.save(targetAddress);
    }

    @Transactional
    public void addCustomerAddress(Long customerId, CreateAddressOrderRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        String addressLine = request == null || request.getAddressLine() == null
                ? ""
                : request.getAddressLine().trim();

        if (!StringUtils.hasText(addressLine)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address line cannot be empty");
        }

        if (addressLine.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address line is too long");
        }

        long currentAddressCount = addressOrderRepository.countByCustomer_CustomerId(customerId);
        if (currentAddressCount >= MAX_ADDRESSES_PER_CUSTOMER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Each customer can save up to 5 delivery addresses"
            );
        }

        boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault()) || currentAddressCount == 0;
        if (shouldBeDefault) {
            addressOrderRepository.resetDefaultAddressForCustomer(customerId);
        }

        AddressOrder newAddress = AddressOrder.builder()
                .addressLine(addressLine)
                .isDefault(shouldBeDefault)
                .customer(customer)
                .build();

        addressOrderRepository.save(newAddress);
    }

    @Transactional
    public void deleteCustomerAddress(Long customerId, Long addressId) {
        AddressOrder address = addressOrderRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        if (!address.getCustomer().getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not have permission to delete this address");
        }

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete the default address");
        }

        addressOrderRepository.delete(address);
    }
}
