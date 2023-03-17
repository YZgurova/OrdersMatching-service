package OrdersMatching.kafkaConfiguration.models;

import java.math.BigDecimal;

public record InstrumentPrice(String ticker, BigDecimal buyPrice, BigDecimal sellPrice)
{

}
