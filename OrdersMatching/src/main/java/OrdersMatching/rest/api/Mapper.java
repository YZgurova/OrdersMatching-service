package OrdersMatching.rest.api;

import OrdersMatching.core.models.ExceedingShares;
import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.core.models.Order;
import OrdersMatching.rest.api.models.ExceedingSharesDto;
import OrdersMatching.rest.api.models.MatchedOrdersDto;
import OrdersMatching.rest.api.models.OrderDto;

import java.util.Optional;

public class Mapper
{

	public static OrderDto fromOrder(Order order)
	{
		return new OrderDto(order.getAccountId(), order.getOrderId(), order.getTicker(), order.getQuantity());
	}

	public static MatchedOrdersDto fromMatchedOrder(MatchedOrders orders)
	{
		return new MatchedOrdersDto(fromOrder(orders.getFirstOrder()), fromOrder(orders.getSecondOrder()));
	}

	public static ExceedingSharesDto fromExceedingShares(ExceedingShares shares)
	{
		return new ExceedingSharesDto(shares.getTicker(), shares.getQuantity(), shares.getPrice());
	}
}
