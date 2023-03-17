package OrdersMatching.repositories.mysql;

import OrdersMatching.core.models.ExceedingShares;
import OrdersMatching.repositories.ExceedingSharesRepository;
import OrdersMatching.repositories.enums.ExceedingSharesMatchedOrdersStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class MySQLExceedingSharesRepository implements ExceedingSharesRepository
{

	private final TransactionTemplate txTemplate;
	private final JdbcTemplate jdbc;
	private final MySQLOrderRepository orderRepository;
	private final Logger LOGGER;

	public MySQLExceedingSharesRepository(TransactionTemplate txTemplate,
										  JdbcTemplate jdbcT,
										  MySQLOrderRepository orderRepository)
	{
		this.txTemplate = txTemplate;
		this.jdbc = jdbcT;
		this.orderRepository = orderRepository;
		LOGGER = Logger.getLogger(MySQLExceedingSharesRepository.class.getName());
	}

	@Override
	public void addExceedingShares(String ticker,
								   int quantity,
								   BigDecimal price)
	{
		try
		{
				Map<String, Object> map = jdbc.call(conn -> {
					CallableStatement cs = conn.prepareCall("{call save_exceeding_shares(?,?,?,?)}");
					cs.setString(1, ticker);
					cs.setInt(2, quantity);
					cs.setBigDecimal(3, price);
					cs.setString(4, ExceedingSharesMatchedOrdersStatus.PENDING.name());
					return cs;
				}, List.of());

		}
		catch (Exception e)
		{
			LOGGER.info("This exceeding shares already has been saved");
		}
	}

	@Override
	public ExceedingShares getExceedingSharesByTickerAndRequestId(int requestId,
																					  String ticker)
	{
		ExceedingShares exceedingShares = getExceedingSharesByRequestId(requestId);
		if (exceedingShares == null)
		{
			setRequestId(requestId, ticker);
		}
		return getExceedingSharesByRequestId(requestId);
	}


	private ExceedingShares getExceedingSharesByRequestId(int requestId)
	{
		return jdbc.queryForObject(Queries.GET_EXCEEDING_SHARES_BY_REQUEST_ID, (rs, rowNum) -> fromResultSet(rs), requestId);
	}

	@Override
	public void setRequestId(int requestId,
							  String ticker)
	{
		jdbc.update(Queries.SET_REQUEST_ID, requestId, ticker);
	}

	private ExceedingShares fromResultSet(ResultSet rs) throws SQLException
	{
		return new ExceedingShares(
				rs.getInt("id"),
				rs.getString("ticker"),
				rs.getInt("quantity"),
				rs.getBigDecimal("price")
		);
	}

	private static class Queries
	{

		public static final String SET_REQUEST_ID =
				"""
				  UPDATE exceeding_shares SET request_id = ? WHERE request_id IS NULL AND ticker=?;
				""";

		public static final String GET_EXCEEDING_SHARES_BY_REQUEST_ID =
				"""
				  SELECT e.id, e.ticker, e.quantity, e.price
				  FROM exceeding_shares as e
				  WHERE request_id=?
				""";
	}
}
