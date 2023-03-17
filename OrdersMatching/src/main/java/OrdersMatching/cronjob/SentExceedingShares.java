package OrdersMatching.cronjob;

import OrdersMatching.core.models.ExceedingShares;
import OrdersMatching.redisson.RedissonClientSingleton;
import OrdersMatching.repositories.mysql.MySQLExceedingSharesRepository;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@EnableScheduling
public class SentExceedingShares
{

	private final RedissonClient redissonClient;
	private final MySQLExceedingSharesRepository repository;

	public SentExceedingShares(final MySQLExceedingSharesRepository repository)
	{
		this.repository = repository;
		this.redissonClient = RedissonClientSingleton.getRedissonClient();
	}

	@Scheduled(fixedDelay = 5_000)
	public void failedMatchingProcess()
	{
		RMap<Integer, ExceedingShares> exceedingShares = redissonClient.getMap("sentExceedingShares");
		if (!exceedingShares.isEmpty())
		{
			for (Map.Entry<Integer, ExceedingShares> shares : exceedingShares.entrySet())
			{
				repository.setRequestId(shares.getKey(), shares.getValue().getTicker());
				exceedingShares.remove(shares.getKey());
			}
		}
	}
}
