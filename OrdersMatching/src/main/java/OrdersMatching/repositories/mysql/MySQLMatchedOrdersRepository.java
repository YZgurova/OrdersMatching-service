package OrdersMatching.repositories.mysql;

import OrdersMatching.core.models.MatchedOrders;
import OrdersMatching.core.models.Order;
import OrdersMatching.repositories.MatchedOrdersRepository;
import OrdersMatching.repositories.OrderRepository;
import OrdersMatching.repositories.enums.ExceedingSharesMatchedOrdersStatus;
import OrdersMatching.repositories.enums.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MySQLMatchedOrdersRepository implements MatchedOrdersRepository
{

	private final TransactionTemplate txTemplate;
	private final JdbcTemplate jdbc;
	private final OrderRepository orderRepository;

	public MySQLMatchedOrdersRepository(TransactionTemplate txTemplate,
										JdbcTemplate jdbcT,
										final OrderRepository orderRepository)
	{
		this.txTemplate = txTemplate;
		this.jdbc = jdbcT;
		this.orderRepository = orderRepository;
	}

	@Override
	public void addListOfMatched(List<MatchedOrders> orders,
								 List<Order> indexes)
	{
		txTemplate.executeWithoutResult(status -> {
			jdbc.update(conn -> {
				PreparedStatement ps = conn.prepareStatement(Queries.ADD_MATCHED);
				int oSize = orders.size();
				for (int i = 0; i < oSize; i++)
				{
					ps.setInt(1, orders.get(i).getFirstOrder().getOrderId());
					ps.setInt(2, orders.get(i).getSecondOrder().getOrderId());
					if (i != oSize - 2)
					{
						ps.addBatch();
					}
				}
				ps.executeBatch();
				return ps;
			});
			orderRepository.updateStatus(indexes, OrderStatus.EXECUTED.name());
		});
	}

	@Override
	public List<MatchedOrders> getOrderMatches(int orderId)
	{
		return jdbc.query(Queries.GET_MATCHED_BY_ID, (rs, rowNum) -> fromResultSet(rs), orderId, orderId);
	}

	@Override
	public void updateStatus(List<MatchedOrders> indexes,
							  String status,
							  int requestId)
	{
		jdbc.update(conn -> {
			List<Integer> list = indexes.stream().map(MatchedOrders::getId).toList();
			final String parameters = list.stream().map(i -> "?").collect(Collectors.joining(",", "(", ")"));
			PreparedStatement ps = conn.prepareStatement("UPDATE matched_orders SET `status` = 'SENТ', `request_id` = ? WHERE `id` IN " + parameters);
			ps.setInt(1, requestId);
			int iSize = indexes.size();
			for (int i = 2; i <= iSize+1; i++)
			{
				ps.setInt(i, indexes.get(i - 2).getId());
			}
			return ps;
		});
	}

	@Override
	public List<MatchedOrders> getMatchedOrders(int requestId)
	{
		List<MatchedOrders> matchedOrders = getMatchedOrdersByRequestId(requestId);
		if (matchedOrders.size() == 0)
		{
			txTemplate.execute(status -> {
				List<MatchedOrders> unsentMatchedOrders = jdbc.query(Queries.GET_UNSENT_MATCHED, (rs, rowNum) -> fromResultSet(rs), ExceedingSharesMatchedOrdersStatus.PENDING.name());
				if (unsentMatchedOrders.size() > 0)
				{
					updateStatus(unsentMatchedOrders, ExceedingSharesMatchedOrdersStatus.SENT.name(), requestId);
				}
				return unsentMatchedOrders;
			});
		}
		return matchedOrders;
	}

	private List<MatchedOrders> getMatchedOrdersByRequestId(int requestId)
	{
		return jdbc.query(Queries.GET_MATCHED_BY_REQUEST_ID, (rs, rowNum) -> fromResultSet(rs), requestId);
	}

	private MatchedOrders fromResultSet(ResultSet rs) throws SQLException
	{
		return new MatchedOrders(
				rs.getInt("id"),
				new Order(
						rs.getInt("first_account_id"),
						rs.getInt("first_order_id"),
						rs.getString("first_ticker"),
						rs.getInt("first_quantity")),
				new Order(
						rs.getInt("second_account_id"),
						rs.getInt("second_order_id"),
						rs.getString("second_ticker"),
						rs.getInt("second_quantity"))
		);
	}

	private static class Queries
	{

		public static final String ADD_MATCHED =
				"""
						INSERT INTO matched_orders(first_order_id, second_order_id) VALUES (?,?)
						""";

		public static final String GET_UNSENT_MATCHED =
				"""
						SELECT m.id, f.account_id as 'first_account_id', f.order_id as 'first_order_id', f.ticker as 'first_ticker', f.quantity as 'first_quantity',
						s.account_id as 'second_account_id', s.order_id as 'second_order_id', s.ticker as 'second_ticker', s.quantity as 'second_quantity'
						FROM matched_orders as m
						JOIN orders as f
						ON  f.order_id = m.first_order_id
						JOIN orders as s
						ON s.order_id = m.second_order_id
						WHERE m.status=?;
						""";

		public static final String GET_MATCHED_BY_ID =
				"""
						SELECT m.id, f.account_id as 'first_account_id', f.order_id as 'first_order_id', f.ticker as 'first_ticker', f.quantity as 'first_quantity',
						s.account_id as 'second_account_id', s.order_id as 'second_order_id', s.ticker as 'second_ticker', s.quantity as 'second_quantity'
						FROM matched_orders as m
						JOIN orders as f
						ON  f.order_id = m.first_order_id
						JOIN orders as s
						ON s.order_id = m.second_order_id
						WHERE m.first_order_id=? OR m.second_order_id=?;
						""";

		public static final String GET_MATCHED_BY_REQUEST_ID =
				"""
						SELECT m.id, f.account_id as 'first_account_id', f.order_id as 'first_order_id', f.ticker as 'first_ticker', f.quantity as 'first_quantity',
						s.account_id as 'second_account_id', s.order_id as 'second_order_id', s.ticker as 'second_ticker', s.quantity as 'second_quantity'
						FROM matched_orders as m
						JOIN orders as f
						ON  f.order_id = m.
						first_order_id
						JOIN orders as s
						ON s.order_id = m.
						second_order_id
						WHERE m.request_id=?;
						""";
	}
}
