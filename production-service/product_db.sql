-- Database: production_db
-- --------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Table structure for table `categories`
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `is_active` BIT(1) NOT NULL,
  `deleted` BIT(1) NOT NULL,
  `description` VARCHAR(1000) DEFAULT NULL,
  `display_order` INT NOT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `name` VARCHAR(150) NOT NULL,
  `slug` VARCHAR(180) NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_categories_slug` (`slug`),
  KEY `FK_categories_parent_id` (`parent_id`),
  CONSTRAINT `FK_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 2. Table structure for table `ingredients`
DROP TABLE IF EXISTS `ingredients`;
CREATE TABLE `ingredients` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `is_active` BIT(1) NOT NULL,
  `cost_per_unit` DECIMAL(12,2) NOT NULL,
  `default_unit` VARCHAR(30) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `name` VARCHAR(150) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 3. Table structure for table `tags`
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `is_active` BIT(1) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `name` VARCHAR(100) NOT NULL,
  `slug` VARCHAR(120) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_tags_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 4. Table structure for table `products`
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `auto_unavailable_when_out_of_stock` BIT(1) NOT NULL,
  `is_available` BIT(1) NOT NULL,
  `available_from` DATE DEFAULT NULL,
  `available_until` DATE DEFAULT NULL,
  `deleted` BIT(1) NOT NULL,
  `description` VARCHAR(2000) DEFAULT NULL,
  `name` VARCHAR(180) NOT NULL,
  `preparation_time` INT DEFAULT NULL,
  `price` DECIMAL(12,2) NOT NULL,
  `sku` VARCHAR(80) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `category_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_products_sku` (`sku`),
  KEY `FK_products_category_id` (`category_id`),
  CONSTRAINT `FK_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 5. Table structure for table `product_images`
DROP TABLE IF EXISTS `product_images`;
CREATE TABLE `product_images` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `display_order` INT NOT NULL,
  `image_url` VARCHAR(500) NOT NULL,
  `large_url` VARCHAR(500) DEFAULT NULL,
  `medium_url` VARCHAR(500) DEFAULT NULL,
  `is_primary` BIT(1) NOT NULL,
  `thumbnail_url` VARCHAR(500) DEFAULT NULL,
  `product_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_product_images_product_id` (`product_id`),
  CONSTRAINT `FK_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 6. Table structure for table `product_ingredients`
