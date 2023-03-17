package OrdersMatching.repositories;

import OrdersMatching.core.models.ExceedingShares;

import java.math.BigDecimal;
import java.util.Optional;

public interface ExceedingSharesRepository
{

	void addExceedingShares(String ticker,
							int quantity,
							BigDecimal price);


	ExceedingShares getExceedingSharesByTickerAndRequestId(int requestId,
														   String ticker);

	void setRequestId(int requestId,
					  String ticker);
}
