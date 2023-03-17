package OrdersMatching.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceedingShares implements Serializable
{

	private int id;
	private String ticker;
	private int quantity;
	private BigDecimal price;
}
