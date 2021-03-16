package consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 27449
 */
@FeignClient("provider")
@Component
public interface ProviderFeign {
    @GetMapping("/index/info")
    String info();
}
