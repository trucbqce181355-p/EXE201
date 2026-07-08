package com.group1.auth_service.repository;

import com.group1.auth_service.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByName(String name);
    Optional<Role> findByName(String name);
    
    // Search by name or description
    Page<Role> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);
    
    // Filter by isActive
    Page<Role> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Search by name or description AND filter by isActive
    Page<Role> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsActive(
            String name, String description, Boolean isActive, Pageable pageable);
}