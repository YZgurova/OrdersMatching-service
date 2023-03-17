package OrdersMatching.core;

import OrdersMatching.redisson.RedissonClientSingleton;
import OrdersMatching.core.models.Order;
import OrdersMatching.repositories.OrderRepository;
import OrdersMatching.repositories.mysql.MySQLExceedingSharesRepository;
import OrdersMatching.repositories.mysql.MySQLOrderRepository;
import io.lettuce.core.RedisConnectionException;
import org.redisson.RedissonScript;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.WriteRedisConnectionException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.module.FindException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class OrderService
{

	private final RedissonClient redissonClient;
	private final TransactionTemplate txTemplate;
	private final OrderRepository repository;
	private final Logger LOGGER;

	public OrderService(OrderRepository repository,
						final TransactionTemplate txTemplate)
	{
		this.repository = repository;
		this.txTemplate = txTemplate;
		this.redissonClient = RedissonClientSingleton.getRedissonClient();
		LOGGER = Logger.getLogger(MySQLOrderRepository.class.getName());
	}

	public void addOrders(List<Order> orders)
	{
		try
		{
			txTemplate.executeWithoutResult(status -> {
				addOrdersToDb(orders);
				addOrdersToRedis(orders);
			});
		}
		catch (RedisConnectionException e)
		{
			addOrdersToDb(orders);
		}
		catch (FindException | CannotCreateTransactionException | CannotGetJdbcConnectionException |
			   TransactionSystemException e)
		{
			addOrdersToRedis(orders);
		}
	}

	private void addOrdersToDb(List<Order> orders)
	{
		repository.addListOfOrders(orders);
	}

	private void addOrdersToRedis(List<Order> newOrders)
	{

		RMap<String, List<Order>> ordersCache = redissonClient.getMap("orders");
		Map<String, List<Order>> map = new ConcurrentHashMap<>();
		for (Order order : newOrders)
		{
			if (map.containsKey(order.getTicker() + "_pending"))
			{
				List<Order> currentOrdersPerTicker = map.get(order.getTicker() + "_pending");
				currentOrdersPerTicker.add(order);
				map.put(order.getTicker() + "_pending", currentOrdersPerTicker);
			}
			else
			{
				List<Order> o1 = new ArrayList<>();
				o1.add(order);
				map.put(order.getTicker() + "_pending", o1);
			}
		}
		for (Map.Entry<String, List<Order>> e : map.entrySet())
		{
			if (ordersCache.containsKey(e.getKey()))
			{
				List<Order> orders = ordersCache.get(e.getKey());
				orders.addAll(e.getValue());
				ordersCache.fastPut(e.getKey(), orders);
			}
			else
			{
				ordersCache.fastPut(e.getKey(), e.getValue());
			}
		}
	}

	public List<Order> getOrdersByTicker(String ticker,
										 LocalTime timestamp)
	{
		try
		{
			return getOrdersByTickerFromRedis(ticker, timestamp);
		}
		catch (RedisConnectionException e)
		{
			LOGGER.info("There is a problem with the Redis service. Lost connection");
			return getOrdersByTickerFromDb(ticker);
		}
		catch (NullPointerException e)
		{
			LOGGER.info("Redis doesn't have orders about " + ticker);
			return getOrdersByTickerFromDb(ticker);
		}
	}

	private List<Order> getOrdersByTickerFromRedis(String ticker,
												   LocalTime timestamp)
	{
		RMap<String, List<Order>> orders = redissonClient.getMap("orders");
		try
		{
			orders.fastPut(ticker + "_processing_" + timestamp, orders.remove(ticker + "_pending"));
		} catch(WriteRedisConnectionException e) {
			LOGGER.info("Redis master is down" + ticker);
			return orders.get(ticker + "_pending");
		}
		return orders.get(ticker + "_processing_" + timestamp);

	}

	private List<Order> getOrdersByTickerFromDb(String ticker)
	{
		return repository.getByTicker(ticker);
	}
}
