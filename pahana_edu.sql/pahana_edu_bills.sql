-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: pahana_edu
-- ------------------------------------------------------
-- Server version	8.0.43

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
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `bill_no` varchar(20) NOT NULL,
  `customer_id` varchar(20) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `payment_method` enum('cash','card') NOT NULL,
  `payment_details` text,
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int NOT NULL,
  PRIMARY KEY (`bill_id`),
  UNIQUE KEY `bill_no` (`bill_no`),
  KEY `customer_id` (`customer_id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`account_no`),
  CONSTRAINT `bills_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `system_users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bills`
--

LOCK TABLES `bills` WRITE;
/*!40000 ALTER TABLE `bills` DISABLE KEYS */;
INSERT INTO `bills` VALUES (1,'BILL-250808-5436','000004',48864.00,'cash','Cash received: LKR 50000.00','2025-08-07 19:03:08',3),(2,'BILL-250808-8483','000004',10000.00,'cash','Cash received: LKR 10000.00','2025-08-07 19:39:54',3),(3,'BILL-250811-8852','000004',27432.00,'cash','Cash received: LKR 30000.00','2025-08-10 19:49:20',3),(4,'BILL-250811-1835','000005',10000.00,'cash','Cash received: LKR 10000.00','2025-08-11 15:57:16',3),(5,'BILL-250812-7025','000005',500.00,'cash','Cash received: LKR 1000.00','2025-08-12 05:53:15',3),(6,'BILL-250812-9188','000005',25532.00,'card','Card: 3123...3123','2025-08-12 09:20:15',4),(7,'BILL-250819-9575','000001',12000.00,'cash','Cash received: LKR 13000.00','2025-08-18 22:13:20',3),(8,'BILL-250819-4180','000001',2000.00,'cash','Cash received: LKR 2000.00','2025-08-18 22:29:28',3),(9,'BILL-250819-6034','000001',8000.00,'cash','Cash received: LKR 8000.00','2025-08-19 15:59:05',3),(10,'BILL-250819-9137','000009',6120.00,'cash','Cash received: LKR 6500.00','2025-08-19 16:51:27',3);
/*!40000 ALTER TABLE `bills` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-21  1:20:32
