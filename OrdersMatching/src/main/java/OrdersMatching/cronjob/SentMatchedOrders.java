package OrdersMatching.cronjob;

import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.redisson.RedissonClientSingleton;
import OrdersMatching.repositories.enums.ExceedingSharesMatchedOrdersStatus;
import OrdersMatching.repositories.mysql.MySQLMatchedOrdersRepository;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@EnableScheduling
public class SentMatchedOrders
{

	private final RedissonClient redissonClient;
	private final MySQLMatchedOrdersRepository repository;

	public SentMatchedOrders(final MySQLMatchedOrdersRepository repository)
	{
		this.repository = repository;
		this.redissonClient = RedissonClientSingleton.getRedissonClient();
	}

	@Scheduled(fixedDelay = 5_000)
	public void failedMatchingProcess()
	{
		RMap<Integer, List<MatchedOrders>> matchedOrders = redissonClient.getMap("sentMatchedOrders");
		if(!matchedOrders.isEmpty())
		{
			for (Map.Entry<Integer, List<MatchedOrders>> orders : matchedOrders.entrySet())
			{
				repository.updateStatus(orders.getValue(), ExceedingSharesMatchedOrdersStatus.SENT.name(), orders.getKey());
				matchedOrders.remove(orders.getKey());
			}
		}
	}
}
