package com.group1.customer_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address_orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AddressOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    private String addressLine;

    private Boolean isDefault;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;
}