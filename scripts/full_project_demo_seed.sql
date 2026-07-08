-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: customer_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `customer_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `customer_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `customer_db`;

--
-- Table structure for table `address_orders`
--

DROP TABLE IF EXISTS `address_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address_orders` (
  `address_id` bigint NOT NULL AUTO_INCREMENT,
  `address_line` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`address_id`),
  KEY `fk_address_orders_customer` (`customer_id`),
  CONSTRAINT `fk_address_orders_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address_orders`
--

LOCK TABLES `address_orders` WRITE;
/*!40000 ALTER TABLE `address_orders` DISABLE KEYS */;
INSERT INTO `address_orders` VALUES (1,'123 Nguyen Hue, Ben Nghe Ward, District 1, Ho Chi Minh City',1,1001),(2,'Tower B, 115 Le Thanh Ton, District 1, Ho Chi Minh City',0,1001),(3,'88 Tan Quy Street, Tan Phong Ward, District 7, Ho Chi Minh City',1,1002),(4,'12 Masteri Riverside, An Phu Ward, Thu Duc City, Ho Chi Minh City',1,1003),(5,'Floor 18, 2 Nguyen Huu Canh, Binh Thanh, Ho Chi Minh City',0,1003),(6,'15 Vo Van Tan, Ward 6, District 3, Ho Chi Minh City',1,1004),(7,'29 Nguyen Cuu Van, Ward 17, Binh Thanh, Ho Chi Minh City',1,1005),(8,'41 Truong Chinh, Ward 12, Tan Binh, Ho Chi Minh City',1,1006),(9,'7 Hoa Su, Ward 7, Phu Nhuan, Ho Chi Minh City',1,1007),(10,'18 Tran Nao, An Khanh Ward, Thu Duc City, Ho Chi Minh City',1,1008);
/*!40000 ALTER TABLE `address_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_segments`
--

DROP TABLE IF EXISTS `customer_segments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_segments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logic_operator` varchar(5) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_spent` double DEFAULT NULL,
  `condition_spent` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_count` int DEFAULT NULL,
  `condition_count` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_order_date` date DEFAULT NULL,
  `condition_date` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loyalty_tier` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `condition_tier` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `condition_location` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_segments`
--

LOCK TABLES `customer_segments` WRITE;
/*!40000 ALTER TABLE `customer_segments` DISABLE KEYS */;
INSERT INTO `customer_segments` VALUES (1,'VIP Loyalists','High-spend premium customers with recent orders','AND',800000,'GTE',3,'GTE','2026-03-01','GTE','GOLD','GTE','Ho Chi Minh City','LIKE','2026-03-01 09:00:00','2026-03-31 09:00:00'),(2,'New Bronze Shoppers','Recently joined bronze customers with low order count','AND',0,'GTE',2,'LTE','2026-03-01','GTE','BRONZE','EQ','Ho Chi Minh City','LIKE','2026-03-05 09:00:00','2026-03-31 09:00:00'),(3,'District 1 Regulars','Frequent customers in central district','AND',150000,'GTE',2,'GTE','2026-02-01','GTE',NULL,NULL,'District 1','LIKE','2026-03-10 10:00:00','2026-03-31 10:00:00'),(4,'At Risk Premium','High-value customers without a very recent completed order','AND',500000,'GTE',2,'GTE','2026-02-15','LTE','GOLD','GTE','Thu Duc','LIKE','2026-03-12 11:00:00','2026-03-31 11:00:00');
/*!40000 ALTER TABLE `customer_segments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `loyalty_tier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `customer_code` (`customer_code`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1009 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1001,'CUST-1001',201,'BRONZE',NULL),(1002,'CUST-1002',202,'SILVER',NULL),(1003,'CUST-1003',203,'PLATINUM',NULL),(1004,'CUST-1004',204,'GOLD',NULL),(1005,'CUST-1005',205,'SILVER',NULL),(1006,'CUST-1006',206,'BRONZE',NULL),(1007,'CUST-1007',207,'BRONZE',NULL),(1008,'CUST-1008',208,'GOLD',NULL);
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyalty`
--

DROP TABLE IF EXISTS `loyalty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyalty` (
  `loyalty_id` bigint NOT NULL AUTO_INCREMENT,
  `current_points` int NOT NULL DEFAULT '0',
  `lifetime_points` int NOT NULL DEFAULT '0',
  `current_tier` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enrolled_at` datetime DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `last_earned_at` datetime DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  PRIMARY KEY (`loyalty_id`),
  UNIQUE KEY `customer_id` (`customer_id`),
  CONSTRAINT `fk_loyalty_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyalty`
--

LOCK TABLES `loyalty` WRITE;
/*!40000 ALTER TABLE `loyalty` DISABLE KEYS */;
INSERT INTO `loyalty` VALUES (1,341,821,'BRONZE','2026-01-15 10:30:00','2026-03-24 18:50:00','2026-03-24 18:50:00',1001),(2,1820,2460,'SILVER','2026-01-16 11:10:00','2026-03-30 10:40:00','2026-03-30 10:40:00',1002),(3,12480,16650,'PLATINUM','2026-01-10 08:10:00','2026-03-26 19:20:00','2026-03-26 19:20:00',1003),(4,7280,9850,'GOLD','2026-01-18 09:30:00','2026-03-21 17:55:00','2026-03-21 17:55:00',1004),(5,2480,3020,'SILVER','2026-01-20 10:50:00','2026-03-11 11:35:00','2026-03-11 11:35:00',1005),(6,420,420,'BRONZE','2026-01-22 09:30:00','2026-03-09 08:05:00','2026-03-09 08:05:00',1006),(7,80,80,'BRONZE','2026-03-10 08:30:00','2026-03-31 20:20:00','2026-03-31 20:20:00',1007),(8,5120,7440,'GOLD','2026-01-25 13:10:00','2026-02-08 18:30:00','2026-01-20 15:50:00',1008);
/*!40000 ALTER TABLE `loyalty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_deliveries`
--

DROP TABLE IF EXISTS `order_deliveries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_deliveries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  CONSTRAINT `fk_order_deliveries_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_deliveries`
--

LOCK TABLES `order_deliveries` WRITE;
/*!40000 ALTER TABLE `order_deliveries` DISABLE KEYS */;
INSERT INTO `order_deliveries` VALUES (1,'123 Nguyen Hue, Ben Nghe Ward, District 1, Ho Chi Minh City','Nguyen Minh Anh','0901234567','DELIVERED',5001),(2,'123 Nguyen Hue, Ben Nghe Ward, District 1, Ho Chi Minh City','Nguyen Minh Anh','0901234567','DELIVERED',5002),(3,'88 Tan Quy Street, Tan Phong Ward, District 7, Ho Chi Minh City','Tran Hoang Nam','0908877766','DELIVERED',5003),(4,'88 Tan Quy Street, Tan Phong Ward, District 7, Ho Chi Minh City','Tran Hoang Nam','0908877766','DELIVERED',5004),(5,'88 Tan Quy Street, Tan Phong Ward, District 7, Ho Chi Minh City','Tran Hoang Nam','0908877766','SHIPPING',5005),(6,'12 Masteri Riverside, An Phu Ward, Thu Duc City, Ho Chi Minh City','Le Bao Chau','0907666888','DELIVERED',5006),(7,'12 Masteri Riverside, An Phu Ward, Thu Duc City, Ho Chi Minh City','Le Bao Chau','0907666888','DELIVERED',5007),(8,'12 Masteri Riverside, An Phu Ward, Thu Duc City, Ho Chi Minh City','Le Bao Chau','0907666888','DELIVERED',5008),(9,'15 Vo Van Tan, Ward 6, District 3, Ho Chi Minh City','Pham Duc Khang','0904567788','DELIVERED',5009),(10,'15 Vo Van Tan, Ward 6, District 3, Ho Chi Minh City','Pham Duc Khang','0904567788','DELIVERED',5010),(11,'29 Nguyen Cuu Van, Ward 17, Binh Thanh, Ho Chi Minh City','Do Thu Ha','0903456677','DELIVERED',5011),(12,'29 Nguyen Cuu Van, Ward 17, Binh Thanh, Ho Chi Minh City','Do Thu Ha','0903456677','CANCELLED',5012),(13,'41 Truong Chinh, Ward 12, Tan Binh, Ho Chi Minh City','Vu Anh Tuan','0903344556','DELIVERED',5013),(14,'7 Hoa Su, Ward 7, Phu Nhuan, Ho Chi Minh City','Bui Gia Linh','0901122334','PENDING',5014),(15,'18 Tran Nao, An Khanh Ward, Thu Duc City, Ho Chi Minh City','Nguyen Hai Dang','0909988776','DELIVERED',5015),(16,'18 Tran Nao, An Khanh Ward, Thu Duc City, Ho Chi Minh City','Nguyen Hai Dang','0909988776','CANCELLED',5016);
/*!40000 ALTER TABLE `order_deliveries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT,
  `product` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `fk_order_items_order` (`order_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,'House Espresso',2,42000.00,5001),(2,'Butter Croissant',1,48000.00,5001),(3,'Coconut Cold Brew',2,69000.00,5002),(4,'Chocolate Danish',1,51000.00,5002),(5,'Oat Milk Latte',1,72000.00,5003),(6,'Peach Oolong Tea',1,53000.00,5003),(7,'Smoked Chicken Sandwich',1,88000.00,5003),(8,'Saigon Americano',1,48000.00,5004),(9,'Butter Croissant',1,48000.00,5004),(10,'Yuzu Sparkling',1,59000.00,5004),(11,'Tiramisu Latte',2,79000.00,5005),(12,'Smoked Chicken Sandwich',1,88000.00,5005),(13,'Chocolate Danish',1,51000.00,5005),(14,'Oat Milk Latte',2,72000.00,5006),(15,'Ceramic Tumbler',1,189000.00,5006),(16,'Butter Croissant',1,48000.00,5006),(17,'Tiramisu Latte',2,79000.00,5007),(18,'Coconut Cold Brew',2,69000.00,5007),(19,'Smoked Chicken Sandwich',1,88000.00,5007),(20,'Matcha Cloud Latte',2,76000.00,5008),(21,'Yuzu Sparkling',1,59000.00,5008),(22,'Butter Croissant',1,48000.00,5008),(23,'Saigon Americano',1,48000.00,5009),(24,'Oat Milk Latte',1,72000.00,5009),(25,'Smoked Chicken Sandwich',1,88000.00,5009),(26,'Matcha Cloud Latte',2,76000.00,5010),(27,'Chocolate Danish',1,51000.00,5010),(28,'Yuzu Sparkling',2,59000.00,5010),(29,'Saigon Americano',2,48000.00,5011),(30,'Butter Croissant',1,48000.00,5011),(31,'Peach Oolong Tea',1,53000.00,5011),(32,'House Espresso',1,42000.00,5012),(33,'Peach Oolong Tea',1,53000.00,5012),(34,'Butter Croissant',1,48000.00,5012),(35,'House Espresso',1,42000.00,5013),(36,'Peach Oolong Tea',1,53000.00,5013),(37,'Butter Croissant',1,48000.00,5013),(38,'Peach Oolong Tea',1,53000.00,5014),(39,'Butter Croissant',1,48000.00,5014),(40,'Tiramisu Latte',1,79000.00,5015),(41,'Ceramic Tumbler',1,189000.00,5015),(42,'Smoked Chicken Sandwich',1,88000.00,5016),(43,'Matcha Cloud Latte',1,76000.00,5016),(44,'Yuzu Sparkling',1,59000.00,5016);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_payments`
--

DROP TABLE IF EXISTS `order_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `method` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  CONSTRAINT `fk_order_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_payments`
--

LOCK TABLES `order_payments` WRITE;
/*!40000 ALTER TABLE `order_payments` DISABLE KEYS */;
INSERT INTO `order_payments` VALUES (1,'CARD','PAID','2026-02-18 09:15:00',5001),(2,'EWALLET','PAID','2026-03-24 18:43:00',5002),(3,'CARD','PAID','2026-03-05 08:31:00',5003),(4,'CASH','PAID','2026-03-18 12:26:00',5004),(5,'EWALLET','PAID','2026-03-30 10:11:00',5005),(6,'CARD','PAID','2026-02-27 14:17:00',5006),(7,'CARD','PAID','2026-03-12 16:31:00',5007),(8,'EWALLET','PAID','2026-03-26 19:06:00',5008),(9,'CARD','PAID','2026-03-07 09:46:00',5009),(10,'EWALLET','PAID','2026-03-21 17:41:00',5010),(11,'CASH','PAID','2026-03-11 11:23:00',5011),(12,'CARD','REFUNDED','2026-03-29 08:52:00',5012),(13,'EWALLET','PAID','2026-03-09 07:57:00',5013),(14,'CARD','PENDING',NULL,5014),(15,'CARD','PAID','2026-01-20 15:36:00',5015),(16,'EWALLET','REFUNDED','2026-02-08 18:20:00',5016);
/*!40000 ALTER TABLE `order_payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_status_history`
--

DROP TABLE IF EXISTS `order_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_status_history_order` (`order_id`),
  CONSTRAINT `fk_order_status_history_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_status_history`
--

LOCK TABLES `order_status_history` WRITE;
/*!40000 ALTER TABLE `order_status_history` DISABLE KEYS */;
INSERT INTO `order_status_history` VALUES (1,'PENDING','2026-02-18 09:12:00',5001),(2,'SHIPPING','2026-02-18 09:40:00',5001),(3,'DELIVERED','2026-02-18 10:25:00',5001),(4,'PENDING','2026-03-24 18:42:00',5002),(5,'SHIPPING','2026-03-24 19:10:00',5002),(6,'DELIVERED','2026-03-24 19:55:00',5002),(7,'PENDING','2026-03-05 08:30:00',5003),(8,'SHIPPING','2026-03-05 08:55:00',5003),(9,'DELIVERED','2026-03-05 09:40:00',5003),(10,'PENDING','2026-03-18 12:25:00',5004),(11,'SHIPPING','2026-03-18 12:55:00',5004),(12,'DELIVERED','2026-03-18 13:35:00',5004),(13,'PENDING','2026-03-30 10:10:00',5005),(14,'SHIPPING','2026-03-30 10:45:00',5005),(15,'PENDING','2026-02-27 14:15:00',5006),(16,'SHIPPING','2026-02-27 14:55:00',5006),(17,'DELIVERED','2026-02-27 15:50:00',5006),(18,'PENDING','2026-03-12 16:30:00',5007),(19,'SHIPPING','2026-03-12 17:00:00',5007),(20,'DELIVERED','2026-03-12 17:50:00',5007),(21,'PENDING','2026-03-26 19:05:00',5008),(22,'SHIPPING','2026-03-26 19:35:00',5008),(23,'DELIVERED','2026-03-26 20:20:00',5008),(24,'PENDING','2026-03-07 09:45:00',5009),(25,'DELIVERED','2026-03-07 10:20:00',5009),(26,'PENDING','2026-03-21 17:40:00',5010),(27,'SHIPPING','2026-03-21 18:10:00',5010),(28,'DELIVERED','2026-03-21 19:00:00',5010),(29,'PENDING','2026-03-11 11:22:00',5011),(30,'DELIVERED','2026-03-11 12:05:00',5011),(31,'PENDING','2026-03-29 08:50:00',5012),(32,'CANCELLED','2026-03-29 09:05:00',5012),(33,'PENDING','2026-03-09 07:55:00',5013),(34,'DELIVERED','2026-03-09 08:35:00',5013),(35,'PENDING','2026-03-31 20:10:00',5014),(36,'PENDING','2026-01-20 15:35:00',5015),(37,'SHIPPING','2026-01-20 16:00:00',5015),(38,'DELIVERED','2026-01-20 16:45:00',5015),(39,'PENDING','2026-02-08 18:18:00',5016),(40,'CANCELLED','2026-02-08 18:35:00',5016);
/*!40000 ALTER TABLE `order_status_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `final_amount` decimal(38,2) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `fk_orders_customer` (`customer_id`),
  CONSTRAINT `fk_orders_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5017 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (5001,'ORD-2026-5001','DELIVERED',132000.00,0.00,132000.00,'2026-02-18 09:12:00',1001),(5002,'ORD-2026-5002','DELIVERED',189000.00,20000.00,169000.00,'2026-03-24 18:42:00',1001),(5003,'ORD-2026-5003','DELIVERED',213000.00,23000.00,190000.00,'2026-03-05 08:30:00',1002),(5004,'ORD-2026-5004','DELIVERED',155000.00,0.00,155000.00,'2026-03-18 12:25:00',1002),(5005,'ORD-2026-5005','SHIPPING',297000.00,30000.00,267000.00,'2026-03-30 10:10:00',1002),(5006,'ORD-2026-5006','DELIVERED',381000.00,50000.00,331000.00,'2026-02-27 14:15:00',1003),(5007,'ORD-2026-5007','DELIVERED',384000.00,60000.00,324000.00,'2026-03-12 16:30:00',1003),(5008,'ORD-2026-5008','DELIVERED',259000.00,45000.00,214000.00,'2026-03-26 19:05:00',1003),(5009,'ORD-2026-5009','DELIVERED',208000.00,15000.00,193000.00,'2026-03-07 09:45:00',1004),(5010,'ORD-2026-5010','DELIVERED',321000.00,40000.00,281000.00,'2026-03-21 17:40:00',1004),(5011,'ORD-2026-5011','DELIVERED',197000.00,0.00,197000.00,'2026-03-11 11:22:00',1005),(5012,'ORD-2026-5012','CANCELLED',143000.00,0.00,143000.00,'2026-03-29 08:50:00',1005),(5013,'ORD-2026-5013','DELIVERED',143000.00,0.00,143000.00,'2026-03-09 07:55:00',1006),(5014,'ORD-2026-5014','PENDING',101000.00,0.00,101000.00,'2026-03-31 20:10:00',1007),(5015,'ORD-2026-5015','DELIVERED',268000.00,30000.00,238000.00,'2026-01-20 15:35:00',1008),(5016,'ORD-2026-5016','CANCELLED',223000.00,20000.00,203000.00,'2026-02-08 18:18:00',1008);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `points_history`
--

DROP TABLE IF EXISTS `points_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` int NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `fk_points_history_customer` (`customer_id`),
  CONSTRAINT `fk_points_history_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_history`
--

LOCK TABLES `points_history` WRITE;
/*!40000 ALTER TABLE `points_history` DISABLE KEYS */;
INSERT INTO `points_history` VALUES (1,'EARNED',132,'Points from delivered order ORD-2026-5001','ORD-2026-5001','2026-02-18 10:30:00',1001),(2,'BONUS',40,'Completed profile bonus',NULL,'2026-02-20 09:00:00',1001),(3,'EARNED',169,'Points from delivered order ORD-2026-5002','ORD-2026-5002','2026-03-24 20:00:00',1001),(4,'EARNED',190,'Points from delivered order ORD-2026-5003','ORD-2026-5003','2026-03-05 10:00:00',1002),(5,'EARNED',155,'Points from delivered order ORD-2026-5004','ORD-2026-5004','2026-03-18 14:00:00',1002),(6,'BONUS',120,'March challenge completion',NULL,'2026-03-29 21:00:00',1002),(7,'EARNED',331,'Points from delivered order ORD-2026-5006','ORD-2026-5006','2026-02-27 16:00:00',1003),(8,'EARNED',324,'Points from delivered order ORD-2026-5007','ORD-2026-5007','2026-03-12 18:00:00',1003),(9,'REDEEMED',500,'Redeemed for premium tasting workshop',NULL,'2026-03-20 10:30:00',1003),(10,'EARNED',193,'Points from delivered order ORD-2026-5009','ORD-2026-5009','2026-03-07 11:00:00',1004),(11,'ADJUSTED',200,'Manual service recovery adjustment',NULL,'2026-03-22 09:45:00',1004),(12,'EARNED',281,'Points from delivered order ORD-2026-5010','ORD-2026-5010','2026-03-21 19:15:00',1004),(13,'EARNED',197,'Points from delivered order ORD-2026-5011','ORD-2026-5011','2026-03-11 12:10:00',1005),(14,'EARNED',143,'Points from delivered order ORD-2026-5013','ORD-2026-5013','2026-03-09 08:45:00',1006),(15,'EARNED',101,'Pending order points snapshot','ORD-2026-5014','2026-03-31 20:30:00',1007),(16,'EARNED',238,'Points from delivered order ORD-2026-5015','ORD-2026-5015','2026-01-20 17:00:00',1008),(17,'EXPIRED',300,'Inactivity expiration during annual review',NULL,'2026-03-01 00:05:00',1008);
/*!40000 ALTER TABLE `points_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `segment_customers`
--

DROP TABLE IF EXISTS `segment_customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `segment_customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `segment_id` bigint DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_segment_customers_segment` (`segment_id`),
  KEY `fk_segment_customers_customer` (`customer_id`),
  CONSTRAINT `fk_segment_customers_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_segment_customers_segment` FOREIGN KEY (`segment_id`) REFERENCES `segments` (`segment_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `segment_customers`
--

LOCK TABLES `segment_customers` WRITE;
/*!40000 ALTER TABLE `segment_customers` DISABLE KEYS */;
INSERT INTO `segment_customers` VALUES (1,301,1003),(2,301,1004),(3,301,1008),(4,302,1001),(5,302,1006),(6,302,1007),(7,303,1008);
/*!40000 ALTER TABLE `segment_customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `segments`
--

DROP TABLE IF EXISTS `segments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `segments` (
  `segment_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `criteria` json DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`segment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=304 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `segments`
--

LOCK TABLES `segments` WRITE;
/*!40000 ALTER TABLE `segments` DISABLE KEYS */;
INSERT INTO `segments` VALUES (301,'VIP Loyalists','Legacy segment for premium loyal customers','{\"tier\": {\"value\": \"GOLD\", \"operator\": \"GTE\"}, \"orderCount\": {\"value\": 3, \"operator\": \"GTE\"}, \"totalSpent\": {\"value\": 800000, \"operator\": \"GTE\"}, \"logicOperator\": \"AND\"}','2026-03-01 09:00:00'),(302,'New Customers','Legacy segment for recently joined customers','{\"tier\": {\"value\": \"BRONZE\", \"operator\": \"EQ\"}, \"orderCount\": {\"value\": 2, \"operator\": \"LTE\"}, \"logicOperator\": \"AND\"}','2026-03-05 09:00:00'),(303,'Churn Watch','Legacy segment for premium customers with stale recent activity','{\"totalSpent\": {\"value\": 500000, \"operator\": \"GTE\"}, \"lastOrderDate\": {\"value\": \"2026-02-15\", \"operator\": \"LTE\"}, \"logicOperator\": \"AND\"}','2026-03-12 11:00:00');
/*!40000 ALTER TABLE `segments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tier_history`
--

DROP TABLE IF EXISTS `tier_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tier_history` (
  `tier_history_id` bigint NOT NULL AUTO_INCREMENT,
  `from_tier` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `to_tier` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reason` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `changed_at` datetime NOT NULL,
  `customer_id` bigint NOT NULL,
  PRIMARY KEY (`tier_history_id`),
  KEY `fk_customer_tier_history_customer` (`customer_id`),
  CONSTRAINT `fk_customer_tier_history_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tier_history`
--

LOCK TABLES `tier_history` WRITE;
/*!40000 ALTER TABLE `tier_history` DISABLE KEYS */;
INSERT INTO `tier_history` VALUES (1,'BRONZE','SILVER','POINTS_EARNED','2026-02-15 09:00:00',1002),(2,'GOLD','PLATINUM','ANNUAL_EVALUATION','2026-01-05 08:30:00',1003),(3,'SILVER','GOLD','POINTS_EARNED','2026-03-01 10:00:00',1004),(4,'BRONZE','SILVER','MANUAL_ADJUSTMENT','2026-02-28 15:20:00',1005),(5,'SILVER','GOLD','POINTS_EARNED','2025-11-10 09:00:00',1008);
/*!40000 ALTER TABLE `tier_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `auth_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `auth_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `auth_db`;

--
-- Table structure for table `email_change_otps`
--

DROP TABLE IF EXISTS `email_change_otps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_change_otps` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `new_email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pending_username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `otp_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_email_change_otps_user` (`user_id`),
  KEY `idx_email_change_otps_expires` (`expires_at`),
  CONSTRAINT `fk_email_change_otps_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_change_otps`
--

LOCK TABLES `email_change_otps` WRITE;
/*!40000 ALTER TABLE `email_change_otps` DISABLE KEYS */;
INSERT INTO `email_change_otps` VALUES (1,202,'tran.hoangnam.new@example.com','customer_demo_2','1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef','2026-04-01 23:00:00',0,'2026-04-01 20:00:00');
/*!40000 ALTER TABLE `email_change_otps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `otps`
--

DROP TABLE IF EXISTS `otps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otps` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(320) COLLATE utf8mb4_unicode_ci NOT NULL,
  `otp_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  `failed_attempt_count` int NOT NULL DEFAULT '0',
  `last_failed_at` datetime DEFAULT NULL,
  `lock_until` datetime DEFAULT NULL,
  `verified_at` datetime DEFAULT NULL,
  `last_sent_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `uk_otps_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otps`
--

LOCK TABLES `otps` WRITE;
/*!40000 ALTER TABLE `otps` DISABLE KEYS */;
INSERT INTO `otps` VALUES (1,'customer.signup.pending@example.com','fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321','2026-04-01 22:30:00',0,NULL,NULL,NULL,'2026-04-01 22:00:00','2026-04-01 22:00:00','2026-04-01 22:00:00');
/*!40000 ALTER TABLE `otps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_requests`
--

DROP TABLE IF EXISTS `password_reset_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_requests`
--

LOCK TABLES `password_reset_requests` WRITE;
/*!40000 ALTER TABLE `password_reset_requests` DISABLE KEYS */;
INSERT INTO `password_reset_requests` VALUES (1,'customer.new@example.com','2026-03-29 21:10:00'),(2,'support.agent@example.com','2026-03-18 08:00:00');
/*!40000 ALTER TABLE `password_reset_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint(1) NOT NULL DEFAULT '0',
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_hash` (`token_hash`),
  KEY `fk_password_reset_tokens_user` (`user_id`),
  KEY `idx_password_reset_tokens_expires` (`expires_at`),
  CONSTRAINT `fk_password_reset_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_tokens`
--

LOCK TABLES `password_reset_tokens` WRITE;
/*!40000 ALTER TABLE `password_reset_tokens` DISABLE KEYS */;
INSERT INTO `password_reset_tokens` VALUES (1,'aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899',207,'2026-04-02 21:10:00',0,NULL,'2026-04-01 09:00:00'),(2,'11223344556677889900aabbccddeeff11223344556677889900aabbccddeeff',5,'2026-03-20 09:00:00',1,'2026-03-19 10:30:00','2026-03-19 09:00:00');
/*!40000 ALTER TABLE `password_reset_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resource` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `uk_permissions_resource_action` (`resource`,`action`),
  UNIQUE KEY `UKthe30sacgvpn9p7s35n6c18ux` (`resource`,`action`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES (1,'USER_VIEW','USER','VIEW','View users'),(2,'USER_CREATE','USER','CREATE','Create users'),(3,'USER_UPDATE','USER','UPDATE','Update users'),(4,'USER_DELETE','USER','DELETE','Delete users'),(5,'USER_LOCK','USER','LOCK','Lock or unlock users'),(6,'ROLE:READ','ROLE','READ','View roles'),(7,'ROLE:CREATE','ROLE','CREATE','Create roles'),(8,'ROLE:UPDATE','ROLE','UPDATE','Update roles'),(9,'ROLE:DELETE','ROLE','DELETE','Delete roles'),(10,'PERMISSION:READ','PERMISSION','READ','View permissions'),(11,'PERMISSION:CREATE','PERMISSION','CREATE','Create permissions'),(12,'PERMISSION:UPDATE','PERMISSION','UPDATE','Update permissions'),(13,'PERMISSION:DELETE','PERMISSION','DELETE','Delete permissions'),(14,'CUSTOMER:READ','CUSTOMER','READ','View customer profiles'),(15,'SEGMENT:CREATE','SEGMENT','CREATE','Create customer segments'),(16,'SEGMENT:READ','SEGMENT','READ','View customer segments'),(17,'SEGMENT:UPDATE','SEGMENT','UPDATE','Update customer segments'),(18,'SEGMENT:DELETE','SEGMENT','DELETE','Delete customer segments'),(19,'LOYALTY:CONFIG','LOYALTY','CONFIG','Manage loyalty configuration'),(20,'LOYALTY:MANAGE_BENEFITS','LOYALTY','MANAGE_BENEFITS','Manage loyalty tier benefits'),(21,'LOYALTY:ADJUST_POINTS','LOYALTY','ADJUST_POINTS','Adjust loyalty points'),(22,'LOYALTY:REPORT','LOYALTY','REPORT','View loyalty analytics reports'),(23,'PROMOTION:CREATE','PROMOTION','CREATE','Create promotions'),(24,'PROMOTION:READ','PROMOTION','READ','View promotions'),(25,'PROMOTION:UPDATE','PROMOTION','UPDATE','Update promotions'),(26,'PROMOTION:DELETE','PROMOTION','DELETE','Delete promotions'),(27,'PROMOTION:REPORT','PROMOTION','REPORT','View promotion analytics reports'),(28,'COUPON_CREATE','COUPON','CREATE','Create coupons'),(29,'COUPON_UPDATE','COUPON','UPDATE','Update coupons'),(30,'COUPON_DEACTIVATE','COUPON','DEACTIVATE','Deactivate coupons'),(31,'PRODUCT:CREATE','PRODUCT','CREATE','Create products'),(32,'PRODUCT:READ','PRODUCT','READ','View products'),(33,'PRODUCT:UPDATE','PRODUCT','UPDATE','Update products'),(34,'PRODUCT:DELETE','PRODUCT','DELETE','Delete products'),(35,'CATEGORY:CREATE','CATEGORY','CREATE','Create categories'),(36,'CATEGORY:READ','CATEGORY','READ','View categories'),(37,'CATEGORY:UPDATE','CATEGORY','UPDATE','Update categories'),(38,'CATEGORY:DELETE','CATEGORY','DELETE','Delete categories');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_refresh_tokens_token` (`token`(255)),
  KEY `idx_refresh_tokens_user` (`user_id`),
  KEY `idx_refresh_tokens_expires` (`expires_at`),
  CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
INSERT INTO `refresh_tokens` VALUES (1,'demo-refresh-admin-001',1,'2026-04-07 09:00:00','2026-03-31 09:00:00'),(2,'demo-refresh-customer-001',201,'2026-04-07 20:00:00','2026-03-31 20:00:00'),(3,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc3NTM2ODA0NSwiZXhwIjoxNzc1OTcyODQ1LCJ1c2VySWQiOjEsImVtYWlsIjoiYWRtaW5AZ21haWwuY29tIiwiZnVsbE5hbWUiOiJTeXN0ZW0gQWRtaW4ifQ.lIyLpHy7l1VcS-TXFbBZoBAo5Vd34eEbPIJjpzCmWAw',1,'2026-04-12 12:47:26','2026-04-05 12:47:26'),(4,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc3NTM2ODQ3MCwiZXhwIjoxNzc1OTczMjcwLCJ1c2VySWQiOjEsImVtYWlsIjoiYWRtaW5AZ21haWwuY29tIiwiZnVsbE5hbWUiOiJTeXN0ZW0gQWRtaW4ifQ.Oi2pc6WO48rEIZHhOnXw_SAhkswth8s1zzgb9FdNOPY',1,'2026-04-12 12:54:31','2026-04-05 12:54:31'),(5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcl9kZW1vIiwiaWF0IjoxNzc1MzY4ODY5LCJleHAiOjE3NzU5NzM2NjksInVzZXJJZCI6MjAxLCJlbWFpbCI6ImN1c3RvbWVyLmRlbW9AZXhhbXBsZS5jb20iLCJmdWxsTmFtZSI6Ik5ndXllbiBNaW5oIEFuaCJ9.Ap8zN8ioFa1cFhJLcvM8OAP5Yp4K-SKh9VNk0hrUQ00',201,'2026-04-12 13:01:09','2026-04-05 13:01:09');
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `revoked_tokens`
--

DROP TABLE IF EXISTS `revoked_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `revoked_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiry_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_revoked_tokens_token` (`token`(255)),
  KEY `idx_revoked_tokens_expiry` (`expiry_date`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `revoked_tokens`
--

LOCK TABLES `revoked_tokens` WRITE;
/*!40000 ALTER TABLE `revoked_tokens` DISABLE KEYS */;
INSERT INTO `revoked_tokens` VALUES (1,'demo-revoked-token-admin-202603','2026-04-15 23:59:59'),(2,'demo-revoked-token-customer-202603','2026-04-05 23:59:59');
/*!40000 ALTER TABLE `revoked_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `fk_role_permissions_permission` (`permission_id`),
  CONSTRAINT `fk_role_permissions_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permissions`
--

LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */;
INSERT INTO `role_permissions` VALUES (1,1),(2,1),(5,1),(1,2),(1,3),(2,3),(1,4),(1,5),(1,6),(2,6),(1,7),(1,8),(1,9),(1,10),(2,10),(1,11),(1,12),(1,13),(1,14),(2,14),(4,14),(5,14),(6,14),(1,15),(4,15),(1,16),(2,16),(4,16),(5,16),(1,17),(4,17),(1,18),(4,18),(1,19),(4,19),(1,20),(4,20),(1,21),(4,21),(5,21),(1,22),(2,22),(4,22),(1,23),(4,23),(1,24),(2,24),(4,24),(5,24),(1,25),(4,25),(1,26),(4,26),(1,27),(2,27),(4,27),(1,28),(4,28),(1,29),(4,29),(1,30),(4,30),(1,31),(3,31),(1,32),(2,32),(3,32),(1,33),(3,33),(1,34),(3,34),(1,35),(3,35),(1,36),(2,36),(3,36),(1,37),(3,37),(1,38),(3,38);
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN','System administrator',1),(2,'MANAGER','Operations manager',1),(3,'PRODUCT_MANAGER','Product and menu manager',1),(4,'MARKETING_MANAGER','Marketing and loyalty manager',1),(5,'SUPPORT_AGENT','Customer support specialist',1),(6,'ROLE_CUSTOMER','Default customer role',1),(7,'USER','General internal user role',1);
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_user_roles_role` (`role_id`),
  CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,1),(2,2),(3,3),(4,4),(5,5),(201,6),(202,6),(203,6),(204,6),(205,6),(206,6),(207,6),(208,6);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `password_changed_at` datetime NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=209 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','admin@gmail.com','System Admin','0900000001','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80','1 Admin Street, District 1, Ho Chi Minh City','1994-01-10','MALE','ACTIVE','2026-04-05 12:54:31','2026-01-05 09:00:00','2025-12-01 09:00:00','2026-04-05 12:54:31'),(2,'manager','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','manager@gmail.com','Operations Manager','0900000002','https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80','2 Operations Avenue, District 3, Ho Chi Minh City','1992-07-15','FEMALE','ACTIVE','2026-03-30 18:10:00','2026-01-05 09:10:00','2025-12-01 09:05:00','2026-03-30 18:10:00'),(3,'product_manager','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','product.manager@example.com','Menu Product Manager','0900000003','https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=300&q=80','15 Product Hub, Binh Thanh, Ho Chi Minh City','1993-11-23','MALE','ACTIVE','2026-03-31 09:05:00','2026-01-08 10:00:00','2025-12-03 10:00:00','2026-03-31 09:05:00'),(4,'marketing_manager','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','marketing.manager@example.com','Marketing Manager','0900000004','https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=300&q=80','33 Campaign Street, District 7, Ho Chi Minh City','1995-05-18','FEMALE','ACTIVE','2026-03-31 07:45:00','2026-01-07 09:30:00','2025-12-04 09:30:00','2026-03-31 07:45:00'),(5,'support_agent','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','support.agent@example.com','Customer Support Agent','0900000005','https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=300&q=80','9 Support Lane, Phu Nhuan, Ho Chi Minh City','1996-03-09','MALE','ACTIVE','2026-03-30 20:30:00','2026-01-12 08:30:00','2025-12-05 08:30:00','2026-03-30 20:30:00'),(201,'customer_demo','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.demo@example.com','Nguyen Minh Anh','0901234567','https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=300&q=80','123 Nguyen Hue, Ben Nghe Ward, District 1, Ho Chi Minh City','1999-03-20','FEMALE','ACTIVE','2026-04-05 13:01:09','2026-01-15 10:10:00','2026-01-15 10:10:00','2026-04-05 13:01:09'),(202,'customer_demo_2','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.demo2@example.com','Tran Hoang Nam','0908877766','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80','88 Tan Quy Street, District 7, Ho Chi Minh City','1997-08-08','MALE','ACTIVE','2026-03-30 21:10:00','2026-01-16 11:00:00','2026-01-16 11:00:00','2026-03-30 21:10:00'),(203,'customer_vip','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.vip@example.com','Le Bao Chau','0907666888','https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=300&q=80','12 Masteri Riverside, Thu Duc City, Ho Chi Minh City','1991-12-01','FEMALE','ACTIVE','2026-03-31 18:55:00','2026-01-10 08:00:00','2026-01-10 08:00:00','2026-03-31 18:55:00'),(204,'customer_gold','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.gold@example.com','Pham Duc Khang','0904567788','https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=300&q=80','15 Vo Van Tan, District 3, Ho Chi Minh City','1994-04-11','MALE','ACTIVE','2026-03-30 19:20:00','2026-01-18 09:20:00','2026-01-18 09:20:00','2026-03-30 19:20:00'),(205,'customer_silver','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.silver@example.com','Do Thu Ha','0903456677','https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=300&q=80','29 Nguyen Cuu Van, Binh Thanh, Ho Chi Minh City','1998-10-05','FEMALE','ACTIVE','2026-03-28 12:35:00','2026-01-20 10:40:00','2026-01-20 10:40:00','2026-03-28 12:35:00'),(206,'customer_bronze','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.bronze@example.com','Vu Anh Tuan','0903344556','https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80','41 Truong Chinh, Tan Binh, Ho Chi Minh City','2001-02-14','MALE','ACTIVE','2026-03-27 07:20:00','2026-01-22 09:15:00','2026-01-22 09:15:00','2026-03-27 07:20:00'),(207,'customer_new','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.new@example.com','Bui Gia Linh','0901122334','https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80','7 Hoa Su, Phu Nhuan, Ho Chi Minh City','2002-06-17','FEMALE','ACTIVE','2026-03-31 21:00:00','2026-03-10 08:15:00','2026-03-10 08:15:00','2026-03-31 21:00:00'),(208,'customer_at_risk','$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS','customer.atrisk@example.com','Nguyen Hai Dang','0909988776','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80','18 Tran Nao, Thu Duc City, Ho Chi Minh City','1990-09-25','MALE','ACTIVE','2026-02-08 18:20:00','2026-01-25 13:00:00','2026-01-25 13:00:00','2026-02-08 18:20:00');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `production_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `production_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `production_db`;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `display_order` int NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `parent_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `fk_categories_parent` (`parent_id`),
  CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (3001,'Beverages','beverages','Main beverage root category for coffee and tea','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80',1,1,0,NULL,'2026-01-01 09:00:00','2026-03-01 09:00:00'),(3002,'Signature Coffee','signature-coffee','Espresso-based and specialty coffee drinks','https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=800&q=80',1,1,0,3001,'2026-01-01 09:05:00','2026-03-01 09:05:00'),(3003,'Tea And Fruit','tea-and-fruit','Tea and sparkling fruit-based drinks','https://images.unsplash.com/photo-1497534446932-c925b458314e?auto=format&fit=crop&w=800&q=80',2,1,0,3001,'2026-01-01 09:10:00','2026-03-01 09:10:00'),(3004,'Pastry','pastry','Fresh bakery and savory light meals','https://images.unsplash.com/photo-1517433670267-08bbd4be890f?auto=format&fit=crop&w=800&q=80',3,1,0,NULL,'2026-01-01 09:15:00','2026-03-01 09:15:00'),(3005,'Seasonal Specials','seasonal-specials','Limited-time drinks and feature launches','https://images.unsplash.com/photo-1464306076886-da185f6a9d05?auto=format&fit=crop&w=800&q=80',4,1,0,NULL,'2026-01-01 09:20:00','2026-03-01 09:20:00'),(3006,'Merchandise','merchandise','Branded reusable products and accessories','https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=800&q=80',5,1,0,NULL,'2026-01-01 09:25:00','2026-03-01 09:25:00');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingredients`
--

DROP TABLE IF EXISTS `ingredients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `default_unit` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cost_per_unit` decimal(12,2) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingredients`
--

