package consumer.controller;


import consumer.feign.ProviderFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author vozrr
 */
@RestController
public class IndexController {

    @Autowired
    private ProviderFeign providerFeign;

    @GetMapping("/init")
    public String init(HttpServletResponse response){
        Cookie cookie1 = new Cookie("token1", "vozrr");
        Cookie cookie2 = new Cookie("token2", "test");
        response.addCookie(cookie1);
        response.addCookie(cookie2);
        return "OK";
    }

    @GetMapping("/")
    public String index(){
        String info = providerFeign.info();
        return "服务调用者调用，获取信息为：" + info;
    }

}
