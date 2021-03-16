# openfeign服务调用

### openfeign介绍

在nacos服务调用中使用的是RestTemplate进行远程服务调用，但在实际开发中使用RestTemplate进行调用比较麻烦，同时一个微服务对另一服务的一个接口调用可能不止一处，那么我们需要在多处使用RestTemplate对同一个接口进行调用，这样无疑增加了代码量，同时，当接口发生变化时，需要维护多处服务调用代码。

openfeign就是为了解决这一问题，它通过定义一个远程调用的接口，在接口中定义远程调用的方法，当应用需要调用远程接口时，只需注入定义的这个接口，调用接口的方法即可实现对远程的调用。

### openfeign使用

+ 使用openfeign需导入openfeign相关依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

+ 例如，我们provider服务有一个远程方法可以获取到服务的相关信息：

```java
@RestController
@RequestMapping("/index")
public class IndexController {
    @GetMapping("/info")
    public String info(HttpServletRequest request) {
        return "服务提供者";
    }
}
```

+ 这时候另一个服务consumer需要调用该方法获取到相关信息：

```java
@RestController
public class IndexController {
    @GetMapping("/")
    public String index(){
        // 调用远程服务 provider 的 /index/info 获取相关信息
        String info = getFromProvider();
        return "服务调用者调用，获取信息为：" + info;
    }

}
```

+ 当我们使用openfeign时，首先我们需要定义一个接口：

  接口使用注解@FeignClient标注，FeignClient中指定远程调用服务的服务名，接口中的方法使用SpringMVC的注解指定远程服务的接口全路径，这样openfeign便能够通过服务名和接口路径构造请求，进行远程调用了，将这个接口注入到IOC容器中，需要使用该接口时将其注入便可以使用。

```java
@FeignClient("provider")
@Component
public interface ProviderFeign {
    @GetMapping("/index/info")
    String info();
}
```

+ 为能够使用openfeign，我们需要在调用服务的启动类上标注@EnableFeignClients注解；
+ 回到我们consumer服务远程调用的位置：将接口注入，在需要的地方调用接口中的方法即可；

```java
@RestController
public class IndexController {
    //注入openfeign接口
    @Autowired
    private ProviderFeign providerFeign;

    @GetMapping("/")
    public String index(){
        //调用接口中的方法进行远程调用
        String info = providerFeign.info();
        return "服务调用者调用，获取信息为：" + info;
    }

}
```

### openfeign请求头丢失问题

当被调用的接口需要某些权限才能调用，那么就需要获取这个请求携带的验证信息如token，而openfeign发起调用不会包含用户请求的相关token等信息，这也就是所谓的openfeign发起请求的请求头丢失问题。

首先要清楚openfeign如何进行远程调用的，openfeign会在调用接口方法时，根据需要调用的服务名和服务api路径构造一个请求，也就是说这个请求是服务调用方生成的一个新请求而非用户的请求，那么这个请求自然不会包含用户请求头的相关信息。

那么解决方法也就很明显了，我们需要在openfeign构造的新请求中添加上用户请求的请求头信息，openfeign提供了一个RequestInterceptor接口，我们可以实现接口中的apply()方法，将用户请求的请求头信息添加到RequestTemplate中，以请求头中的Cookie为例，其他请求头信息可通过同样方式设置。

```java
@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String cookies = request.getHeader("Cookie");
            requestTemplate.header("Cookie", cookies);
        };
    }
}
```

将RequestInterceptor注入到IOC容器中后，openfeign在构造请求时会执行这个apply()方法，这样构造的请求就包含了请求头信息。openfeign在构造请求时会执行下面的代码。

```java
Request targetRequest(RequestTemplate template) {
    Iterator var2 = this.requestInterceptors.iterator();
	//遍历所有的RequestInterceptor，依次调用apply()方法
    while(var2.hasNext()) {
        RequestInterceptor interceptor = (RequestInterceptor)var2.next();
        interceptor.apply(template);
    }

    return this.target.apply(template);
}
```

### openfeign与负载均衡

openfeign本身使用了Ribbon做负载均衡，默认采用轮询策略，需要修改负载均衡策略或者自定义负载均衡按照ribbon设置即可。