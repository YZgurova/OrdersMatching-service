package OrdersMatching.core;

import OrdersMatching.redisson.RedissonClientSingleton;
import OrdersMatching.core.models.ExceedingShares;
import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.core.models.Order;
import OrdersMatching.kafkaConfiguration.models.InstrumentPrice;
import OrdersMatching.repositories.mysql.MySQLExceedingSharesRepository;
import OrdersMatching.repositories.mysql.MySQLOrderRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class MatchingOrdersAlgorithm
{

	private final RedissonClient redissonClient;
	private final ExceedingSharesService exceedingSharesService;
	private final MatchOrdersService matchOrdersService;
	private final OrderService orderService;
	private final Logger LOGGER;

	public MatchingOrdersAlgorithm(final ExceedingSharesService exceedingSharesService,
								   final MatchOrdersService matchOrdersService,
								   final OrderService orderService)
	{
		this.redissonClient = RedissonClientSingleton.getRedissonClient();
		this.exceedingSharesService = exceedingSharesService;
		this.matchOrdersService = matchOrdersService;
		this.orderService = orderService;
		LOGGER = Logger.getLogger(MySQLOrderRepository.class.getName());
	}

	public void matchOrders(InstrumentPrice price)
	{
		long start = System.currentTimeMillis();
		LocalTime timestamp = LocalTime.now();
		List<Order> orderEntities = orderService.getOrdersByTicker(price.ticker(), timestamp);
		if (orderEntities.size() == 0)
		{
			LOGGER.info("Database doesn't have orders about " + price.ticker());
		}
		else
		{
			String lockKey = price.ticker() + "_processing_" + timestamp;
			RLock lock = redissonClient.getLock(lockKey);
			lock.lock();
			try
			{
				List<MatchedOrders> matchedOrders = new ArrayList<>();
				PriorityQueue<Order> positiveOrder = new PriorityQueue<>((p1, p2) -> p2.getQuantity() - p1.getQuantity());
				PriorityQueue<Order> negativeOrder = new PriorityQueue<>(Comparator.comparingInt(Order::getQuantity));

				for (Order order : orderEntities)
				{
					if (order.getQuantity() > 0)
					{
						positiveOrder.add(order);
					}
					else
					{
						negativeOrder.add(order);
					}
				}

				int positiveQueueSize = positiveOrder.size();
				int negativeQueueSize = negativeOrder.size();

				if (positiveQueueSize > 0 && negativeQueueSize > 0)
				{
					Order currentPositiveOrder = positiveOrder.poll();
					Order currentNegativeOrder = negativeOrder.poll();
					while (!positiveOrder.isEmpty() && !negativeOrder.isEmpty())
					{
						int positiveQuantity = currentPositiveOrder.getQuantity();
						int negativeQuntity = currentNegativeOrder.getQuantity();
						matchedOrders.add(MatchedOrders.builder().firstOrder(currentPositiveOrder).secondOrder(currentNegativeOrder).build());
						if (positiveQuantity > negativeQuntity * (-1))
						{
							currentPositiveOrder.setQuantity(positiveQuantity + negativeQuntity);
							currentNegativeOrder = negativeOrder.poll();
						}
						else
						{
							currentNegativeOrder.setQuantity(positiveQuantity + negativeQuntity);
							currentPositiveOrder = positiveOrder.poll();
						}
					}
				}
				calculateExceedingShares(positiveOrder, negativeOrder, price, start);
				matchOrdersService.addMatchedOrders(price.ticker(), timestamp, matchedOrders, orderEntities);
				lock.unlockAsync();
			}
			catch (Exception e)
			{
				lock.unlockAsync();
				throw e;
			}
			System.out.println("Your time is: " + (System.currentTimeMillis() - start));
		}
	}

	private void calculateExceedingShares(PriorityQueue<Order> positiveOrder,
										  PriorityQueue<Order> negativeOrder,
										  InstrumentPrice price,
										  long start)
	{
		int exceedingShares = 0;
		if (positiveOrder.isEmpty())
		{
			while (!negativeOrder.isEmpty())
			{
				exceedingShares += negativeOrder.poll().getQuantity();
			}
		}
		else
		{
			while (!positiveOrder.isEmpty())
			{
				exceedingShares += positiveOrder.poll().getQuantity();
			}
		}
		System.out.print("Matched time is: " + (System.currentTimeMillis() - start));

		if (exceedingShares != 0)
		{
			ExceedingShares currentExceedingShares = ExceedingShares.builder().quantity(exceedingShares).ticker(price.ticker()).price(exceedingShares > 0 ? price.buyPrice() : price.sellPrice()).build();
			exceedingSharesService.addExceedingShares(currentExceedingShares);
		}
	}
}
