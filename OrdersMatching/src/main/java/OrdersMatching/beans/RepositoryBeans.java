package OrdersMatching.beans;

import OrdersMatching.repositories.ExceedingSharesRepository;
import OrdersMatching.repositories.MatchedOrdersRepository;
import OrdersMatching.repositories.OrderRepository;
import OrdersMatching.repositories.mysql.MySQLExceedingSharesRepository;
import OrdersMatching.repositories.mysql.MySQLMatchedOrdersRepository;
import OrdersMatching.repositories.mysql.MySQLOrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan
public class RepositoryBeans
{

	@Bean
	public OrderRepository orderRepository(JdbcTemplate jdbcTemplate)
	{
		return new MySQLOrderRepository(jdbcTemplate);
	}

	@Bean
	public ExceedingSharesRepository exceedingSharesRepository(TransactionTemplate txTemplate,
															   JdbcTemplate jdbcTemplate)
	{
		MySQLOrderRepository orderRepository = new MySQLOrderRepository(jdbcTemplate);
		return new MySQLExceedingSharesRepository(txTemplate, jdbcTemplate, orderRepository);
	}

	@Bean
	public MatchedOrdersRepository matchedOrdersRepository(TransactionTemplate txTemplate,
														   JdbcTemplate jdbcTemplate,
														   OrderRepository orderRepository)
	{
		return new MySQLMatchedOrdersRepository(txTemplate, jdbcTemplate, orderRepository);
	}
}
