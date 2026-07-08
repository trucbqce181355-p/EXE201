
package com.group1.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = { "resource", "action" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String resource;

    @Column(nullable = false)
    private String action;

    @Column(length = 255)
    private String description;
}