package com.group1.customer_service.service;

import com.group1.customer_service.dto.request.CreateAddressOrderRequest;
import com.group1.customer_service.entity.AddressOrder;
import com.group1.customer_service.entity.Customer;
import com.group1.customer_service.repository.AddressOrderRepository;
import com.group1.customer_service.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressOrderServiceTest {

    @Mock
    private AddressOrderRepository addressOrderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AddressOrderService addressOrderService;

    @Test
    void addCustomerAddressMakesTheFirstAddressDefault() {
        Customer customer = Customer.builder()
                .customerId(1L)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressOrderRepository.countByCustomer_CustomerId(1L)).thenReturn(0L);

        CreateAddressOrderRequest request = new CreateAddressOrderRequest(" 123 Brew Street ", false);

        addressOrderService.addCustomerAddress(1L, request);

        ArgumentCaptor<AddressOrder> savedAddress = ArgumentCaptor.forClass(AddressOrder.class);
        verify(addressOrderRepository).resetDefaultAddressForCustomer(1L);
        verify(addressOrderRepository).save(savedAddress.capture());

        assertThat(savedAddress.getValue().getAddressLine()).isEqualTo("123 Brew Street");
        assertThat(savedAddress.getValue().getIsDefault()).isTrue();
        assertThat(savedAddress.getValue().getCustomer()).isSameAs(customer);
    }

    @Test
    void addCustomerAddressRejectsSavingMoreThanFiveAddresses() {
        Customer customer = Customer.builder()
                .customerId(1L)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressOrderRepository.countByCustomer_CustomerId(1L)).thenReturn(5L);

        ResponseStatusException exception = catchThrowableOfType(
                () -> addressOrderService.addCustomerAddress(1L, new CreateAddressOrderRequest("123 Brew Street", false)),
                ResponseStatusException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("Each customer can save up to 5 delivery addresses");
        verify(addressOrderRepository, never()).save(org.mockito.ArgumentMatchers.any(AddressOrder.class));
    }

    @Test
    void deleteCustomerAddressRejectsDeletingTheDefaultAddress() {
        Customer customer = Customer.builder()
                .customerId(1L)
                .build();
        AddressOrder address = AddressOrder.builder()
                .addressId(10L)
                .addressLine("123 Brew Street")
                .isDefault(true)
                .customer(customer)
                .build();

        when(addressOrderRepository.findById(10L)).thenReturn(Optional.of(address));

        ResponseStatusException exception = catchThrowableOfType(
                () -> addressOrderService.deleteCustomerAddress(1L, 10L),
                ResponseStatusException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("Cannot delete the default address");
        verify(addressOrderRepository, never()).delete(address);
    }

    @Test
    void setDefaultAddressRejectsAddressesThatBelongToAnotherCustomer() {
        Customer owner = Customer.builder()
                .customerId(2L)
                .build();
        AddressOrder address = AddressOrder.builder()
                .addressId(10L)
                .addressLine("123 Brew Street")
                .isDefault(false)
                .customer(owner)
                .build();

        when(addressOrderRepository.findById(10L)).thenReturn(Optional.of(address));

        ResponseStatusException exception = catchThrowableOfType(
                () -> addressOrderService.setDefaultAddress(1L, 10L),
                ResponseStatusException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("You are not allowed to change this address");
        verify(addressOrderRepository, never()).resetDefaultAddressForCustomer(1L);
    }
}
