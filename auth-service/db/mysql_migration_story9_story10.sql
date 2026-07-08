-- One-time migration from the legacy single-role schema
-- to the current dev2 schema used by auth-service.
--
-- Legacy model:
-- - users.role_id
-- - permissions(name, description)
--
-- Current model:
-- - users <-> roles via user_roles
-- - permissions(name, resource, action, description)

USE auth_db;

ALTER TABLE permissions
    ADD COLUMN IF NOT EXISTS resource VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS action VARCHAR(50) NULL;

UPDATE permissions
SET
    resource = CASE name
        WHEN 'USER_VIEW' THEN 'USER'
        WHEN 'USER_CREATE' THEN 'USER'
        WHEN 'USER_UPDATE' THEN 'USER'
        WHEN 'USER_DELETE' THEN 'USER'
        WHEN 'USER_LOCK' THEN 'USER'
        WHEN 'ROLE_VIEW' THEN 'ROLE'
        WHEN 'ROLE_CREATE' THEN 'ROLE'
        WHEN 'ROLE_UPDATE' THEN 'ROLE'
        WHEN 'ROLE_DELETE' THEN 'ROLE'
        WHEN 'PERMISSION_VIEW' THEN 'PERMISSION'
        WHEN 'PERMISSION_CREATE' THEN 'PERMISSION'
        WHEN 'PERMISSION_UPDATE' THEN 'PERMISSION'
        WHEN 'PERMISSION_DELETE' THEN 'PERMISSION'
        WHEN 'PERMISSION_ASSIGN' THEN 'ROLE'
        ELSE COALESCE(resource, 'SYSTEM')
    END,
    action = CASE name
        WHEN 'USER_VIEW' THEN 'VIEW'
        WHEN 'USER_CREATE' THEN 'CREATE'
        WHEN 'USER_UPDATE' THEN 'UPDATE'
        WHEN 'USER_DELETE' THEN 'DELETE'
        WHEN 'USER_LOCK' THEN 'LOCK'
        WHEN 'ROLE_VIEW' THEN 'VIEW'
        WHEN 'ROLE_CREATE' THEN 'CREATE'
        WHEN 'ROLE_UPDATE' THEN 'UPDATE'
        WHEN 'ROLE_DELETE' THEN 'DELETE'
        WHEN 'PERMISSION_VIEW' THEN 'VIEW'
        WHEN 'PERMISSION_CREATE' THEN 'CREATE'
        WHEN 'PERMISSION_UPDATE' THEN 'UPDATE'
        WHEN 'PERMISSION_DELETE' THEN 'DELETE'
        WHEN 'PERMISSION_ASSIGN' THEN 'ASSIGN_PERMISSION'
        ELSE COALESCE(action, 'MANAGE')
    END
WHERE resource IS NULL OR action IS NULL;

ALTER TABLE permissions
    MODIFY COLUMN resource VARCHAR(50) NOT NULL,
    MODIFY COLUMN action VARCHAR(50) NOT NULL;

SET @permissions_unique_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'permissions'
      AND CONSTRAINT_NAME = 'uk_permissions_resource_action'
      AND CONSTRAINT_TYPE = 'UNIQUE'
);
SET @permissions_unique_sql = IF(
    @permissions_unique_exists = 0,
    'ALTER TABLE permissions ADD CONSTRAINT uk_permissions_resource_action UNIQUE (resource, action)',
    'SELECT 1'
);
PREPARE stmt FROM @permissions_unique_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS last_login_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS password_changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

UPDATE users
SET
    full_name = COALESCE(NULLIF(TRIM(full_name), ''), username),
    password_changed_at = COALESCE(password_changed_at, CURRENT_TIMESTAMP),
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

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

SET @has_users_role_id = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role_id'
);
SET @copy_user_roles_sql = IF(
    @has_users_role_id > 0,
    'INSERT IGNORE INTO user_roles (user_id, role_id) SELECT id, role_id FROM users WHERE role_id IS NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @copy_user_roles_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @users_role_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role_id'
      AND REFERENCED_TABLE_NAME = 'roles'
    LIMIT 1
);
SET @drop_users_role_fk_sql = IF(
    @users_role_fk IS NOT NULL,
    CONCAT('ALTER TABLE users DROP FOREIGN KEY ', @users_role_fk),
    'SELECT 1'
);
PREPARE stmt FROM @drop_users_role_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @users_role_idx = (
    SELECT INDEX_NAME
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role_id'
      AND INDEX_NAME <> 'PRIMARY'
    LIMIT 1
);
SET @drop_users_role_idx_sql = IF(
    @users_role_idx IS NOT NULL,
    CONCAT('ALTER TABLE users DROP INDEX ', @users_role_idx),
    'SELECT 1'
);
PREPARE stmt FROM @drop_users_role_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_users_role_column_sql = IF(
    @has_users_role_id > 0,
    'ALTER TABLE users DROP COLUMN role_id',
    'SELECT 1'
);
PREPARE stmt FROM @drop_users_role_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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

INSERT IGNORE INTO permissions (name, resource, action, description) VALUES
('USER_LOCK', 'USER', 'LOCK', 'Lock or unlock users'),
('PERMISSION_CREATE', 'PERMISSION', 'CREATE', 'Create permissions'),
('PERMISSION_UPDATE', 'PERMISSION', 'UPDATE', 'Update permissions'),
('PERMISSION_DELETE', 'PERMISSION', 'DELETE', 'Delete permissions');
