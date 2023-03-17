package OrdersMatching.kafkaConfiguration;

import OrdersMatching.core.OrderService;
import OrdersMatching.core.models.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ComponentScan
public class KafkaOrdersListener
{

	private final OrderService service;
	private final ObjectMapper mapper;

	public KafkaOrdersListener(OrderService service,
							   ObjectMapper mapper)
	{
		this.service = service;
		this.mapper = mapper;
	}

	@KafkaListener(topics = "order", groupId = "groupId1", containerFactory = "batchFactory")
	@Retryable(value = { Exception.class },
			   maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
	void listener(List<byte[]> data)
	{
		long start = System.currentTimeMillis();
		List<Order> orders = data.stream()
								 .map(bytes -> {
									 try 
									 {
										 return mapper.readValue(bytes, Order.class);
									 }
									 catch (IOException e)
									 {
										 throw new RuntimeException("Error converting byte[] to Order object", e);
									 }
								 })
								 .collect(Collectors.toList());

		System.out.println("Start :" + orders.size());
		service.addOrders(orders);
		System.out.println("Your time is: " + (System.currentTimeMillis() - start));
		System.out.println("\nExit");
	}
}
