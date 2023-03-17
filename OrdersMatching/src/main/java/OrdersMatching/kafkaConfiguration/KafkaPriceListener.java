package OrdersMatching.kafkaConfiguration;

import OrdersMatching.core.MatchingOrdersAlgorithm;
import OrdersMatching.kafkaConfiguration.models.InstrumentPrice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ComponentScan
public class KafkaPriceListener
{

	private final MatchingOrdersAlgorithm matcher;
	private final ObjectMapper mapper;

	public KafkaPriceListener(MatchingOrdersAlgorithm service,
							  ObjectMapper mapper)
	{
		this.matcher = service;
		this.mapper = mapper;
	}

	@RetryableTopic(
	attempts = "4",
	backoff = @Backoff(delay = 1000, multiplier = 2.0),
	topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
	@KafkaListener(topics = "quotes.raw.equity", groupId = "groupId2", containerFactory = "factory")
	void listener(byte[] data) throws IOException
	{
		long start = System.currentTimeMillis();
		InstrumentPrice price = mapper.readValue(data, InstrumentPrice.class);
		System.out.println("Start matching:" + price.ticker());
		matcher.matchOrders(price);
		System.out.println("Your time is: " + (System.currentTimeMillis() - start));
		System.out.println("Exit matching");
	}
}
