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
    private static final int FREQUENCY = 1;

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