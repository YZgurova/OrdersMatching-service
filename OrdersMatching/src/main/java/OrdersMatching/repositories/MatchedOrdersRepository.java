package OrdersMatching.repositories;


import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.core.models.Order;

import java.util.List;
import java.util.Optional;

public interface MatchedOrdersRepository
{

	void addListOfMatched(List<MatchedOrders> orders,
						  List<Order> indexes);

	List<MatchedOrders> getOrderMatches(int orderId);

	List<MatchedOrders> getMatchedOrders(int requestId);

	void updateStatus(List<MatchedOrders> indexes,
					  String status,
					  int requestId);
}
