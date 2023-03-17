package OrdersMatching.bin.kafkaWorker;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
		"OrdersMatching.beans",
		"OrdersMatching.kafkaConfiguration",
		"OrdersMatching.cronjob"
})
@EnableKafka
@EnableCaching
public class Main
{

	public static void main(String[] args)
	{
		new SpringApplicationBuilder(Main.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}
}
