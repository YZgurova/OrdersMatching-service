package OrdersMatching.rest.api;

import OrdersMatching.core.ExceedingSharesService;
import OrdersMatching.core.MatchOrdersService;
import OrdersMatching.core.models.ExceedingShares;
import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.rest.api.models.ExceedingSharesDto;
import OrdersMatching.rest.api.models.MatchedOrdersDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/shares")
public class OrderController
{

	private final MatchOrdersService matchOrdersService;
	private final ExceedingSharesService exceedingSharesService;

	public OrderController(
			final MatchOrdersService matchOrdersService,
			final ExceedingSharesService exceedingSharesService)
	{
		this.matchOrdersService = matchOrdersService;
		this.exceedingSharesService = exceedingSharesService;
	}

	@GetMapping(value = "/matched")
	public List<MatchedOrdersDto> matchedOrdersByRequestId(@RequestHeader("Request-Id") Integer requestId)
	{
		List<MatchedOrders> matchedOrders = matchOrdersService.getUnsentMatchOrders(requestId);
		if(matchedOrders!=null) {
			return matchedOrders.stream()
						 .map(Mapper::fromMatchedOrder)
						 .collect(Collectors.toList());
		}
		return null;
	}

	@GetMapping(value = "/exceeding")
	public ExceedingSharesDto exceedingSharesByRequestId(@RequestHeader("Request-Id") Integer requestId,
																  @RequestBody String ticker)
	{
		ExceedingShares exceedingShares = exceedingSharesService.getExceedingSharesByTicker(ticker, requestId);
		if(exceedingShares!=null) {
			return Mapper.fromExceedingShares(exceedingShares);
		}
		return null;
	}

	@GetMapping(value = "/matched/order/{id}")
	public List<MatchedOrdersDto> matchedOrdersByOrderId(@PathVariable Integer id)
	{
		List<MatchedOrders> matchedOrders = matchOrdersService.getOrderMatches(id);
		if(matchedOrders!=null) {
			return matchedOrders
					.stream()
					.map(Mapper::fromMatchedOrder)
					.collect(Collectors.toList());
		}
		return null;
	}
}
