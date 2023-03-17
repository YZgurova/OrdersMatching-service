package OrdersMatching.rest.api.models;

import java.math.BigDecimal;

public record ExceedingSharesDto(String ticker, int quantity, BigDecimal price)
{

}
