package top.vozrr.springcloud.provider.controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/{info}")
    public String index(@PathVariable("info") String info){
        return "调用provider方法, 该服务端口为：" + port +" 调用方信息：" + info;
    }
}
