package OrdersMatching.beans;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@ComponentScan
public class DbBeans
{

	@Bean
	@ConfigurationProperties("spring.datasource")
	public DataSource dataSource()
	{
		return DataSourceBuilder
				.create()
				.type(SingleConnectionDataSource.class)
				.build();
	}

	@Bean
	public TransactionTemplate transactionTemplate(DataSource dataSource)
	{
		return new TransactionTemplate(new DataSourceTransactionManager(dataSource));
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource)
	{
		return new JdbcTemplate(dataSource);
	}
}
