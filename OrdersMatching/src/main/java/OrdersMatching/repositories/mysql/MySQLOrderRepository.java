package OrdersMatching.repositories.mysql;

import OrdersMatching.core.models.Order;
import OrdersMatching.repositories.OrderRepository;
import OrdersMatching.repositories.enums.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MySQLOrderRepository implements OrderRepository
{

	private final JdbcTemplate jdbc;
	private final Logger LOGGER;

	public MySQLOrderRepository(JdbcTemplate jdbcT)
	{
		this.jdbc = jdbcT;
		LOGGER = Logger.getLogger(MySQLExceedingSharesRepository.class.getName());
	}

	@Override
	public void addListOfOrders(List<Order> orders)
	{
		try
		{
			jdbc.update(conn -> {
				PreparedStatement ps = conn.prepareStatement(Queries.ADD_ORDER);
				int ordersSize = orders.size();
				for (int i = 0; i < ordersSize; i++)
				{
					ps.setInt(1, orders.get(i).getAccountId());
					ps.setInt(2, orders.get(i).getOrderId());
					ps.setString(3, orders.get(i).getTicker());
					ps.setInt(4, orders.get(i).getQuantity());
					if (i != ordersSize - 1)
					{
						ps.addBatch();
					}
				}
				ps.executeBatch();
				return ps;
			});
		}
		catch (Exception e)
		{
			LOGGER.info("This orders already exist");
		}
	}

	@Override
	public void updateStatus(List<Order> indexes,
							 String status)
	{
		jdbc.update(conn -> {
			List<Integer> list = indexes.stream().map(Order::getOrderId).toList();
			final String parameters = list.stream().map(i -> "?").collect(Collectors.joining(",", "(", ")"));
			String query = "UPDATE orders SET status = '" + status + "' WHERE order_id IN " + parameters;
			PreparedStatement ps = conn.prepareStatement(query);
			int iSize = indexes.size();
			for (int i = 1; i <= iSize; i++)
			{
				ps.setInt(i, indexes.get(i - 1).getOrderId());
			}
			return ps;
		});
	}

	@Override
	public List<Order> getByTicker(String ticker)
	{
		List<Order> orders = jdbc.query(Queries.GET_BY_TICKER, (rs, rowNum) -> fromResultSet(rs), ticker);
		if (orders.size() > 0)
		{
			updateStatus(orders, OrderStatus.PROCESSING.name());
		}
		return orders;
	}

	private Order fromResultSet(ResultSet rs) throws SQLException
	{
		return new Order(
				rs.getInt("account_id"),
				rs.getInt("order_id"),
				rs.getString("ticker"),
				rs.getInt("quantity")
		);
	}


	private static class Queries
	{

		public static final String ADD_ORDER =
				"""
						INSERT INTO orders(account_id, order_id, ticker, quantity) 
						VALUES (?,?,?,?)
						""";
		public static final String GET_BY_TICKER =
				"""
						SELECT account_id, order_id, ticker, quantity FROM orders WHERE ticker=? AND status='PENDING';
						""";
	}
}
