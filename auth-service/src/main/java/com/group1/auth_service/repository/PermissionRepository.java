package com.group1.auth_service.repository;

import com.group1.auth_service.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);

    boolean existsByResourceAndAction(String resource, String action);

    Page<Permission> findByResource(String resource, Pageable pageable);

    Page<Permission> findByAction(String action, Pageable pageable);

    Page<Permission> findByResourceAndAction(String resource, String action, Pageable pageable);
}