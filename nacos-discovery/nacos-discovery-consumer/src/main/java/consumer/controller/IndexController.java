package consumer.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class IndexController {

    /**
     * 这里向ioc中注入RestTemplate,后续使用RestTemplate发起服务调用；
     * LoadBalanced注解标识将解析estTemplate中的服务名，对目标服务发起调用
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient balancerClient;

    @GetMapping("/")
    public String index(){
        //获取服务提供者实例
        //ServiceInstance instance = balancerClient.choose("service-provider");
        //服务Host信息
        //String host = instance.getHost();
        //服务端口信息
        //int port = instance.getPort();
        //使用restTemplate对service-provider服务发起请求
        //String url = String.format("http://%s:%s/服务调用者", host, port);
        //return restTemplate.getForObject(url, String.class);
        //使用restTemplate对service-provider服务发起请求
        return restTemplate.getForObject("http://service-provider/" + "服务调用者",
                String.class);
    }
}
