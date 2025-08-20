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
-- Table structure for table `system_users`
--

DROP TABLE IF EXISTS `system_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `employee_no` varchar(20) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(255) NOT NULL,
  `role` enum('admin','staff') DEFAULT 'staff',
  `status` enum('active','inactive') DEFAULT 'active',
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `employee_no` (`employee_no`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_users`
--

LOCK TABLES `system_users` WRITE;
/*!40000 ALTER TABLE `system_users` DISABLE KEYS */;
INSERT INTO `system_users` VALUES (1,'Administrator','EMP001','admin','employee@example.com','admin','active','2025-07-24 23:22:39','$2a$12$7SC7xfM4Sm7I/QfCDoooUuENI1kfzzdO5Z1kYBL6nlxTm41IbpPDu'),(2,'John Doe','EMP002','johndoe','johndoe@gmail.com','staff','active','2025-07-25 00:48:45','$2a$12$YfDJFeg8ikrbiUzDvIrKU.Vvxu8o6fscHFfMnUVAdQ/Wy2NrYEhn2'),(3,'Janithi Maheshi','715244','Janithi','janithiicbt@gmail.com','staff','active','2025-07-27 14:43:07','$2a$12$7z.81l3ujo3SWF86lduGdONSLSj3w4kAUYmdWSlvH4iY2ZG57/fJ.'),(4,'Janula Pesandu','710555','Janula','sallycrusher@gmail.com','staff','active','2025-07-27 14:47:11','$2a$12$8X5Fz8RsS8DYz9V4bJk9iuYu1HRXjrWpS5uaVZkIoLtaw9IWrfdH6'),(7,'Test 1','Emptest','test1','janulapesandu21@gmail.com','admin','active','2025-07-27 19:53:17','$2a$12$2SCP860kZ92dOapZoof3uecmBk9ZXY2.jWw1HG4NKMAA6bF4WMJUa'),(8,'Test 2','Emptest2','test2','test2@gmail.com','staff','inactive','2025-07-27 19:58:44','$2a$12$Kqw42lajt1JfOIm9hFScK.My4ZW3nsYSHTr.2xjsYaSQsLYOMgKeK');
/*!40000 ALTER TABLE `system_users` ENABLE KEYS */;
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