DROP TABLE IF EXISTS `product_ingredients`;
CREATE TABLE `product_ingredients` (
  `product_id` BIGINT NOT NULL,
  `ingredient_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `quantity` DECIMAL(12,2) NOT NULL,
  `unit` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`ingredient_id`,`product_id`),
  KEY `FK_product_ingredients_product_id` (`product_id`),
  CONSTRAINT `FK_product_ingredients_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK_product_ingredients_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 7. Table structure for table `product_tags`
DROP TABLE IF EXISTS `product_tags`;
CREATE TABLE `product_tags` (
  `product_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`product_id`,`tag_id`),
  KEY `FK_product_tags_tag_id` (`tag_id`),
  CONSTRAINT `FK_product_tags_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK_product_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 8. Table structure for table `product_availabilities`
DROP TABLE IF EXISTS `product_availabilities`;
CREATE TABLE `product_availabilities` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `is_available` BIT(1) NOT NULL,
  `available_from` DATE DEFAULT NULL,
  `available_until` DATE DEFAULT NULL,
  `franchise_id` BIGINT NOT NULL,
  `price_override` DECIMAL(12,2) DEFAULT NULL,
  `product_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_product_availabilities_product_id` (`product_id`),
  CONSTRAINT `FK_product_availabilities_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- INSERT SAMPLE DATA
-- ==============================================================================

/* 1. categories */
INSERT INTO categories (id, name, slug, description, image_url, parent_id, display_order, is_active, deleted, created_at, updated_at) VALUES 
(1, 'Cà phê truyền thống', 'ca-phe-truyen-thong', 'Các loại cà phê pha phin truyền thống Việt Nam', 'https://placehold.co/400?text=Ca+Phe+Truyen+Thong', NULL, 1, b'1', b'0', NOW(), NOW()),
(2, 'Cà phê pha máy', 'ca-phe-pha-may', 'Espresso, Americano, Latte, Cappuccino', 'https://placehold.co/400?text=Ca+Phe+Pha+May', NULL, 2, b'1', b'0', NOW(), NOW()),
(3, 'Trà Trái Cây', 'tra-trai-cay', 'Trà đào, trà vải thanh mát', 'https://placehold.co/400?text=Tra+Trai+Cay', NULL, 3, b'1', b'0', NOW(), NOW());

/* 2. ingredients */
INSERT INTO ingredients (id, name, description, default_unit, cost_per_unit, is_active, created_at, updated_at) VALUES
(1, 'Hạt cà phê Robusta', 'Hạt cà phê nguyên chất 100%', 'g', 150.00, b'1', NOW(), NOW()),
(2, 'Sữa đặc', 'Sữa đặc có đường', 'ml', 50.00, b'1', NOW(), NOW()),
(3, 'Đường cát', 'Đường trắng', 'g', 20.00, b'1', NOW(), NOW());

/* 3. tags */
INSERT INTO tags (id, name, slug, description, is_active, created_at, updated_at) VALUES
(1, 'Bán chạy', 'ban-chay', 'Sản phẩm được yêu thích', b'1', NOW(), NOW()),
(2, 'Mới', 'moi', 'Sản phẩm mới ra mắt', b'1', NOW(), NOW());

/* 4. products */
INSERT INTO products (id, name, sku, description, price, category_id, is_available, preparation_time, status, auto_unavailable_when_out_of_stock, deleted, created_at, updated_at) VALUES
(1, 'Cà phê sữa đá', 'CFSD-001', 'Cà phê sữa đá pha phin đậm đà', 29000.00, 1, b'1', 5, 'ACTIVE', b'1', b'0', NOW(), NOW()),
(2, 'Cà phê đen đá', 'CFDD-001', 'Cà phê đen nguyên chất không đường', 25000.00, 1, b'1', 5, 'ACTIVE', b'1', b'0', NOW(), NOW()),
(3, 'Latte Macchiato', 'LATM-001', 'Latte nóng thơm lừng', 55000.00, 2, b'1', 10, 'ACTIVE', b'1', b'0', NOW(), NOW()),
(4, 'Trà Đào Cam Sả', 'TDCS-001', 'Trà đào thanh mát giải nhiệt', 35000.00, 3, b'1', 5, 'ACTIVE', b'1', b'0', NOW(), NOW());

/* 5. product_images */
INSERT INTO product_images (id, product_id, image_url, thumbnail_url, medium_url, large_url, is_primary, display_order, created_at, updated_at) VALUES
(1, 1, 'https://placehold.co/800x800?text=SUA+DA', 'https://placehold.co/150x150?text=SUA+DA', 'https://placehold.co/500x500?text=SUA+DA', 'https://placehold.co/1000x1000?text=SUA+DA', b'1', 1, NOW(), NOW()),
(2, 2, 'https://placehold.co/800x800?text=DEN+DA', 'https://placehold.co/150x150?text=DEN+DA', 'https://placehold.co/500x500?text=DEN+DA', 'https://placehold.co/1000x1000?text=DEN+DA', b'1', 1, NOW(), NOW()),
(3, 3, 'https://placehold.co/800x800?text=LATTE', 'https://placehold.co/150x150?text=LATTE', 'https://placehold.co/500x500?text=LATTE', 'https://placehold.co/1000x1000?text=LATTE', b'1', 1, NOW(), NOW());

/* 6. product_ingredients */
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, unit, created_at, updated_at) VALUES
(1, 1, 20.00, 'g', NOW(), NOW()), 
(1, 2, 30.00, 'ml', NOW(), NOW()), 
(2, 1, 25.00, 'g', NOW(), NOW()); 

/* 7. product_tags */
INSERT INTO product_tags (product_id, tag_id) VALUES
(1, 1),
(3, 2),
(4, 1);

/* 8. product_availabilities */
INSERT INTO product_availabilities (id, product_id, franchise_id, is_available, price_override, created_at, updated_at) VALUES
(1, 1, 1, b'1', NULL, NOW(), NOW()),
(2, 2, 1, b'1', NULL, NOW(), NOW()),
(3, 3, 1, b'1', NULL, NOW(), NOW()),
(4, 4, 1, b'1', NULL, NOW(), NOW());

-- 9. Table structure for table `product_reviews`
DROP TABLE IF EXISTS `product_reviews`;
CREATE TABLE `product_reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `user_name` VARCHAR(150) NOT NULL,
  `rating` INT NOT NULL,
  `comment` VARCHAR(2000) DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_reviews_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO product_reviews (id, product_id, user_id, user_name, rating, comment, created_at, updated_at) VALUES
(1, 1, 1, 'Nguyễn Văn A', 5, 'Cà phê sữa đá ngon tuyệt vời, vị đậm đà đúng chất phin!', NOW(), NOW()),
(2, 1, 2, 'Trần Thị B', 4, 'Hơi ngọt một chút so với khẩu vị của mình nhưng vẫn rất ngon và thơm.', NOW(), NOW()),
(3, 3, 3, 'Lê Văn C', 5, 'Latte Macchiato thơm ngậy béo ngậy, bọt sữa siêu mịn luôn.', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

