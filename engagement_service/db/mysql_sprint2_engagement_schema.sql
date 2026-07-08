CREATE DATABASE IF NOT EXISTS engagement_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE engagement_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS coupon_usages;
DROP TABLE IF EXISTS coupons;
DROP TABLE IF EXISTS promotions;
DROP TABLE IF EXISTS point_histories;
DROP TABLE IF EXISTS loyalty_balances;
DROP TABLE IF EXISTS tier_benefits;
DROP TABLE IF EXISTS tiers;
DROP TABLE IF EXISTS loyalty_configs;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE loyalty_configs (
  id BIGINT PRIMARY KEY,
  points_per_currency DECIMAL(19,8) NOT NULL,
  min_order_amount DECIMAL(19,2) NOT NULL,
  excluded_categories VARCHAR(1000) NULL,
  expiration_months INT NOT NULL,
  evaluation_period_months INT NOT NULL,
  inherit_from_lower_tiers TINYINT(1) NOT NULL DEFAULT 0
);

CREATE TABLE tiers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  min_points INT NOT NULL,
  max_points INT NULL
);

CREATE TABLE tier_benefits (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tier_id BIGINT NOT NULL,
  type VARCHAR(50) NOT NULL,
  value DECIMAL(10,2) NULL,
  description VARCHAR(255) NULL,
  CONSTRAINT fk_tier_benefits_tier
    FOREIGN KEY (tier_id) REFERENCES tiers(id)
);

CREATE TABLE loyalty_balances (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL UNIQUE,
  tier_id BIGINT NULL,
  current_points INT NOT NULL DEFAULT 0,
  pending_points INT NOT NULL DEFAULT 0,
  total_points_earned INT NULL DEFAULT 0,
  CONSTRAINT fk_loyalty_balances_tier
    FOREIGN KEY (tier_id) REFERENCES tiers(id)
);

CREATE TABLE point_histories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  type VARCHAR(50) NOT NULL,
  amount INT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  adjusted_by VARCHAR(255) NULL,
  order_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE promotions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT NULL,
  type VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  value DECIMAL(10,2) NOT NULL,
  start_date DATETIME NOT NULL,
  end_date DATETIME NOT NULL,
  is_featured TINYINT(1) NULL DEFAULT 0,
  min_order_amount DECIMAL(10,2) NULL,
  min_quantity INT NULL,
  applicable_products VARCHAR(1000) NULL,
  applicable_categories VARCHAR(1000) NULL,
  max_discount_amount DECIMAL(10,2) NULL,
  max_uses_total INT NULL,
  max_uses_per_customer INT NULL,
  target_segment_ids VARCHAR(1000) NULL,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coupons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(255) NOT NULL UNIQUE,
  promotion_id BIGINT NOT NULL,
  max_uses INT NULL,
  times_used INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  CONSTRAINT fk_coupons_promotion
    FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

CREATE TABLE coupon_usages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  coupon_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  discount_amount DECIMAL(10,2) NULL,
  used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_coupon_usages_coupon
    FOREIGN KEY (coupon_id) REFERENCES coupons(id)
);

CREATE INDEX idx_tiers_min_points ON tiers(min_points);
CREATE INDEX idx_point_histories_customer_created_at ON point_histories(customer_id, created_at);
CREATE INDEX idx_promotions_status_dates ON promotions(status, start_date, end_date);
CREATE INDEX idx_coupons_status ON coupons(status);

INSERT INTO loyalty_configs (
  id,
  points_per_currency,
  min_order_amount,
  excluded_categories,
  expiration_months,
  evaluation_period_months,
  inherit_from_lower_tiers
) VALUES
  (1, 0.00010000, 0.00, 'GIFT_CARD,MERCH', 12, 12, 1);

INSERT INTO tiers (id, name, min_points, max_points) VALUES
  (1, 'BRONZE', 0, 999),
  (2, 'SILVER', 1000, 4999),
  (3, 'GOLD', 5000, 9999),
  (4, 'PLATINUM', 10000, NULL);

INSERT INTO tier_benefits (tier_id, type, value, description) VALUES
  (1, 'DISCOUNT', 5.00, '5% off all orders'),
  (2, 'FREE_SHIPPING', 150000.00, 'Free shipping from 150,000 VND'),
  (2, 'BONUS_POINTS', 100.00, '100 bonus points on tier upgrade'),
  (3, 'DISCOUNT', 12.00, '12% off all orders'),
  (3, 'EXCLUSIVE_ACCESS', NULL, 'Early access to limited promotions'),
  (4, 'DISCOUNT', 20.00, '20% off all orders'),
  (4, 'GIFT', NULL, 'Monthly premium drink voucher');

INSERT INTO loyalty_balances (customer_id, tier_id, current_points, pending_points, total_points_earned) VALUES
  (101, 1, 320, 40, 320),
  (102, 2, 1450, 120, 1750),
  (103, 3, 6400, 0, 9100),
  (104, 4, 12050, 200, 15500);

INSERT INTO point_histories (customer_id, type, amount, reason, adjusted_by, order_id, created_at) VALUES
  (101, 'EARN', 120, 'Earned from order ORD-2026-001', NULL, 5001, '2026-03-12 09:15:00'),
  (101, 'BONUS', 200, 'Birthday bonus for March member', 'system', NULL, '2026-03-18 08:00:00'),
  (102, 'REDEEM', 300, 'Redeemed points for shipping reward', NULL, 5002, '2026-03-20 14:45:00'),
  (103, 'ADJUST', 250, 'Manual adjustment after complaint handling', 'admin@cfsm.local', NULL, '2026-03-22 16:30:00'),
  (104, 'EXPIRE', 150, 'Points expired after inactivity period', 'system', NULL, '2026-03-25 00:00:00');

INSERT INTO promotions (
  id,
  name,
  description,
  type,
  status,
  value,
  start_date,
  end_date,
  is_featured,
  min_order_amount,
  min_quantity,
  applicable_products,
  applicable_categories,
  max_discount_amount,
  max_uses_total,
  max_uses_per_customer,
  target_segment_ids,
  is_deleted,
  created_at,
  updated_at
) VALUES
  (1, 'Spring Gold Welcome', 'Featured promotion for loyal customers', 'PERCENTAGE_DISCOUNT', 'ACTIVE', 15.00, '2026-03-01 00:00:00', '2026-04-30 23:59:59', 1, 150000.00, 1, NULL, 'BEVERAGE,DESSERT', 50000.00, 1000, 2, 'gold-loyal,platinum-core', 0, '2026-03-01 09:00:00', '2026-03-01 09:00:00'),
  (2, 'Free Shipping Friday', 'Test promotion for coupon validation', 'FREE_SHIPPING', 'SCHEDULED', 0.00, '2026-04-05 00:00:00', '2026-04-30 23:59:59', 0, 99000.00, 1, NULL, 'DELIVERY', NULL, 500, 1, NULL, 0, '2026-03-15 10:00:00', '2026-03-15 10:00:00');

INSERT INTO coupons (id, code, promotion_id, max_uses, times_used, status) VALUES
  (1, 'SPRING-GOLD-2026', 1, 200, 14, 'ACTIVE'),
  (2, 'SHIP-FRIDAY-APR', 2, 500, 0, 'INACTIVE');

INSERT INTO coupon_usages (coupon_id, customer_id, order_id, discount_amount, used_at) VALUES
  (1, 103, 7001, 45000.00, '2026-03-10 11:12:00'),
  (1, 104, 7002, 50000.00, '2026-03-21 18:45:00');
