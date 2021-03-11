package top.vozrr.springcloud.nacosdiscoveryprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NacosDisCoveryProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosDisCoveryProviderApplication.class, args);
	}

}
