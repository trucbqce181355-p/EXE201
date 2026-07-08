-- Local demo reset for Story 20: View Customer Profile
-- WARNING:
-- - This script is intended for LOCAL DEVELOPMENT only.
-- - It will DROP and recreate auth_db and customer_db.
-- - Run it in MySQL before starting auth-service and customer-service.
--
-- Demo accounts after running:
--   admin@gmail.com / 123456
--   manager@gmail.com / 123456
--   customer.demo@example.com / 123456

DROP DATABASE IF EXISTS customer_db;
DROP DATABASE IF EXISTS auth_db;

CREATE DATABASE auth_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE customer_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE auth_db;

CREATE TABLE permissions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  resource VARCHAR(255) NOT NULL,
  action VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  UNIQUE KEY uk_permissions_resource_action (resource, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255),
  is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  full_name VARCHAR(100),
  phone VARCHAR(20),
  avatar_url VARCHAR(500),
  address VARCHAR(255),
  date_of_birth DATE,
  gender VARCHAR(255),
  status VARCHAR(20) NOT NULL,
  last_login_at DATETIME NULL,
  password_changed_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE revoked_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(512) NOT NULL,
  expiry_date DATETIME NULL,
  INDEX idx_revoked_tokens_token (token(255)),
  INDEX idx_revoked_tokens_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(512) NOT NULL,
  user_id BIGINT NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_refresh_tokens_token (token(255)),
  INDEX idx_refresh_tokens_user (user_id),
  INDEX idx_refresh_tokens_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expires_at DATETIME NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  used_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_prt_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_requests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100) NOT NULL,
  requested_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE email_change_otps (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  new_email VARCHAR(255) NOT NULL,
  pending_username VARCHAR(255) NOT NULL,
  otp_hash VARCHAR(64) NOT NULL,
  expires_at DATETIME NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_eco_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_eco_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (name, resource, action, description) VALUES
('USER_VIEW', 'USER', 'VIEW', 'View users'),
('USER_CREATE', 'USER', 'CREATE', 'Create users'),
('USER_UPDATE', 'USER', 'UPDATE', 'Update users'),
('USER_DELETE', 'USER', 'DELETE', 'Delete users'),
('USER_LOCK', 'USER', 'LOCK', 'Lock or unlock users'),
('ROLE:READ', 'ROLE', 'READ', 'View roles'),
('ROLE:CREATE', 'ROLE', 'CREATE', 'Create roles'),
('ROLE:UPDATE', 'ROLE', 'UPDATE', 'Update roles'),
('ROLE:DELETE', 'ROLE', 'DELETE', 'Delete roles'),
('PERMISSION:READ', 'PERMISSION', 'READ', 'View permissions'),
('PERMISSION:CREATE', 'PERMISSION', 'CREATE', 'Create permissions'),
('PERMISSION:UPDATE', 'PERMISSION', 'UPDATE', 'Update permissions'),
('PERMISSION:DELETE', 'PERMISSION', 'DELETE', 'Delete permissions'),
('CUSTOMER:READ', 'CUSTOMER', 'READ', 'View customer profiles');

INSERT INTO roles (name, description, is_active) VALUES
('ADMIN', 'System administrator', TRUE),
('MANAGER', 'System manager', TRUE),
('ROLE_CUSTOMER', 'Default customer role', TRUE);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.name = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('USER_VIEW', 'ROLE:READ', 'PERMISSION:READ', 'CUSTOMER:READ')
WHERE r.name = 'MANAGER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('CUSTOMER:READ')
WHERE r.name = 'ROLE_CUSTOMER';

-- BCrypt hash for password: 123456
SET @demo_password_hash = '$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS';

INSERT INTO users (
  username,
  password,
  email,
  full_name,
  phone,
  avatar_url,
  address,
  date_of_birth,
  gender,
  status,
  last_login_at,
  password_changed_at,
  created_at,
  updated_at
) VALUES
(
  'admin',
  @demo_password_hash,
  'admin@gmail.com',
  'System Admin',
  '0900000001',
  'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80',
  '1 Admin Street, Ho Chi Minh City',
  '1998-01-10',
  'MALE',
  'ACTIVE',
  NULL,
  NOW(),
  NOW(),
  NOW()
),
(
  'manager',
  @demo_password_hash,
  'manager@gmail.com',
  'System Manager',
  '0900000002',
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80',
  '2 Manager Avenue, Ho Chi Minh City',
  '1997-07-15',
  'FEMALE',
  'ACTIVE',
  NULL,
  NOW(),
  NOW(),
  NOW()
),
(
  'customer_demo',
  @demo_password_hash,
  'customer.demo@example.com',
  'Customer Demo',
  '+84901234567',
  'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=300&q=80',
  '123 Brew Street, District 1, Ho Chi Minh City',
  '2000-03-20',
  'FEMALE',
  'ACTIVE',
  NULL,
  NOW(),
  NOW(),
  NOW()
),
(
  'customer_demo_2',
  @demo_password_hash,
  'customer.demo2@example.com',
  'Customer Demo Two',
  '+84908887766',
  'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=300&q=80',
  '456 Roast Avenue, District 7, Ho Chi Minh City',
  '1999-08-08',
  'MALE',
  'ACTIVE',
  NULL,
  NOW(),
  NOW(),
  NOW()
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'MANAGER'
WHERE u.username = 'manager';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_CUSTOMER'
WHERE u.username IN ('customer_demo', 'customer_demo_2');

USE customer_db;

CREATE TABLE customers (
  customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_code VARCHAR(255) UNIQUE,
  user_id BIGINT NOT NULL,
  UNIQUE KEY uk_customers_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loyalty (
  loyalty_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  current_points INT,
  lifetime_points INT,
  current_tier VARCHAR(255),
  enrolled_at DATETIME,
  customer_id BIGINT UNIQUE,
  CONSTRAINT fk_loyalty_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
  order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_number VARCHAR(255),
  status VARCHAR(255),
  total_amount DECIMAL(19, 2),
  created_at DATETIME,
  customer_id BIGINT,
  CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_items (
  order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product VARCHAR(255),
  quantity INT,
  price DECIMAL(19, 2),
  order_id BIGINT,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  method VARCHAR(255),
  status VARCHAR(255),
  paid_at DATETIME,
  order_id BIGINT UNIQUE,
  CONSTRAINT fk_order_payments_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_deliveries (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  address VARCHAR(255),
  receiver_name VARCHAR(255),
  phone VARCHAR(255),
  status VARCHAR(255),
  order_id BIGINT UNIQUE,
  CONSTRAINT fk_order_deliveries_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_status_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  status VARCHAR(255),
  updated_at DATETIME,
  order_id BIGINT,
  CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE address_orders (
  address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  address_line VARCHAR(255),
  is_default BOOLEAN,
  customer_id BIGINT,
  CONSTRAINT fk_address_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE points_history (
  history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(255),
  amount INT,
  description VARCHAR(255),
  order_id DECIMAL(19, 2),
  created_at DATETIME,
  customer_id BIGINT,
  CONSTRAINT fk_points_history_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tier_history (
  tier_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  from_tier VARCHAR(255),
  to_tier VARCHAR(255),
  reason VARCHAR(255),
  changed_at DATETIME,
  customer_id BIGINT,
  CONSTRAINT fk_tier_history_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE segments (
  segment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(255),
  criteria JSON,
  created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE segment_customers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  segment_id BIGINT,
  customer_id BIGINT,
  CONSTRAINT fk_segment_customers_segment FOREIGN KEY (segment_id) REFERENCES segments(segment_id) ON DELETE CASCADE,
  CONSTRAINT fk_segment_customers_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO customers (customer_code, user_id)
SELECT 'CUS-20260322-0001', id
FROM auth_db.users
WHERE username = 'customer_demo';

INSERT INTO customers (customer_code, user_id)
SELECT 'CUS-20260322-0002', id
FROM auth_db.users
WHERE username = 'customer_demo_2';

SET @customer_demo_id = (
  SELECT customer_id
  FROM customers
  WHERE user_id = (SELECT id FROM auth_db.users WHERE username = 'customer_demo')
);

SET @customer_demo_2_id = (
  SELECT customer_id
  FROM customers
  WHERE user_id = (SELECT id FROM auth_db.users WHERE username = 'customer_demo_2')
);

INSERT INTO loyalty (current_points, lifetime_points, current_tier, enrolled_at, customer_id) VALUES
(245, 520, 'GOLD', NOW() - INTERVAL 180 DAY, @customer_demo_id),
(90, 110, 'BRONZE', NOW() - INTERVAL 30 DAY, @customer_demo_2_id);

INSERT INTO address_orders (address_line, is_default, customer_id) VALUES
('123 Brew Street, District 1, Ho Chi Minh City', TRUE, @customer_demo_id),
('Tower B, 88 Nguyen Hue, District 1, Ho Chi Minh City', FALSE, @customer_demo_id),
('456 Roast Avenue, District 7, Ho Chi Minh City', TRUE, @customer_demo_2_id);

INSERT INTO orders (order_number, status, total_amount, created_at, customer_id) VALUES
('ORD-1001', 'DELIVERED', 120000, NOW() - INTERVAL 10 DAY, @customer_demo_id),
('ORD-1002', 'PROCESSING', 185000, NOW() - INTERVAL 2 DAY, @customer_demo_id),
('ORD-2001', 'DELIVERED', 95000, NOW() - INTERVAL 6 DAY, @customer_demo_2_id);

SET @order_1001_id = (
  SELECT order_id FROM orders WHERE order_number = 'ORD-1001'
);
SET @order_1002_id = (
  SELECT order_id FROM orders WHERE order_number = 'ORD-1002'
);
SET @order_2001_id = (
  SELECT order_id FROM orders WHERE order_number = 'ORD-2001'
);

INSERT INTO order_items (product, quantity, price, order_id) VALUES
('Latte', 2, 50000, @order_1001_id),
('Cookie', 1, 20000, @order_1001_id),
('Cold Brew', 1, 65000, @order_1002_id),
('Cheesecake', 2, 60000, @order_1002_id),
('Espresso', 1, 45000, @order_2001_id),
('Brownie', 1, 50000, @order_2001_id);

INSERT INTO order_payments (method, status, paid_at, order_id) VALUES
('CARD', 'PAID', NOW() - INTERVAL 10 DAY, @order_1001_id),
('CASH', 'PENDING', NULL, @order_1002_id),
('CARD', 'PAID', NOW() - INTERVAL 6 DAY, @order_2001_id);

INSERT INTO order_deliveries (address, receiver_name, phone, status, order_id) VALUES
('123 Brew Street, District 1, Ho Chi Minh City', 'Customer Demo', '+84901234567', 'DELIVERED', @order_1001_id),
('123 Brew Street, District 1, Ho Chi Minh City', 'Customer Demo', '+84901234567', 'SHIPPING', @order_1002_id),
('456 Roast Avenue, District 7, Ho Chi Minh City', 'Customer Demo Two', '+84908887766', 'DELIVERED', @order_2001_id);

INSERT INTO order_status_history (status, updated_at, order_id) VALUES
('CONFIRMED', NOW() - INTERVAL 11 DAY, @order_1001_id),
('DELIVERED', NOW() - INTERVAL 10 DAY, @order_1001_id),
('CONFIRMED', NOW() - INTERVAL 3 DAY, @order_1002_id),
('PROCESSING', NOW() - INTERVAL 2 DAY, @order_1002_id),
('CONFIRMED', NOW() - INTERVAL 7 DAY, @order_2001_id),
('DELIVERED', NOW() - INTERVAL 6 DAY, @order_2001_id);

INSERT INTO points_history (type, amount, description, order_id, created_at, customer_id) VALUES
('EARNED', 120, 'Points earned from order ORD-1001', @order_1001_id, NOW() - INTERVAL 10 DAY, @customer_demo_id),
('BONUS', 25, 'Birthday bonus', NULL, NOW() - INTERVAL 5 DAY, @customer_demo_id),
('EARNED', 95, 'Points earned from order ORD-2001', @order_2001_id, NOW() - INTERVAL 6 DAY, @customer_demo_2_id);

INSERT INTO tier_history (from_tier, to_tier, reason, changed_at, customer_id) VALUES
('BRONZE', 'GOLD', 'POINTS_EARNED', NOW() - INTERVAL 15 DAY, @customer_demo_id),
('BRONZE', 'BRONZE', 'MANUAL_ADJUSTMENT', NOW() - INTERVAL 20 DAY, @customer_demo_2_id);

SELECT id, username, email, full_name, status
FROM auth_db.users
ORDER BY id;

SELECT customer_id, customer_code, user_id
FROM customer_db.customers
ORDER BY customer_id;
