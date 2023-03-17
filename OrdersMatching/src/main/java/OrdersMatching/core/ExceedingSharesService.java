package OrdersMatching.core;

import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.redisson.RedissonClientSingleton;
import OrdersMatching.core.models.ExceedingShares;
import OrdersMatching.repositories.ExceedingSharesRepository;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalTime;
import java.util.List;

public class ExceedingSharesService
{

	private final RedissonClient redisson;
	private final RMap<String, ExceedingShares> cacheExceedingShares;
	private final RMap<Integer, ExceedingShares> sentExceedingShares;

	private final ExceedingSharesRepository repository;
	private final TransactionTemplate txTemplate;

	public ExceedingSharesService(final ExceedingSharesRepository repository,
								  final TransactionTemplate txTemplate)
	{
		this.repository = repository;
		this.txTemplate = txTemplate;
		this.redisson = RedissonClientSingleton.getRedissonClient();
		this.cacheExceedingShares = redisson.getMap("exceedingShares");
		sentExceedingShares = redisson.getMap("sentExceedingShares");
	}

	public void addExceedingShares(ExceedingShares exceedingShares)
	{
		txTemplate.executeWithoutResult(status -> {
			addExceedingSharesToDb(exceedingShares);
			addExceedingSharesToRedis(exceedingShares);
		});
	}

	private void addExceedingSharesToDb(ExceedingShares exceedingShares)
	{
		repository.addExceedingShares(exceedingShares.getTicker(), exceedingShares.getQuantity(), exceedingShares.getPrice());
	}

	private void addExceedingSharesToRedis(ExceedingShares excShares)
	{
		String ticker = excShares.getTicker().replaceAll("[\\p{Cc}\\p{Cn}\\p{Z}]+", "_");
		ExceedingShares exceedingShares = cacheExceedingShares.get(ticker + "_unsent");
		if (exceedingShares == null)
		{
			cacheExceedingShares.put(ticker + "_unsent", excShares);
		}
		else
		{
			exceedingShares.setQuantity(exceedingShares.getQuantity() + exceedingShares.getQuantity());
			exceedingShares.setPrice(excShares.getPrice());
			cacheExceedingShares.put(ticker + "_unsent", exceedingShares);
		}
	}

	public ExceedingShares getExceedingSharesByTicker(String ticker,
													  int requestId)
	{
		try
		{
			return getExceedingSharesByTickerFromRedis(ticker, requestId);
		}
		catch (Exception e)
		{
			return getExceedingSharesByTickerFromDb(ticker, requestId);
		}
	}

	private ExceedingShares getExceedingSharesByTickerFromDb(String ticker,
															 int requestId)
	{
		return repository.getExceedingSharesByTickerAndRequestId(requestId, ticker);
	}

	private ExceedingShares getExceedingSharesByTickerFromRedis(String ticker,
																int requestId)
	{
		ExceedingShares exceedingShares = cacheExceedingShares.get(ticker + "_" + requestId);
		if (exceedingShares == null)
		{
			exceedingShares = cacheExceedingShares.remove(ticker + "_unsent");
			if (exceedingShares != null)
			{
				sentExceedingShares.fastPutIfAbsent(requestId, exceedingShares);
				cacheExceedingShares.fastPutIfAbsent(ticker + "_" + requestId, exceedingShares);
			}
		}
		return exceedingShares;
	}
}
