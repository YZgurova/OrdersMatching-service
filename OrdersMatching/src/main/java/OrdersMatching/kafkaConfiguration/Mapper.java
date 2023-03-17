package OrdersMatching.kafkaConfiguration;

import OrdersMatching.core.models.Order;
import OrdersMatching.kafkaConfiguration.models.KafkaOrder;

public class Mapper
{

	public static Order toOrder(KafkaOrder order)
	{
		return new Order(order.accountId(), order.orderId(), order.ticker(), order.quantity());
	}
}
