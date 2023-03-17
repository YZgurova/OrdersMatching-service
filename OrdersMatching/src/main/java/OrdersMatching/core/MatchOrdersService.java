package OrdersMatching.core;

import OrdersMatching.redisson.RedissonClientSingleton;
import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.core.models.Order;
import OrdersMatching.repositories.MatchedOrdersRepository;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalTime;
import java.util.*;

public class MatchOrdersService
{

	private final MatchedOrdersRepository matchedOrdersRepository;
	private final TransactionTemplate txTemplate;
	private final RMap<String, List<MatchedOrders>> cacheMatchedOrders;
	private final RMap<Integer, List<MatchedOrders>> sentMatchedOrders;

	public MatchOrdersService(MatchedOrdersRepository matchedOrdersRepository,
							  final TransactionTemplate txTemplate)
	{
		this.matchedOrdersRepository = matchedOrdersRepository;
		this.txTemplate = txTemplate;
		RedissonClient redisson = RedissonClientSingleton.getRedissonClient();
		cacheMatchedOrders = redisson.getMap("matchedOrders");
		sentMatchedOrders = redisson.getMap("sentMatchedOrders");
	}

	public void addMatchedOrders(String ticker, LocalTime timestamp, List<MatchedOrders> matchedOrders, List<Order> indexes)
	{
		txTemplate.executeWithoutResult(status -> {
			addMatchedOrdersToDb(matchedOrders, indexes);
			addMatchedOrdersToRedis(ticker, matchedOrders, timestamp);
		});
	}

	private void addMatchedOrdersToRedis(String ticker, List<MatchedOrders> newMatchedOrders, LocalTime timestamp)
	{
		List<MatchedOrders> matchedOrders = cacheMatchedOrders.get("unsent");
		if (matchedOrders == null)
		{
			cacheMatchedOrders.put("unsent", newMatchedOrders);
		}
		else
		{
			matchedOrders.addAll(newMatchedOrders);
			cacheMatchedOrders.put("unsent", matchedOrders);
		}
		cacheMatchedOrders.remove(ticker + "_processing_" + timestamp);
	}

	private void addMatchedOrdersToDb(List<MatchedOrders> matchedOrders, List<Order> indexes)
	{
		matchedOrdersRepository.addListOfMatched(matchedOrders, indexes);
	}

	public List<MatchedOrders> getUnsentMatchOrders(int requestId)
	{
		try
		{
			return getUnsentMatchOrdersByRedis(requestId);
		}
		catch (Exception e)
		{
			return getUnsentMatchOrdersByDb(requestId);
		}
	}

	public List<MatchedOrders> getUnsentMatchOrdersByRedis(int requestId)
	{
		List<MatchedOrders> matchedOrders = cacheMatchedOrders.get(String.valueOf(requestId));
		if (matchedOrders == null)
		{
			matchedOrders = cacheMatchedOrders.get("unsent");
			if(matchedOrders!=null)
			{
				sentMatchedOrders.put(requestId, matchedOrders);
				cacheMatchedOrders.fastPutIfAbsent(String.valueOf(requestId), matchedOrders);
				cacheMatchedOrders.fastPut(String.valueOf(requestId), cacheMatchedOrders.remove("unsent"));
			} else {
				throw new NullPointerException();
			}
		}
		return matchedOrders;
	}

	public List<MatchedOrders> getUnsentMatchOrdersByDb(int requestId)
	{
		return matchedOrdersRepository.getMatchedOrders(requestId);
	}

	public List<MatchedOrders> getOrderMatches(int orderId)
	{
		return matchedOrdersRepository.getOrderMatches(orderId);
	}
}