LOCK TABLES `ingredients` WRITE;
/*!40000 ALTER TABLE `ingredients` DISABLE KEYS */;
INSERT INTO `ingredients` VALUES (2101,'Espresso Shot','Freshly pulled espresso shot','shot',4500.00,1,'2026-01-01 10:00:00','2026-01-01 10:00:00'),(2102,'Arabica Beans','House roast coffee beans','gram',0.45,1,'2026-01-01 10:01:00','2026-01-01 10:01:00'),(2103,'Oat Milk','Barista oat milk','ml',0.18,1,'2026-01-01 10:02:00','2026-01-01 10:02:00'),(2104,'Coconut Cream','Coconut cream blend','ml',0.20,1,'2026-01-01 10:03:00','2026-01-01 10:03:00'),(2105,'Matcha Powder','Premium ceremonial matcha','gram',2.80,1,'2026-01-01 10:04:00','2026-01-01 10:04:00'),(2106,'Peach Syrup','Peach flavor syrup','ml',0.22,1,'2026-01-01 10:05:00','2026-01-01 10:05:00'),(2107,'Butter Croissant Base','Butter croissant pastry base','piece',18.00,1,'2026-01-01 10:06:00','2026-01-01 10:06:00'),(2108,'Chocolate Filling','Chocolate pastry filling','gram',0.32,1,'2026-01-01 10:07:00','2026-01-01 10:07:00'),(2109,'Yuzu Syrup','Imported yuzu syrup concentrate','ml',0.25,1,'2026-01-01 10:08:00','2026-01-01 10:08:00'),(2110,'Chicken Filling','Smoked chicken and mayo filling','gram',0.65,1,'2026-01-01 10:09:00','2026-01-01 10:09:00'),(2111,'Bread Bun','Toasted sandwich bun','piece',12.00,1,'2026-01-01 10:10:00','2026-01-01 10:10:00'),(2112,'Ceramic Mug Body','Branded ceramic tumbler body','piece',95.00,1,'2026-01-01 10:11:00','2026-01-01 10:11:00');
/*!40000 ALTER TABLE `ingredients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_availabilities`
--

DROP TABLE IF EXISTS `product_availabilities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_availabilities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `franchise_id` bigint NOT NULL,
  `is_available` tinyint(1) NOT NULL,
  `price_override` decimal(12,2) DEFAULT NULL,
  `available_from` date DEFAULT NULL,
  `available_until` date DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_product_availabilities_product` (`product_id`),
  CONSTRAINT `fk_product_availabilities_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_availabilities`
--

LOCK TABLES `product_availabilities` WRITE;
/*!40000 ALTER TABLE `product_availabilities` DISABLE KEYS */;
INSERT INTO `product_availabilities` VALUES (1,4001,1,1,NULL,NULL,NULL,'2026-01-10 09:00:00','2026-01-10 09:00:00'),(2,4001,2,1,NULL,NULL,NULL,'2026-01-10 09:00:00','2026-01-10 09:00:00'),(3,4002,1,1,NULL,NULL,NULL,'2026-01-10 09:01:00','2026-01-10 09:01:00'),(4,4002,2,1,NULL,NULL,NULL,'2026-01-10 09:01:00','2026-01-10 09:01:00'),(5,4003,1,1,NULL,NULL,NULL,'2026-01-10 09:02:00','2026-01-10 09:02:00'),(6,4003,2,1,74000.00,NULL,NULL,'2026-01-10 09:02:00','2026-01-10 09:02:00'),(7,4004,1,1,NULL,NULL,NULL,'2026-01-10 09:03:00','2026-01-10 09:03:00'),(8,4004,2,0,NULL,NULL,NULL,'2026-01-10 09:03:00','2026-03-25 09:03:00'),(9,4005,1,1,NULL,'2026-03-01','2026-06-30','2026-02-20 09:00:00','2026-02-20 09:00:00'),(10,4005,2,1,78000.00,'2026-03-01','2026-06-30','2026-02-20 09:00:00','2026-02-20 09:00:00'),(11,4006,1,1,NULL,NULL,NULL,'2026-01-10 09:04:00','2026-01-10 09:04:00'),(12,4006,2,1,NULL,NULL,NULL,'2026-01-10 09:04:00','2026-01-10 09:04:00'),(13,4007,1,1,NULL,NULL,NULL,'2026-01-10 09:05:00','2026-01-10 09:05:00'),(14,4007,2,1,NULL,NULL,NULL,'2026-01-10 09:05:00','2026-01-10 09:05:00'),(15,4008,1,1,NULL,NULL,NULL,'2026-01-10 09:06:00','2026-01-10 09:06:00'),(16,4008,2,1,NULL,NULL,NULL,'2026-01-10 09:06:00','2026-01-10 09:06:00'),(17,4009,1,1,NULL,'2026-03-01','2026-06-30','2026-02-20 09:10:00','2026-02-20 09:10:00'),(18,4009,2,1,82000.00,'2026-03-01','2026-06-30','2026-02-20 09:10:00','2026-02-20 09:10:00'),(19,4010,1,1,NULL,NULL,NULL,'2026-01-10 09:07:00','2026-01-10 09:07:00'),(20,4010,2,1,NULL,NULL,NULL,'2026-01-10 09:07:00','2026-01-10 09:07:00'),(21,4011,1,1,NULL,NULL,NULL,'2026-01-10 09:08:00','2026-01-10 09:08:00'),(22,4011,2,1,NULL,NULL,NULL,'2026-01-10 09:08:00','2026-01-10 09:08:00'),(23,4012,1,1,NULL,NULL,NULL,'2026-01-10 09:09:00','2026-01-10 09:09:00'),(24,4012,2,1,NULL,NULL,NULL,'2026-01-10 09:09:00','2026-01-10 09:09:00');
/*!40000 ALTER TABLE `product_availabilities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `thumbnail_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `medium_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `large_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_primary` tinyint(1) NOT NULL DEFAULT '0',
  `display_order` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_product_images_product` (`product_id`),
  CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` VALUES (1,4001,'https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:00:00','2026-01-06 09:00:00'),(2,4002,'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:05:00','2026-01-06 09:05:00'),(3,4003,'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:10:00','2026-01-06 09:10:00'),(4,4004,'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:15:00','2026-01-06 09:15:00'),(5,4005,'https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=1000&q=80',1,0,'2026-02-21 09:00:00','2026-02-21 09:00:00'),(6,4006,'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:20:00','2026-01-06 09:20:00'),(7,4007,'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:25:00','2026-01-06 09:25:00'),(8,4008,'https://images.unsplash.com/photo-1483695028939-5bb13f8648b0?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1483695028939-5bb13f8648b0?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1483695028939-5bb13f8648b0?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1483695028939-5bb13f8648b0?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:30:00','2026-01-06 09:30:00'),(9,4009,'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=1000&q=80',1,0,'2026-02-21 09:10:00','2026-02-21 09:10:00'),(10,4010,'https://images.unsplash.com/photo-1461823385004-d7660947a7c0?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1461823385004-d7660947a7c0?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1461823385004-d7660947a7c0?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1461823385004-d7660947a7c0?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:35:00','2026-01-06 09:35:00'),(11,4011,'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:40:00','2026-01-06 09:40:00'),(12,4012,'https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=1000&q=80','https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=300&q=80','https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=600&q=80','https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=1000&q=80',1,0,'2026-01-06 09:45:00','2026-01-06 09:45:00');
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_ingredients`
--

DROP TABLE IF EXISTS `product_ingredients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_ingredients` (
  `product_id` bigint NOT NULL,
  `ingredient_id` bigint NOT NULL,
  `quantity` decimal(12,2) NOT NULL,
  `unit` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`product_id`,`ingredient_id`),
  KEY `fk_product_ingredients_ingredient` (`ingredient_id`),
  CONSTRAINT `fk_product_ingredients_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredients` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_ingredients_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_ingredients`
--

LOCK TABLES `product_ingredients` WRITE;
/*!40000 ALTER TABLE `product_ingredients` DISABLE KEYS */;
INSERT INTO `product_ingredients` VALUES (4001,2101,2.00,'shot','2026-01-05 10:00:00','2026-01-05 10:00:00'),(4001,2102,18.00,'gram','2026-01-05 10:00:00','2026-01-05 10:00:00'),(4002,2101,2.00,'shot','2026-01-05 10:01:00','2026-01-05 10:01:00'),(4002,2102,18.00,'gram','2026-01-05 10:01:00','2026-01-05 10:01:00'),(4003,2101,2.00,'shot','2026-01-05 10:02:00','2026-01-05 10:02:00'),(4003,2103,180.00,'ml','2026-01-05 10:02:00','2026-01-05 10:02:00'),(4004,2102,22.00,'gram','2026-01-05 10:03:00','2026-01-05 10:03:00'),(4004,2104,90.00,'ml','2026-01-05 10:03:00','2026-01-05 10:03:00'),(4005,2103,160.00,'ml','2026-02-20 10:00:00','2026-02-20 10:00:00'),(4005,2105,8.00,'gram','2026-02-20 10:00:00','2026-02-20 10:00:00'),(4006,2106,35.00,'ml','2026-01-05 10:04:00','2026-01-05 10:04:00'),(4007,2107,1.00,'piece','2026-01-05 10:05:00','2026-01-05 10:05:00'),(4008,2107,1.00,'piece','2026-01-05 10:06:00','2026-01-05 10:06:00'),(4008,2108,35.00,'gram','2026-01-05 10:06:00','2026-01-05 10:06:00'),(4009,2101,2.00,'shot','2026-02-20 10:10:00','2026-02-20 10:10:00'),(4009,2103,160.00,'ml','2026-02-20 10:10:00','2026-02-20 10:10:00'),(4010,2109,40.00,'ml','2026-01-05 10:07:00','2026-01-05 10:07:00'),(4011,2110,95.00,'gram','2026-01-05 10:08:00','2026-01-05 10:08:00'),(4011,2111,1.00,'piece','2026-01-05 10:08:00','2026-01-05 10:08:00'),(4012,2112,1.00,'piece','2026-01-05 10:09:00','2026-01-05 10:09:00');
/*!40000 ALTER TABLE `product_ingredients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_tags`
--

DROP TABLE IF EXISTS `product_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_tags` (
  `product_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`product_id`,`tag_id`),
  KEY `fk_product_tags_tag` (`tag_id`),
  CONSTRAINT `fk_product_tags_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_product_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_tags`
--

LOCK TABLES `product_tags` WRITE;
/*!40000 ALTER TABLE `product_tags` DISABLE KEYS */;
INSERT INTO `product_tags` VALUES (4001,2201),(4002,2201),(4004,2201),(4007,2201),(4005,2202),(4010,2202),(4011,2202),(4001,2203),(4003,2203),(4009,2203),(4006,2205),(4010,2205),(4004,2206),(4005,2206),(4006,2206),(4009,2206),(4007,2207),(4008,2207),(4012,2208);
/*!40000 ALTER TABLE `product_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sku` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(12,2) NOT NULL,
  `category_id` bigint NOT NULL,
  `is_available` tinyint(1) NOT NULL DEFAULT '1',
  `preparation_time` int DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `available_from` date DEFAULT NULL,
  `available_until` date DEFAULT NULL,
  `auto_unavailable_when_out_of_stock` tinyint(1) NOT NULL DEFAULT '0',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sku` (`sku`),
  KEY `fk_products_category` (`category_id`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (4001,'House Espresso','PROD-ESP-001','Double-shot house espresso with balanced chocolate notes.',42000.00,3002,1,4,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:00:00','2026-03-01 09:00:00'),(4002,'Saigon Americano','PROD-AME-002','Smooth americano crafted for daily coffee drinkers.',48000.00,3002,1,4,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:05:00','2026-03-01 09:05:00'),(4003,'Oat Milk Latte','PROD-LAT-003','Creamy latte with barista oat milk and house espresso.',72000.00,3002,1,6,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:10:00','2026-03-01 09:10:00'),(4004,'Coconut Cold Brew','PROD-CBR-004','Cold brew topped with rich coconut cream.',69000.00,3002,1,5,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:15:00','2026-03-01 09:15:00'),(4005,'Matcha Cloud Latte','PROD-MAT-005','Seasonal matcha latte finished with soft cream.',76000.00,3005,1,6,'ACTIVE','2026-03-01','2026-06-30',0,0,'2026-02-20 09:00:00','2026-03-20 09:00:00'),(4006,'Peach Oolong Tea','PROD-TEA-006','Refreshing peach oolong tea served iced.',53000.00,3003,1,4,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:20:00','2026-03-01 09:20:00'),(4007,'Butter Croissant','PROD-PAS-007','All-butter croissant baked fresh each morning.',48000.00,3004,1,3,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:25:00','2026-03-01 09:25:00'),(4008,'Chocolate Danish','PROD-PAS-008','Layered pastry with dark chocolate filling.',51000.00,3004,1,3,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:30:00','2026-03-01 09:30:00'),(4009,'Tiramisu Latte','PROD-SEA-009','Limited seasonal latte inspired by tiramisu dessert.',79000.00,3005,1,6,'ACTIVE','2026-03-01','2026-06-30',0,0,'2026-02-20 09:10:00','2026-03-20 09:10:00'),(4010,'Yuzu Sparkling','PROD-FRU-010','Bright sparkling yuzu refresher.',59000.00,3003,1,4,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:35:00','2026-03-01 09:35:00'),(4011,'Smoked Chicken Sandwich','PROD-SAV-011','Savory toasted sandwich with smoked chicken filling.',88000.00,3004,1,7,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:40:00','2026-03-01 09:40:00'),(4012,'Ceramic Tumbler','PROD-MER-012','Branded ceramic tumbler for retail merchandising.',189000.00,3006,1,0,'ACTIVE',NULL,NULL,0,0,'2026-01-05 09:45:00','2026-03-01 09:45:00');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=2209 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` VALUES (2201,'Best Seller','best-seller','Top-performing menu items',1,'2026-01-01 11:00:00','2026-01-01 11:00:00'),(2202,'New Arrival','new-arrival','New products launched this season',1,'2026-01-01 11:01:00','2026-01-01 11:01:00'),(2203,'Signature','signature','Brand signature items',1,'2026-01-01 11:02:00','2026-01-01 11:02:00'),(2204,'Vegetarian','vegetarian','Vegetarian-friendly items',1,'2026-01-01 11:03:00','2026-01-01 11:03:00'),(2205,'No Caffeine','no-caffeine','Items without caffeine',1,'2026-01-01 11:04:00','2026-01-01 11:04:00'),(2206,'Seasonal','seasonal','Limited seasonal offering',1,'2026-01-01 11:05:00','2026-01-01 11:05:00'),(2207,'Pastry','pastry','Bakery products',1,'2026-01-01 11:06:00','2026-01-01 11:06:00'),(2208,'Merchandise','merchandise-tag','Branded merchandise items',1,'2026-01-01 11:07:00','2026-01-01 11:07:00');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `engagement_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `engagement_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `engagement_db`;

--
-- Table structure for table `cart_coupons`
--

DROP TABLE IF EXISTS `cart_coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_coupons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint DEFAULT NULL,
  `coupon_id` bigint DEFAULT NULL,
  `discount_value` decimal(38,2) DEFAULT NULL,
  `applied_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cart_coupons_cart` (`cart_id`),
  KEY `fk_cart_coupons_coupon` (`coupon_id`),
  CONSTRAINT `fk_cart_coupons_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_coupons_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_coupons`
--

LOCK TABLES `cart_coupons` WRITE;
/*!40000 ALTER TABLE `cart_coupons` DISABLE KEYS */;
INSERT INTO `cart_coupons` VALUES (1,5,4,30000.00,'2026-04-01 09:30:00');
/*!40000 ALTER TABLE `cart_coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint DEFAULT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `subtotal` decimal(38,2) DEFAULT NULL,
  `cart_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cart_items_cart` (`cart_id`),
  CONSTRAINT `fk_cart_items_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (1,4006,'Peach Oolong Tea',53000.00,1,53000.00,1),(2,4007,'Butter Croissant',48000.00,1,48000.00,1),(3,4002,'Saigon Americano',48000.00,1,48000.00,2),(4,4003,'Oat Milk Latte',72000.00,1,72000.00,2),(5,4004,'Coconut Cold Brew',69000.00,1,69000.00,3),(6,4008,'Chocolate Danish',51000.00,1,51000.00,3),(7,4011,'Smoked Chicken Sandwich',88000.00,1,88000.00,4),(8,4009,'Tiramisu Latte',79000.00,1,79000.00,5),(9,4012,'Ceramic Tumbler',189000.00,1,189000.00,5);
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `final_amount` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (1,1001,101000.00,0.00,101000.00),(2,1002,120000.00,0.00,120000.00),(3,1004,120000.00,0.00,120000.00),(4,1007,88000.00,0.00,88000.00),(5,1008,268000.00,30000.00,238000.00);
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_usages`
--

DROP TABLE IF EXISTS `coupon_usages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_usages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `used_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_coupon_usages_coupon` (`coupon_id`),
  CONSTRAINT `fk_coupon_usages_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_usages`
--

LOCK TABLES `coupon_usages` WRITE;
/*!40000 ALTER TABLE `coupon_usages` DISABLE KEYS */;
INSERT INTO `coupon_usages` VALUES (1,1,1002,5003,23000.00,'2026-03-05 08:31:00'),(2,1,1004,5009,15000.00,'2026-03-07 09:46:00'),(3,2,1003,5007,60000.00,'2026-03-12 16:31:00'),(4,4,1002,5005,30000.00,'2026-03-30 10:11:00'),(5,7,1008,5016,20000.00,'2026-02-08 18:19:00');
/*!40000 ALTER TABLE `coupon_usages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `promotion_id` bigint NOT NULL,
  `max_uses` int DEFAULT NULL,
  `times_used` int NOT NULL DEFAULT '0',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `fk_coupons_promotion` (`promotion_id`),
  KEY `idx_coupons_status` (`status`),
  CONSTRAINT `fk_coupons_promotion` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons`
--

LOCK TABLES `coupons` WRITE;
/*!40000 ALTER TABLE `coupons` DISABLE KEYS */;
INSERT INTO `coupons` VALUES (1,'SPRING-GOLD-2026',1,400,2,'ACTIVE'),(2,'SPRING-GOLD-VIP',1,150,1,'ACTIVE'),(3,'HH-APR-25K',2,500,0,'INACTIVE'),(4,'SHIP-WEEKEND',3,800,1,'ACTIVE'),(5,'CROISSANT-SET',4,300,1,'ACTIVE'),(6,'VIP-BUNDLE',5,120,0,'INACTIVE'),(7,'LUNAR-30K',6,200,1,'EXPIRED');
/*!40000 ALTER TABLE `coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyalty_balances`
--

DROP TABLE IF EXISTS `loyalty_balances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyalty_balances` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `tier_id` bigint DEFAULT NULL,
  `current_points` int NOT NULL DEFAULT '0',
  `pending_points` int NOT NULL DEFAULT '0',
  `total_points_earned` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `customer_id` (`customer_id`),
  KEY `fk_loyalty_balances_tier` (`tier_id`),
  CONSTRAINT `fk_loyalty_balances_tier` FOREIGN KEY (`tier_id`) REFERENCES `tiers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyalty_balances`
--

LOCK TABLES `loyalty_balances` WRITE;
/*!40000 ALTER TABLE `loyalty_balances` DISABLE KEYS */;
INSERT INTO `loyalty_balances` VALUES (1,1001,1,341,25,821),(2,1002,2,1820,120,2460),(3,1003,4,12480,310,16650),(4,1004,3,7280,80,9850),(5,1005,2,2480,0,3020),(6,1006,1,420,30,420),(7,1007,1,80,101,80),(8,1008,3,5120,0,7440);
/*!40000 ALTER TABLE `loyalty_balances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyalty_configs`
--

DROP TABLE IF EXISTS `loyalty_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyalty_configs` (
  `id` bigint NOT NULL,
  `points_per_currency` decimal(19,8) NOT NULL,
  `min_order_amount` decimal(19,2) NOT NULL,
  `excluded_categories` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expiration_months` int NOT NULL,
  `evaluation_period_months` int NOT NULL,
  `inherit_from_lower_tiers` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyalty_configs`
--

LOCK TABLES `loyalty_configs` WRITE;
/*!40000 ALTER TABLE `loyalty_configs` DISABLE KEYS */;
INSERT INTO `loyalty_configs` VALUES (1,0.00100000,0.00,'MERCHANDISE',12,12,1);
/*!40000 ALTER TABLE `loyalty_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyalty_redemptions`
--

DROP TABLE IF EXISTS `loyalty_redemptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyalty_redemptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `reward_id` bigint DEFAULT NULL,
  `points_used` int DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_loyalty_redemptions_reward` (`reward_id`),
  CONSTRAINT `fk_loyalty_redemptions_reward` FOREIGN KEY (`reward_id`) REFERENCES `reward` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyalty_redemptions`
--

LOCK TABLES `loyalty_redemptions` WRITE;
/*!40000 ALTER TABLE `loyalty_redemptions` DISABLE KEYS */;
INSERT INTO `loyalty_redemptions` VALUES (1,1003,4,2500,'SUCCESS','NORMAL_REWARD',NULL,'Redeemed workshop seat for April tasting session','2026-03-20 10:30:00'),(2,1003,2,450,'SUCCESS','NORMAL_REWARD',NULL,'Redeemed free signature drink voucher','2026-03-25 15:00:00'),(3,1002,1,300,'SUCCESS','CHECKOUT_DISCOUNT',5005,'Applied reward during checkout','2026-03-30 10:12:00'),(4,1008,5,800,'FAILED','NORMAL_REWARD',NULL,'Reward unavailable during redemption','2026-03-05 09:10:00');
/*!40000 ALTER TABLE `loyalty_redemptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point_histories`
--

DROP TABLE IF EXISTS `point_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_histories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` int NOT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `adjusted_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_point_histories_customer_created_at` (`customer_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point_histories`
--

LOCK TABLES `point_histories` WRITE;
/*!40000 ALTER TABLE `point_histories` DISABLE KEYS */;
INSERT INTO `point_histories` VALUES (1,1001,'EARN',132,'Earned from order ORD-2026-5001',NULL,5001,'2026-02-18 10:30:00'),(2,1001,'BONUS',40,'Profile completion bonus','system',NULL,'2026-02-20 09:00:00'),(3,1001,'EARN',169,'Earned from order ORD-2026-5002',NULL,5002,'2026-03-24 20:00:00'),(4,1002,'EARN',190,'Earned from order ORD-2026-5003',NULL,5003,'2026-03-05 10:00:00'),(5,1002,'EARN',155,'Earned from order ORD-2026-5004',NULL,5004,'2026-03-18 14:00:00'),(6,1002,'BONUS',120,'March challenge completion','system',NULL,'2026-03-29 21:00:00'),(7,1003,'EARN',331,'Earned from order ORD-2026-5006',NULL,5006,'2026-02-27 16:00:00'),(8,1003,'EARN',324,'Earned from order ORD-2026-5007',NULL,5007,'2026-03-12 18:00:00'),(9,1003,'REDEEM',500,'Redeemed for coffee tasting workshop',NULL,NULL,'2026-03-20 10:30:00'),(10,1004,'EARN',193,'Earned from order ORD-2026-5009',NULL,5009,'2026-03-07 11:00:00'),(11,1004,'ADJUST',200,'Manual goodwill adjustment after support case','support.agent@example.com',NULL,'2026-03-22 09:45:00'),(12,1004,'EARN',281,'Earned from order ORD-2026-5010',NULL,5010,'2026-03-21 19:15:00'),(13,1005,'EARN',197,'Earned from order ORD-2026-5011',NULL,5011,'2026-03-11 12:10:00'),(14,1006,'EARN',143,'Earned from order ORD-2026-5013',NULL,5013,'2026-03-09 08:45:00'),(15,1007,'EARN',101,'Pending capture from order ORD-2026-5014',NULL,5014,'2026-03-31 20:30:00'),(16,1008,'EARN',238,'Earned from order ORD-2026-5015',NULL,5015,'2026-01-20 17:00:00'),(17,1008,'EXPIRE',300,'Points expired during inactivity review','system',NULL,'2026-03-01 00:05:00');
/*!40000 ALTER TABLE `point_histories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotions`
--

DROP TABLE IF EXISTS `promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` decimal(38,2) NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `is_featured` tinyint(1) DEFAULT '0',
  `min_order_amount` decimal(38,2) DEFAULT NULL,
  `min_quantity` int DEFAULT NULL,
  `applicable_products` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `applicable_categories` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `max_discount_amount` decimal(38,2) DEFAULT NULL,
  `max_uses_total` int DEFAULT NULL,
  `max_uses_per_customer` int DEFAULT NULL,
  `target_segment_ids` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_segment_mode` enum('EXCLUSIVE','INCLUSIVE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_promotions_status_dates` (`status`,`start_date`,`end_date`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotions`
--

LOCK TABLES `promotions` WRITE;
/*!40000 ALTER TABLE `promotions` DISABLE KEYS */;
INSERT INTO `promotions` VALUES (1,'Spring Gold Welcome','Featured promotion for gold and platinum shoppers','PERCENTAGE_DISCOUNT','ACTIVE',15.00,'2026-03-01 00:00:00','2026-04-30 23:59:59',1,150000.00,1,NULL,'SIGNATURE_COFFEE,PASTRY',60000.00,1000,2,'1,4',0,'2026-03-01 09:00:00','2026-03-28 09:00:00',NULL,NULL),(2,'Happy Hour After 2PM','Fixed discount for afternoon beverage orders','FIXED_DISCOUNT','SCHEDULED',25000.00,'2026-04-05 14:00:00','2026-05-05 18:00:00',0,99000.00,1,'4001,4002,4004,4010',NULL,25000.00,500,3,'2,3',0,'2026-03-15 10:00:00','2026-03-15 10:00:00',NULL,NULL),(3,'Weekend Free Shipping','Free shipping for weekend orders above threshold','FREE_SHIPPING','ACTIVE',0.00,'2026-03-15 00:00:00','2026-05-15 23:59:59',0,120000.00,1,NULL,NULL,NULL,800,4,NULL,0,'2026-03-15 11:00:00','2026-03-29 11:00:00',NULL,NULL),(4,'Buy 2 Get 1 Croissant','Bundle offer for pastry lovers','BUY_X_GET_Y','ACTIVE',1.00,'2026-03-10 00:00:00','2026-04-20 23:59:59',1,0.00,3,'4007','PASTRY',48000.00,300,2,NULL,0,'2026-03-10 09:00:00','2026-03-30 14:00:00',NULL,NULL),(5,'VIP Secret Menu Access','Draft campaign for premium secret-menu launch','BUNDLE','DRAFT',0.00,'2026-04-20 00:00:00','2026-05-20 23:59:59',0,250000.00,1,'4009,4012','SEASONAL_SPECIALS,MERCHANDISE',0.00,120,1,'1',0,'2026-03-25 09:30:00','2026-03-25 09:30:00',NULL,NULL),(6,'Lunar Flash Sale','Expired campaign kept for history views','FIXED_DISCOUNT','EXPIRED',30000.00,'2026-01-10 00:00:00','2026-02-10 23:59:59',0,100000.00,1,NULL,NULL,30000.00,200,1,NULL,0,'2026-01-05 08:00:00','2026-02-10 23:59:59',NULL,NULL);
/*!40000 ALTER TABLE `promotions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reward`
--

DROP TABLE IF EXISTS `reward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reward` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `required_points` int DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `availability` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reward`
--

LOCK TABLES `reward` WRITE;
/*!40000 ALTER TABLE `reward` DISABLE KEYS */;
INSERT INTO `reward` VALUES (1,'DISCOUNT_VOUCHER',300,'30,000 VND off next order',1),(2,'FREE_PRODUCT',450,'Free signature drink reward',1),(3,'GIFT_CARD',1200,'120,000 VND digital gift card',1),(4,'EXPERIENCE',2500,'Coffee tasting workshop seat',1),(5,'DISCOUNT_VOUCHER',800,'80,000 VND premium voucher',0);
/*!40000 ALTER TABLE `reward` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tier_benefits`
--

DROP TABLE IF EXISTS `tier_benefits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tier_benefits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tier_id` bigint NOT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` decimal(38,2) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tier_benefits_tier` (`tier_id`),
  CONSTRAINT `fk_tier_benefits_tier` FOREIGN KEY (`tier_id`) REFERENCES `tiers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tier_benefits`
--

LOCK TABLES `tier_benefits` WRITE;
/*!40000 ALTER TABLE `tier_benefits` DISABLE KEYS */;
INSERT INTO `tier_benefits` VALUES (1,1,'DISCOUNT',5.00,'5% discount on standard menu items'),(2,2,'FREE_SHIPPING',150000.00,'Free shipping for orders from 150,000 VND'),(3,2,'BONUS_POINTS',100.00,'100 welcome bonus points after qualifying order'),(4,3,'DISCOUNT',12.00,'12% discount on all menu items'),(5,3,'EXCLUSIVE_ACCESS',NULL,'Early access to featured promotions'),(6,4,'DISCOUNT',18.00,'18% discount on premium menu items'),(7,4,'GIFT',NULL,'Monthly signature drink voucher');
/*!40000 ALTER TABLE `tier_benefits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tier_history`
--

DROP TABLE IF EXISTS `tier_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tier_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `old_tier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_tier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `changed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tier_history`
--

LOCK TABLES `tier_history` WRITE;
/*!40000 ALTER TABLE `tier_history` DISABLE KEYS */;
INSERT INTO `tier_history` VALUES (1,1002,'BRONZE','SILVER','2026-02-15 09:00:00'),(2,1003,'GOLD','PLATINUM','2026-01-05 08:30:00'),(3,1004,'SILVER','GOLD','2026-03-01 10:00:00'),(4,1005,'BRONZE','SILVER','2026-02-28 15:20:00'),(5,1008,'SILVER','GOLD','2025-11-10 09:00:00');
/*!40000 ALTER TABLE `tier_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tiers`
--

DROP TABLE IF EXISTS `tiers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tiers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `min_points` int NOT NULL,
  `max_points` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tiers`
--

LOCK TABLES `tiers` WRITE;
/*!40000 ALTER TABLE `tiers` DISABLE KEYS */;
INSERT INTO `tiers` VALUES (1,'BRONZE',0,999),(2,'SILVER',1000,4999),(3,'GOLD',5000,9999),(4,'PLATINUM',10000,NULL);
/*!40000 ALTER TABLE `tiers` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-05 13:12:07
