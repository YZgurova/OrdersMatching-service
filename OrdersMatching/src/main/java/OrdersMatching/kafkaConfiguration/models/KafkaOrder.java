package OrdersMatching.kafkaConfiguration.models;

public record KafkaOrder(int accountId, int orderId, String ticker, int quantity)
{

}

