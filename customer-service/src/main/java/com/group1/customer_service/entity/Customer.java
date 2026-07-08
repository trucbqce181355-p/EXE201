package com.group1.customer_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "customers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(unique = true)
    private String customerCode;

    @Column(unique = true)
    private Long userId;

    @Column(name = "loyalty_tier")
    private String loyaltyTier;

    @OneToMany(mappedBy = "customer")
    private List<AddressOrder> addressOrders;

    // Relationships

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;

    @JsonIgnore
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private Loyalty loyalty;

    @PrePersist
    public void prePersist() {
        if (this.customerCode == null) {
            this.customerCode = "CUST_" + this.userId;
        }
    }
}
    
//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//
//        if (this.customerCode == null) {
//            this.customerCode = generateCustomerCode();
//        }
//    }
//
//    private String generateCustomerCode() {
//        String datePart = LocalDate.now()
//                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//
//        int random = (int) (Math.random() * 10000);
//
//        return String.format("CUS-%s-%04d", datePart, random);
//    }