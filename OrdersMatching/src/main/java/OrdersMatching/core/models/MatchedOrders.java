package OrdersMatching.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchedOrders implements Serializable
{

	private int id;
	private Order firstOrder;
	private Order secondOrder;
}
