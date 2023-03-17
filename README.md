# OrdersMatching-service
OrdersMatching service get some orders for shares and match their quantity in way which to send as small as possible exceeding shares to next service

Problem definition \n
●	Orders matching means that when a client wants to buy shares from a given instrument and other clients want to sell the same amount of shares from the same instrument, we can match their orders.
 It's important because If we don't match orders the volume of orders we will send to next service will be times larger and process in the next service will be more slower
●	The main challenge is performance - we need to match orders as quickly as possible and, on request, be able to output exceeding shares and matching orders
Implementation
	This is an overview of the system architecture: 
Components overview
Orders Matching service
The main purpose of this service is each operation to be performed as quickly as possible for the largest possible amount of data.
When “Orders Matching” receives orders, save them in MySQL with status “PROCESSED” and they wait to be “EXECUTED” while service does not receive a price.
Price events give a price to some ticker, so orders about every instrument become executed after “Orders Matching” receive price for it.
When orders are matched they must be saved in the database and exceeding shares also and change orders status to “EXECUTED”.
 
Shares Checker
“Orders Matching” calculates exceeding shares, then after request from the “Shares checker”, sends all exceeding shares and matched orders, which are not sent.
The system buys the shares which clients are selling. “Shares Checker” checks whether the company has availability of these shares which clients want to buy.
 If the answer is yes, the company sells its shares to clients, else a request to “Shares supplier” is sent to buy the needed quantity. 
Shares Supplier
For the purposes of this project, this service will be mocked, unless time permits
Its goal is to buy shares from external brokers.
When a “Shares Checker” receives exceeding shares, but doesn’t have needed quantity send a request to “Shares supplier” service, whose task is to buy this needed quantity of shares, and if operation has been successful return response ‘OK’.
Database
The main challenge is that the service will be receiving a huge load of orders which are saved in the database and when receiving price instruments will read all not executed orders.
Average load of orders which this service will receive per seconds will be 10 000, and price instruments can reach 400,000 per seconds. This means that if I received this quantity every second I must save orders into the database and within one second I will take 400,000 times the unfulfilled orders.
Since we know that NoSQL and MySQL both will be able to handle this volume of data and can also handle the optimized writing and reading from and into the database they will both have similar performance in this case. The disadvantage of using NoSQL is that it does not support ACID and eventual consistency can occur. This is a problem in my project since the matcher service will be able to read stale data so It’s going to be possible for the matcher to match orders that have been matched already. So for the sake of the project MySQL will be a more suitable option since it supports strong consistency and issues like the one mentioned above cannot happen.
DB Structure
Host: mysql
Database: orders_matching
Tables: 
orders(
id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
accountId INT NOT NULL,
orderId INT UNIQUE NOT NULL,
ticker TEXT NOT NULL,
quantity INT NOT NULL,
matched_with_orderId INT,
status ENUM('PROCESSED', 'EXECUTED') DEFAULT 'PROCESSED');

exceeding_shares(
id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
ticker TEXT NOT NULL,
quantity INT NOT NULL,
price DECIMAL(19,2),
status ENUM('PENDING', 'SENТ') DEFAULT 'PENDING',
request_id INT UNIQUE);

matched_orders(
id int PRIMARY KEY AUTO_INCREMENT NOT NULL,
first_orderId int NOT NULL,
second_orderId int NOT NULL,
status ENUM('PENDING', 'SENТ') DEFAULT 'PENDING',
request_id INT UNIQUE,
FOREIGN KEY (first_orderId) REFERENCES orders(orderId),
FOREIGN KEY (second_orderId) REFERENCES orders(orderId));
 
Performance Improvements
Main challenge in this task is performance. 
Here are some ways with which will increase “Orders matching” flow performance.
1. When consumers (Orders matching) pool events from orders topic it will use batching to take not only one event, but all those which are waiting to be pooled.
2. Orders must be saved in database but instead of insert them one by one, they will be bulk inserted 
3. I will use Redis cache which will contain in every moment all not executed orders, to hurry up taking orders when the price instrument comes.
4. MySQL can be optimized for reading and writing 
	- managing my.cnf file(memory usage, cache, sizes and communication parameters)
-using good queries
-indexes and partitions

Introduced API
●	Kafka topics
order {
Int accountId,
Int orderId,
String ticker,
Int quantity,
}
quotes.raw.equity
	     code|bid|ask
	(ticker code| buy price| sell price )
●	Rest API
The Share checker will make a request to Orders Matching with Rest API.
Orders Matching have two endpoints

GET:	shares/exceeding
GET:	shares/matched

 which will take exceeding shares and matched orders.
●	Redis
When services receive orders they will be saved in Redis and MySQL. In this way when we need unfulfilled orders we will take them from redis, and after execution these orders will be deleted from redis.

Service availability
This is an important service, which we would like to have even if something falls down users can continue their experience.
“Orders matching” service will be a consumer group, which means that in every moment all services will work. If some of them fall down, a kafka broker will detect it and events will be distributed. 
If the database falls down we also will have replicas which will continue leader work.
Troubleshooting and monitoring
Observability is out of scope for MVP, but if i had to monitor the process I would use both:
●	Logs
●	Errors
●	Redis Sentinel
