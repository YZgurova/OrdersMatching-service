package OrdersMatching.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class RedissonClientSingleton
{

	private static RedissonClient redisson;

	static
	{
//		Config config = new Config();
//		config.useSentinelServers()
//			  .setMasterName("mymaster")
//			  .addSentinelAddress("redis://127.0.0.1:26384")
//			  .addSentinelAddress("redis://127.0.0.1:26385")
//			  .setCheckSentinelsList(false);

		Config config = new Config();
		config.useMasterSlaveServers()
			  .setMasterAddress("redis://127.0.0.1:6379")
			  .addSlaveAddress("redis://127.0.0.1:6380", "redis://127.0.0.1:6381")
			  .setConnectTimeout(10000)
			  .setMasterConnectionPoolSize(100)
			  .setSlaveConnectionPoolSize(100);

		redisson = Redisson.create(config);
	}

	public static RedissonClient getRedissonClient()
	{
		return redisson;
	}


	public static void shutdown()
	{
		redisson.shutdown();
	}
}