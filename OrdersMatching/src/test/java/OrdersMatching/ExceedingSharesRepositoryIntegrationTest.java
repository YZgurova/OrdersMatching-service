package OrdersMatching;

import OrdersMatching.core.models.ExceedingShares;
import OrdersMatching.repositories.ExceedingSharesRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

import java.util.Optional;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;

@DataJdbcTest
public class ExceedingSharesRepositoryIntegrationTest
{
//	@Resource
//	private ExceedingSharesRepository subject;
//
//	@AfterEach
//	public void tearDown() throws Exception {
////		subject.deleteAll();
//	}
//
//	@Test
//	public void shouldSaveAndFetchPerson() throws Exception {
//		var exceedingShares = new ExceedingShares();
//		subject.addExceedingShares(exceedingShares.getTicker(), exceedingShares.getQuantity(), exceedingShares.getPrice());
//
//		var maybePeter = subject.getExceedingSharesByTickerAndRequestId("Pan");
//
//		assertThat(maybePeter, is(Optional.of(exceedingShares)));
//	}
}
