package OrdersMatching.repositories.mysql;

import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MySQLExceedingSharesRepositoryTest {
    MySQLExceedingSharesRepository exceedingSharesRepository;
    @Before
    public void setUp() throws Exception {
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3307/orders_matching","root","root");
        DataSource dataSource = new SingleConnectionDataSource(conn, false);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        MySQLOrderRepository orderRepository = new MySQLOrderRepository(jdbcTemplate);
        exceedingSharesRepository = new MySQLExceedingSharesRepository(txTemplate, jdbcTemplate, orderRepository);
    }
    @Test
    public void addExceedingShares() {
//        Assert.assertEquals(exceedingSharesRepository.addExceedingShares("tesla", 10, new BigDecimal(198.98)));
    }
    MySQLExceedingSharesRepositoryTest() throws SQLException {
    }
}