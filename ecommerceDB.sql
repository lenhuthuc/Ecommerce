DROP DATABASE IF EXISTS ecommerce;
Create database ecommerce;
use ecommerce;
CREATE TABLE `users` (
  `id` BIGINT PRIMARY KEY auto_increment,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `address` VARCHAR(255)
);

CREATE TABLE `roles` (
  `id` BIGINT PRIMARY KEY auto_increment,
  `role_name` VARCHAR(50) NOT NULL
);

CREATE TABLE `payment_method` (
  `id` BIGINT PRIMARY KEY auto_increment,
  `method_name` VARCHAR(100) NOT NULL
);

CREATE TABLE `product` (
  `id` BIGINT PRIMARY KEY auto_increment,
  `img_name` varchar(255),
  `img_data` VARCHAR(255),
  `product_name` VARCHAR(255) NOT NULL,
  `price` DECIMAL NOT NULL,
  `quantity` BIGINT NOT NULL
);

CREATE TABLE `cart` (
  `id` BIGINT PRIMARY KEY auto_increment,
  `user_id` BIGINT unique
);

CREATE TABLE `invoice` (
  `id` BIGINT PRIMARY KEY auto_increment,
  `user_id` BIGINT,
  `order_id` bigint unique,
  `total_price` DECIMAL,
  `payment_id` BIGINT,
  `created_at` DATETIME
);

CREATE TABLE `users_roles` (
  `user_id` BIGINT,
  `role_id` BIGINT,
  PRIMARY KEY (`user_id`, `role_id`)
);

CREATE TABLE `user_payment_methods` (
  `user_id` BIGINT,
  `payment_id` BIGINT,
  PRIMARY KEY (`user_id`, `payment_id`)
);

CREATE TABLE `cart_items` (
  `cart_id` BIGINT,
  `product_id` BIGINT,
  `quantity` BIGINT NOT NULL,
  PRIMARY KEY (`cart_id`, `product_id`)
);

CREATE TABLE `invoice_items` (
  `invoice_id` BIGINT,
  `product_id` BIGINT,
  `quantity` BIGINT NOT NULL,
  `price` DECIMAL NOT NULL,
  `total` DECIMAL NOT NULL,
  PRIMARY KEY (`invoice_id`, `product_id`)
);

create table `review` (
	`id` bigint primary key auto_increment,
    `rating` varchar(50) not null,
	`content` varchar(255),
    `user_id` bigint,
    `product_id` bigint
);

CREATE TABLE `orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
	`payment_id` bigint not null,
    `status` VARCHAR(50),
    `total_price` DECIMAL(12,2),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    foreign key (`user_id`) references `users` (`id`)
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


ALTER TABLE `cart` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `invoice` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `invoice` ADD FOREIGN KEY (`payment_id`) REFERENCES `payment_method` (`id`);

ALTER TABLE `users_roles` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `users_roles` ADD FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);

ALTER TABLE `user_payment_methods` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `user_payment_methods` ADD FOREIGN KEY (`payment_id`) REFERENCES `payment_method` (`id`);

ALter table `orders` ADD foreign key(`payment_id`) REFERENCES `payment_method` (`id`);

ALTER TABLE `cart_items` ADD FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`);

ALTER TABLE `cart_items` ADD FOREIGN KEY (`product_id`) REFERENCES `product` (`id`);

ALTER TABLE `invoice_items` ADD FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`id`);

ALTER TABLE `invoice_items` ADD FOREIGN KEY (`product_id`) REFERENCES `product` (`id`);

ALter table `review` add foreign key (`user_id`) references `users` (`id`);

ALter table `review` add foreign key (`product_id`) references `product` (`id`);
Alter table `invoice` add foreign key(`order_id`) references `orders` (`id`);
