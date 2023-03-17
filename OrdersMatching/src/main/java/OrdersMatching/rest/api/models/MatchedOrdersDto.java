package OrdersMatching.rest.api.models;

import java.util.Optional;

public record MatchedOrdersDto(OrderDto firstOrder, OrderDto secondOrder)
{

}
