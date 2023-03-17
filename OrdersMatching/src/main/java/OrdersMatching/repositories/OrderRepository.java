package OrdersMatching.repositories;

import OrdersMatching.core.models.Order;

import java.util.List;

public interface OrderRepository
{

	void addListOfOrders(List<Order> orders);

	void updateStatus(List<Order> indexes,
					  String status);

	List<Order> getByTicker(String ticker);
}
