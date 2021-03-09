package top.vozrr.springcloud.nacosconfig.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class IndexController {
    @Value("${website.name}")
    private String name;

    @Value("${website.info}")
    private String info;

    @Value("${mysql.info}")
    private String mysqlInfo;

    @Value("${logging.info}")
    private String loggingInfo;

    @GetMapping("/")
    public String index(){
        return info + ", " + name + ", 数据库信息" + mysqlInfo + ", 日志信息：" + loggingInfo;
    }
}
