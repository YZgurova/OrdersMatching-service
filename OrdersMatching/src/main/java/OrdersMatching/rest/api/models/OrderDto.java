package OrdersMatching.rest.api.models;

public record OrderDto(int accountId, int orderId, String ticker, int quantity)
{

}