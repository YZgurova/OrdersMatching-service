package OrdersMatching.kafkaConfiguration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig
{

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServer;

	public Map<String, Object> consumerConfig()
	{
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		return props;
	}

	public Map<String, Object> consumerBatchConfig()
	{
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
		return props;
	}

	@Bean
	public ConsumerFactory<byte[], byte[]> consumerFactory()
	{
		return new DefaultKafkaConsumerFactory<>(consumerConfig());
	}

	@Bean
	public ConsumerFactory<byte[], byte[]> consumerBatchFactory()
	{
		return new DefaultKafkaConsumerFactory<>(consumerBatchConfig());
	}

	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<byte[], byte[]>> factory(
			ConsumerFactory<byte[], byte[]> consumerFactory
																									)
	{
		ConcurrentKafkaListenerContainerFactory<byte[], byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}

	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<byte[], byte[]>> batchFactory(
			ConsumerFactory<byte[], byte[]> consumerBatchFactory
																										 )
	{
		ConcurrentKafkaListenerContainerFactory<byte[], byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerBatchFactory);
		factory.setBatchListener(true);
		return factory;
	}
}
