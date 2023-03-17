package OrdersMatching.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable
{

	private int accountId;
	private int orderId;
	private String ticker;
	private int quantity;
}
