package consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import consumer.config.MyRule;

@SpringBootApplication
@EnableDiscoveryClient
//指定对service-provider服务使用自定义负载均衡策略
@RibbonClient(name = "service-provider", configuration = MyRule.class)
public class RibbonConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RibbonConsumerApplication.class, args);
	}

}
