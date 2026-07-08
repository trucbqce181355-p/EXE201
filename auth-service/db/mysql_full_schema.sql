-- Fresh MySQL schema for auth-service on branch dev2.
-- Matches the current JPA mapping:
-- - users <-> roles is many-to-many via user_roles
-- - permissions stores name + resource + action
-- - includes tables used by Story 9 and Story 10

CREATE DATABASE IF NOT EXISTS auth_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE auth_db;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT uk_permissions_resource_action UNIQUE (resource, action)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    avatar_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME NULL,
    password_changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_users_status (status)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role_id (role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    KEY idx_role_permissions_permission_id (permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS revoked_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_revoked_tokens_token (token),
    KEY idx_revoked_tokens_expiry_date (expiry_date)
);

CREATE TABLE IF NOT EXISTS password_reset_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_password_reset_requests_email_requested_at (email, requested_at)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash CHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_password_reset_tokens_token_hash (token_hash),
    KEY idx_password_reset_tokens_user_id (user_id),
    KEY idx_password_reset_tokens_expires_at (expires_at),
    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS email_change_otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    new_email VARCHAR(100) NOT NULL,
    pending_username VARCHAR(50) NOT NULL,
    otp_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_email_change_otps_user_email_used_created (user_id, new_email, used, created_at),
    CONSTRAINT fk_email_change_otps_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

INSERT IGNORE INTO roles (name, description) VALUES
('ADMIN', 'System administrator'),
('MANAGER', 'System manager'),
('USER', 'Standard user');

INSERT IGNORE INTO permissions (name, resource, action, description) VALUES
('USER_VIEW', 'USER', 'VIEW', 'View users'),
('USER_CREATE', 'USER', 'CREATE', 'Create users'),
('USER_UPDATE', 'USER', 'UPDATE', 'Update users'),
('USER_DELETE', 'USER', 'DELETE', 'Delete users'),
('USER_LOCK', 'USER', 'LOCK', 'Lock or unlock users'),
('ROLE_VIEW', 'ROLE', 'VIEW', 'View roles'),
('ROLE_CREATE', 'ROLE', 'CREATE', 'Create roles'),
('ROLE_UPDATE', 'ROLE', 'UPDATE', 'Update roles'),
('ROLE_DELETE', 'ROLE', 'DELETE', 'Delete roles'),
('PERMISSION_VIEW', 'PERMISSION', 'VIEW', 'View permissions'),
('PERMISSION_CREATE', 'PERMISSION', 'CREATE', 'Create permissions'),
('PERMISSION_UPDATE', 'PERMISSION', 'UPDATE', 'Update permissions'),
('PERMISSION_DELETE', 'PERMISSION', 'DELETE', 'Delete permissions'),
('PERMISSION_ASSIGN', 'ROLE', 'ASSIGN_PERMISSION', 'Assign permissions to roles');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_VIEW',
    'USER_UPDATE',
    'ROLE_VIEW',
    'PERMISSION_VIEW'
)
WHERE r.name = 'MANAGER';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_VIEW'
)
WHERE r.name = 'USER';

INSERT IGNORE INTO users (
    email,
    username,
    password,
    full_name,
    phone,
    avatar_url,
    status
) VALUES
(
    'admin@gmail.com',
    'admin',
    '$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS',
    'System Admin',
    '0900000001',
    NULL,
    'ACTIVE'
),
(
    'manager@gmail.com',
    'manager',
    '$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS',
    'System Manager',
    '0900000002',
    NULL,
    'ACTIVE'
),
(
    'user@gmail.com',
    'user',
    '$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS',
    'Default User',
    '0900000003',
    NULL,
    'ACTIVE'
),
(
    'sasakihasei123@gmail.com',
    'hoanganh2005',
    '$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS',
    'Hoang Anh',
    '0900000004',
    NULL,
    'ACTIVE'
),
(
    'apitest.story@example.com',
    'apitest_story',
    '$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS',
    'API Test User',
    '+84900000001',
    'https://example.com/seed.jpg',
    'ACTIVE'
);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'MANAGER'
WHERE u.username = 'manager';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'USER'
WHERE u.username IN ('user', 'hoanganh2005', 'apitest_story');

SELECT
    u.id,
    u.email,
    u.username,
    u.full_name,
    u.phone,
    u.status,
    u.updated_at
FROM users u
ORDER BY u.id;
