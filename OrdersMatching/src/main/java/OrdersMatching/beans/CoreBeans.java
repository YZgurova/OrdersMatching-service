package OrdersMatching.beans;

import OrdersMatching.core.ExceedingSharesService;
import OrdersMatching.core.MatchOrdersService;
import OrdersMatching.core.MatchingOrdersAlgorithm;
import OrdersMatching.core.OrderService;
import OrdersMatching.repositories.ExceedingSharesRepository;
import OrdersMatching.repositories.MatchedOrdersRepository;
import OrdersMatching.repositories.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan
public class CoreBeans
{

	@Bean
	public OrderService ordersService(OrderRepository repository,
									  TransactionTemplate txTemplate)
	{
		return new OrderService(repository, txTemplate);
	}

	@Bean
	public ExceedingSharesService exceedingSharesService(ExceedingSharesRepository exceedingSharesRepository,
														 TransactionTemplate txTemplate)
	{
		return new ExceedingSharesService(exceedingSharesRepository, txTemplate);
	}

	@Bean
	public MatchOrdersService matchOrdersService(MatchedOrdersRepository matchedOrdersRepository,
												 TransactionTemplate txTemplate)
	{
		return new MatchOrdersService(matchedOrdersRepository, txTemplate);
	}

	@Bean
	public MatchingOrdersAlgorithm matchedOrdersAlgorithm(ExceedingSharesService exceedingSharesService,
														  OrderService orderService,
														  MatchOrdersService matchOrdersService)
	{
		return new MatchingOrdersAlgorithm(exceedingSharesService, matchOrdersService, orderService);
	}
}
