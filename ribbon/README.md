# Ribbon负载均衡策略

Ribbon负载均衡策略：（摘抄）

| 策略名称                                 | 策略对应的类名            | 实现                                                         |
| :--------------------------------------- | :------------------------ | :----------------------------------------------------------- |
| 轮询策略（默认）                         | RoundRobinRule            | 轮询策略表示每次都顺序取下一个 provider，比如一共有 5 个provider，第 1 次取第 1 个，第 2次取第 2 个，第 3 次取第 3 个，以此类推 |
| 权重轮询策略                             | WeightedResponseTimeRule  | 1.根据每个 provider 的响应时间分配一个权重，响应时间越长，权重越小，被选中的可能性越低。2.原理：一开始为轮询策略，并开启一个计时器，每 30 秒收集一次每个 provider 的平均响应时间，当信息足够时，给每个 provider附上一个权重，并按权重随机选择provider，高权越重的 provider会被高概率选中。 |
| 随机策略                                 | RandomRule                | 从 provider 列表中随机选择一个provider                       |
| 最少并发数策略                           | BestAvailableRule         | 选择正在请求中的并发数最小的 provider，除非这个provider 在熔断中。 |
| 在“选定的负载均衡策略”基础上进行重试机制 | RetryRule                 | 1.“选定的负载均衡策略”这个策略是轮询策略RoundRobinRule2.该重试策略先设定一个阈值时间段，如果在这个阈值时间段内当选择 provider 不成功，则一直尝试采用“选定的负载均衡策略：轮询策略”最后选择一个可用的provider |
| 可用性敏感策略                           | AvailabilityFilteringRule | 过滤性能差的 provider,有 2种：第一种：过滤掉在 eureka 中处于一直连接失败 provider 第二种：过滤掉高并发的 provider |
| 区域敏感性策略                           | ZoneAvoidanceRule         | 1.以一个区域为单位考察可用性，对于不可用的区域整个丢弃，从剩下区域中选可用的provider2.如果这个 ip 区域内有一个或多个实例不可达或响应变慢，都会降低该 ip 区域内其他 ip 被选中的权重。 |

![image-20210315111548298](https://gitee.com/vozrr/blog-img/raw/master/image-20210315111548298.png)

### IRule接口

IRule接口是定义负载均衡规则的接口，可以通过实现该接口，重写其中的自定义负载均衡策略

```java
public interface IRule{
    
    public Server choose(Object key);
    
    public void setLoadBalancer(ILoadBalancer lb);
    
    public ILoadBalancer getLoadBalancer();    
}
```



### RoundRobinRule 轮询策略

以最简单的轮询策略为例，其中的choose()方法通过incrementAndGetModulo()方法获取下一个请求，该方法中对nextServerCyclicCounter进行加一然后对服务数量取模获取下一个服务索引；

```java
//维护要获取的服务索引
private AtomicInteger nextServerCyclicCounter;

public Server choose(ILoadBalancer lb, Object key) {
    if (lb == null) {
        log.warn("no load balancer");
        return null;
    }

    Server server = null;
    int count = 0;
    //循环获取10次，若仍未获取成功，返回null
    while (server == null && count++ < 10) {
        //获取可用实例
        List<Server> reachableServers = lb.getReachableServers();
        //获取全部实例
        List<Server> allServers = lb.getAllServers();
        int upCount = reachableServers.size();
        int serverCount = allServers.size();

        if ((upCount == 0) || (serverCount == 0)) {
            log.warn("No up servers available from load balancer: " + lb);
            return null;
        }
        //得到下一个服务实例索引，从服务列表中获取出来
        int nextServerIndex = incrementAndGetModulo(serverCount);
        server = allServers.get(nextServerIndex);

        if (server == null) {
            Thread.yield();
            continue;
        }

        if (server.isAlive() && (server.isReadyToServe())) {
            return (server);
        }

        // Next.
        server = null;
    }

    if (count >= 10) {
        log.warn("No available alive servers after 10 tries from load balancer: "
                 + lb);
    }
    return server;
}

private int incrementAndGetModulo(int modulo) {
    for (;;) {
        int current = nextServerCyclicCounter.get();
        //按顺序获取下一个实例位置
        int next = (current + 1) % modulo;
        if (nextServerCyclicCounter.compareAndSet(current, next))
            return next;
    }
}
```

### 自定义负载均衡策略

通过实现IRule接口的choose()方法实现自定义的负载均衡方法，由于AbstractLoadBalancerRule实现了IRule接口，可以通过继承AbstractLoadBalancerRule实现自定义负载均衡；

```java
package consumer.config;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vozrr
 */
public class MyRule extends AbstractLoadBalancerRule {

    private final Logger log = LoggerFactory.getLogger(MyRule.class);

    private final AtomicInteger nextServerCounter;

    /**
     * 每个节点连续调用次数 frequency
     */
    private static final int FREQUENCY = 2;

    public MyRule() {
        nextServerCounter = new AtomicInteger(0);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
        ILoadBalancer loadBalancer = getLoadBalancer();
        return choose(loadBalancer, key);
    }

    private Server choose(ILoadBalancer loadBalancer, Object key) {
        if (loadBalancer == null) {
            return null;
        }
        Server server = null;
        int count = 0;
        int num = 10;
        while (count++ < num){
            List<Server> allServers = loadBalancer.getAllServers();
            List<Server> reachableServers = loadBalancer.getReachableServers();
            int allCount = allServers.size();
            int upCount = reachableServers.size();
            if(allCount == 0 || upCount == 0){
                log.warn("无可用服务");
                return null;
            }
            int nextIndex = getNextServerIndex(allCount);
            server = allServers.get(nextIndex);
            if(server == null){
                Thread.yield();
                continue;
            }
            if(server.isAlive() && server.isReadyToServe()){
                return server;
            }
            if(count >= 10){
                log.warn("未找到可用服务");
            }
        }
        return server;
    }

    /**
     * 获取下一个服务实例在服务列表中的索引
     * 每个服务实例连续调用 FREQUENCY 次；
     * 如 FREQUENCY = 2，则对于实例列表[1,2],则调用顺序为{1,1,2,2,1,1....}
     * @param allCount 服务总数
     * @return 下一索引位置
     */
    private int getNextServerIndex(int allCount) {
        int current, next;
        int size = FREQUENCY * allCount;
        do {
            current = nextServerCounter.get();
            next = (current + 1) % size;
        }while (!nextServerCounter.compareAndSet(current, next));
        return next / FREQUENCY;
    }
}
```

### 使用自定义负载均衡策略

在启动类上标注@RibbonClient注解即可指定相应的负载均衡策略

```java
@SpringBootApplication
@EnableDiscoveryClient
//指定对service-provider服务使用自定义负载均衡策略
@RibbonClient(name = "service-provider", configuration = MyRule.class)
public class RibbonConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RibbonConsumerApplication.class, args);
	}

}
```

### idea启动多份同一服务

+ 可在idea的services中右键需要启动多份的应用，选择复制配置：

![image-20210316110432977](https://gitee.com/vozrr/blog-img/raw/master/image-20210316110432977.png)

+ 修改配置名称和端口号，以防止项目在启动中端口冲突：

![image-20210316110720497](https://gitee.com/vozrr/blog-img/raw/master/image-20210316110720497.png)

+ 点击ok，即可在services中启动了。