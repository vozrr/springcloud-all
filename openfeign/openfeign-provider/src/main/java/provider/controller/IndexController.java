package provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author vozrr
 */
@RestController
@RequestMapping("/index")
public class IndexController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/info")
    public String info(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        StringBuilder str = new StringBuilder();
        if(cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                str.append(cookie.getValue()).append(",");
            }
        }
        return "服务提供者" + str.toString() + "。端口号：" + port;
    }
}
