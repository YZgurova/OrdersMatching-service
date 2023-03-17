package OrdersMatching.cronjob;

import OrdersMatching.core.MatchOrdersService;
import OrdersMatching.redisson.RedissonClientSingleton;
import OrdersMatching.core.models.Order;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
public class FailedMatchingHandler
{


	private final RedissonClient redissonClient = RedissonClientSingleton.getRedissonClient();;
	private static final String processingKeyTemplate = "_processing_";

	@Scheduled(fixedDelay = 1000, initialDelay = 100)
	public void failedMatchingProcess()
	{

		RMap<String, List<Order>> orders = redissonClient.getMap("orders");
		if(!orders.isEmpty())
		{
			for (Map.Entry<String, List<Order>> orderPart : orders.entrySet())
			{
				String lockKey = orderPart.getKey();
				RLock lock = redissonClient.getLock(lockKey);
				boolean lockAcquired = lock.isLocked();
				if (lockAcquired)
				{
					long ttl = redissonClient.getKeys().remainTimeToLive(lockKey);
					if (TimeUnit.MILLISECONDS.toSeconds(ttl) > 3000)
					{
						lock.unlock();
						returnOrdersToStartState(orders, lockKey);
					}
				}
				if (lockKey.contains(processingKeyTemplate) && !lockAcquired)
				{
					returnOrdersToStartState(orders, lockKey);
				}
			}
		}
	}

	private static void returnOrdersToStartState(final RMap<String, List<Order>> orders,
												 final String lockKey)
	{
		List<Order> ordersByTicker = orders.remove(lockKey);
		orders.fastPutIfAbsent(ordersByTicker.get(0).getTicker() + "_pending", ordersByTicker);
	}
}
