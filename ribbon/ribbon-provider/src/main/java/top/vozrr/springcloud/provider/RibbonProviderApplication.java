package top.vozrr.springcloud.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RibbonProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RibbonProviderApplication.class, args);
	}

}
