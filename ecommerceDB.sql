DROP DATABASE IF EXISTS ecommerce;
CREATE DATABASE ecommerce;
USE ecommerce;

CREATE TABLE `users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `address` VARCHAR(255)
);

CREATE TABLE `roles` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `role_name` VARCHAR(50) NOT NULL
);

CREATE TABLE `payment_method` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `method_name` VARCHAR(100) NOT NULL
);

CREATE TABLE `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `product_name` VARCHAR(500) NOT NULL, 
  `price` DECIMAL(12, 2) NOT NULL,
  `category` VARCHAR(255),              
  `image` VARCHAR(1000),                
  `description` TEXT,                   
  `rating_count` INT DEFAULT 0,        
  `quantity` BIGINT DEFAULT 100        
);

CREATE TABLE `cart` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT UNIQUE,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `orders` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `payment_id` BIGINT NOT NULL,
  `address` VARCHAR(255),
  `status` VARCHAR(50),
  `total_price` DECIMAL(12,2),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  FOREIGN KEY (`payment_id`) REFERENCES `payment_method` (`id`)
);

CREATE TABLE `invoice` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT,
  `order_id` BIGINT UNIQUE,
  `total_price` DECIMAL(12,2),
  `payment_id` BIGINT,
  `created_at` DATETIME,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  FOREIGN KEY (`payment_id`) REFERENCES `payment_method` (`id`)
);

CREATE TABLE `users_roles` (
  `user_id` BIGINT,
  `role_id` BIGINT,
  PRIMARY KEY (`user_id`, `role_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
);

CREATE TABLE `user_payment_methods` (
  `user_id` BIGINT,
  `payment_id` BIGINT,
  PRIMARY KEY (`user_id`, `payment_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  FOREIGN KEY (`payment_id`) REFERENCES `payment_method` (`id`)
);

CREATE TABLE `cart_items` (
  `cart_id` BIGINT,
  `product_id` BIGINT,
  `quantity` BIGINT NOT NULL,
  PRIMARY KEY (`cart_id`, `product_id`),
  FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`),
  FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
);

CREATE TABLE `order_items` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` BIGINT NOT NULL,
  `price` DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`),
  FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
);

CREATE TABLE `invoice_items` (
  `invoice_id` BIGINT,
  `product_id` BIGINT,
  `quantity` BIGINT NOT NULL,
  `price` DECIMAL(12,2) NOT NULL,
  `total` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`invoice_id`, `product_id`),
  FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`),
  FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
);

CREATE TABLE `review` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `rating` INT NOT NULL,
  `content` VARCHAR(255),
  `user_id` BIGINT,
  `product_id` BIGINT,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
);

CREATE TABLE `user_interactions` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
);

-- Insert initial data
INSERT INTO roles (role_name) VALUES ('USER'), ('ADMIN');
INSERT INTO payment_method (method_name) VALUES ('COD'), ('VNPAY');