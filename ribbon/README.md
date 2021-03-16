# Springcloud学习-nacos服务注册调用

项目代码已发布在github上：[springcloud-all](https://github.com/vozrr/springcloud-all)

### 服务注册与发现

​	首先，什么是服务注册发现，在分布式系统中，不同服务之间常常需要互相调用，而一个服务又可能有多份实例运行，那如何管理这些实例呢？这时我们就需要一个服务管理中心，将每一个实例都注册到服务管理中心，这样当一个服务需要调用其他服务时，通过从服务管理中心获取到被调用服务的信息，向其发起调用。

​	例如一个论坛系统中，一个文章系统查询文章时需要获取评论信息，这时需要调用评论系统的相关接口，当我们将服务都注册到服务中心后，文章系统就可以从服务中心获取到评论系统的实例信息，继而发起查询请求；对于多个评论系统实例，我们也可以进行负载均衡，即根据一定规则向评论系统实例发起请求（例如轮询，第一次请求发给一号实例，第二次发给二号实例，依此类推），以保证请求较为合理的分摊给多个评论系统实例，避免其中有些实例无法处理过多请求而宕机；

​	所以，服务注册发现是构成分布式系统的基础，本文将介绍使用nacos作为服务管理中心。

![image-20210311172101969](https://gitee.com/vozrr/blog-img/raw/master/image-20210311172101969.png)

### 使用nacos作为服务注册中心

#### 导入nacos作为注册中心的依赖

```xml
	<!-- spring-cloud-alibaba、spring-boot依赖版本统一管理 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>2.2.3.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.2.5.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
    </dependencies>
```

#### 创建服务提供方

+ 导入依赖后，需要在application.properties中配置相关信息：

```properties
# 服务名称，也作为服务列表中的服务名
spring.application.name=service-provider
# 注册中心地址
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
# 该服务所占端口
server.port=8000
```

+ 在spring boot启动类上标上 @EnableDiscoveryClient 注解

```java
@SpringBootApplication
@EnableDiscoveryClient
public class NacosDisCoveryProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosDisCoveryProviderApplication.class, args);
	}

}
```

+ 编写测试接口

​	现在已经做好相关配置了，项目启动后将自动注册到注册中心，为方便测试，我们编写一个controller，后续通过服务调用方调用这个接口；

```java
@RestController
public class IndexController {

    @GetMapping("/{info}")
    public String index(@PathVariable("info") String info){
        return "provider方法, 调用方信息：" + info;
    }
}
```

#### 创建服务调用方

与服务提供方基本一致；

+ 配置注册中心地址；

  ```properties
  # 服务名称，也作为服务列表中的服务名
  spring.application.name=service-consumer
  # 注册中心地址
  spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
  # 该服务所占端口
  server.port=9000
  ```

+ 在启动类上方标注 @EnableDiscoveryClient 注解；
+ 编写测试controller类用于测试：

```java
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

    @GetMapping("/")
    public String index(){
        //使用restTemplate对service-provider服务发起请求
        return restTemplate.getForObject("http://service-provider/" + "服务调用者", 
                String.class);
    }
}
```

这里也可以使用 LoadBalancerClient 获取服务信息，更易于理解：

```java
@RestController
public class IndexController {
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
        ServiceInstance instance = balancerClient.choose("service-provider");
        //服务Host信息
        String host = instance.getHost();
        //服务端口信息
        int port = instance.getPort();
        //使用restTemplate对service-provider服务发起请求
        String url = String.format("http://%s:%s/服务调用者", host, port);
        return restTemplate.getForObject(url, String.class);
    }
}

```

#### 启动nacos服务

+ 启动nacos服务，作为服务注册中心

#### 启动两个服务

+ 启动服务提供方和服务调用方

#### 测试

​	访问服务调用方 http://localhost:9000 ，可以看到输出了服务提供者信息；

![image-20210311175701065](https://gitee.com/vozrr/blog-img/raw/master/image-20210311175701065.png)

------

以上便是nacos作为服务注册中心的简单使用。