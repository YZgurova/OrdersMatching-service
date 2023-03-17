CREATE DATABASE IF NOT EXISTS orders_matching;
USE orders_matching;

CREATE TABLE orders(
       account_id INT NOT NULL,
       order_id INT PRIMARY KEY NOT NULL,
       ticker VARCHAR(50) NOT NULL,
       quantity INT NOT NULL,
       status ENUM('PENDING','PROCESSING', 'EXECUTED') DEFAULT 'PENDING');

CREATE TABLE exceeding_shares(
     id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
     ticker VARCHAR(50) NOT NULL,
     quantity INT NOT NULL,
     price DECIMAL(19,2),
     status ENUM('PENDING', 'SENТ') DEFAULT 'PENDING',
     request_id INT UNIQUE,
    UNIQUE KEY `uniq_exceeding_shares` (ticker,status));

CREATE TABLE matched_orders(
    id int PRIMARY KEY AUTO_INCREMENT NOT NULL,
    first_order_id int NOT NULL,
    second_order_id int NOT NULL,
    status ENUM('PENDING', 'SENТ') DEFAULT 'PENDING',
    request_id INT,
    FOREIGN KEY (first_order_id) REFERENCES orders(order_id),
    FOREIGN KEY (second_order_id) REFERENCES orders(order_id),
#     UNIQUE KEY `uniq_matched` (first_order_id,second_order_id),
    constraint not_equal check (matched_orders.first_order_id <> matched_orders.second_order_id));