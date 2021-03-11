package top.vozrr.springcloud.nacosdiscoveryprovider.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/{info}")
    public String index(@PathVariable("info") String info){
        return "provider方法, 调用方信息：" + info;
    }
}
