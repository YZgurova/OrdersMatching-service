package OrdersMatching.bin.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
		"OrdersMatching.beans",
		"OrdersMatching.rest.api"
})

@EnableKafka
@EnableCaching
public class OrdersMatchingApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(OrdersMatchingApplication.class, args);
	}
}
