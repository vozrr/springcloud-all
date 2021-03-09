# Springcloud学习-nacos配置中心

### 配置中心简介

在一般单机模式下，配置文件都固定写死在配置文件中，这样配置需要修改时必须停止项目，修改配置文件再重新打包上线，这样无疑耗时耗力，同时在微服务集群情况下，服务众多维护困难，因此需要一个统一的配置管理中心，我们希望能通过修改配置中心的信息，直接作用到与此配置关联的所有服务上，这样将简化整个维护流程。本文介绍使用阿里巴巴的nacos作为配置中心。

### nacos下载启动

+ 参照官网https://nacos.io/zh-cn/docs/quick-start.html

+ 启动nacos服务器：根据第一步的官网说明启动nacos服务(默认nacos以集群方式启动，使用`cmd startup.cmd -m standalone`)以单机模式启动；

+ nacos启动后在浏览器中访问http://localhost:8848/nacos/index.html 会自动跳转至登录页，输入账号密码nacos/nacos即可登录至管理界面：

  ![image-20210309155324294](https://gitee.com/vozrr/blog-img/raw/master/image-20210309155324294.png)

### 新建springboot项目

+ 创建springboot项目，导入nacos配置中心依赖，依赖版本请参考[版本说明](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)；

+ 具体pom.xml配置如下：

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  	<modelVersion>4.0.0</modelVersion>
  	<parent>
  		<groupId>org.springframework.boot</groupId>
  		<artifactId>spring-boot-starter-parent</artifactId>
  		<version>2.2.5.RELEASE</version>
  		<relativePath/> <!-- lookup parent from repository -->
  	</parent>
  	<groupId>top.vozrr.springcloud</groupId>
  	<artifactId>nacos-config</artifactId>
  	<version>0.0.1-SNAPSHOT</version>
  	<name>nacos-config</name>
  	<description>nacos配置中心</description>
  	<properties>
  		<java.version>1.8</java.version>
  	</properties>
  
  	<!-- spring-cloud-alibaba依赖版本统一管理 -->
  	<dependencyManagement>
  		<dependencies>
  			<dependency>
  				<groupId>com.alibaba.cloud</groupId>
  				<artifactId>spring-cloud-alibaba-dependencies</artifactId>
  				<version>2.2.3.RELEASE</version>
  				<type>pom</type>
  				<scope>import</scope>
  			</dependency>
  		</dependencies>
  	</dependencyManagement>
  
  	<dependencies>
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-web</artifactId>
  		</dependency>
  		
  		<!-- 导入nacos-config依赖 -->
  		<dependency>
  			<groupId>com.alibaba.cloud</groupId>
  			<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
  		</dependency>
  
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-test</artifactId>
  			<scope>test</scope>
  		</dependency>
  	</dependencies>
  
  	<build>
  		<plugins>
  			<plugin>
  				<groupId>org.springframework.boot</groupId>
  				<artifactId>spring-boot-maven-plugin</artifactId>
  			</plugin>
  		</plugins>
  	</build>
  
  </project>
  ```



### 在nacos新建配置

打开nacos管理界面，新建一个配置如下，如：

  ![image-20210309155752461](https://gitee.com/vozrr/blog-img/raw/master/image-20210309155752461.png)

  其中nacos-config.yml内容如下：

  ```yaml
  website:
      name: "top.vozrr"
      info: "welcome"
  ```

  点击发布，此时我们就有了一个配置文件；

  ![image-20210309160018427](https://gitee.com/vozrr/blog-img/raw/master/image-20210309160018427.png)



### springboot加载nacos配置文件

springboot启动时加载配置文件优先级如下：

`bootstrap.properties ->bootstrap.yml -> application.properties -> application.yml`，使用application.properties读取配置中心的配置无法初始化到程序中，所以我们采用bootstrap.properties或者bootstrap.yaml进行配置；

我们在resources文件夹下创建bootstrap.yaml文件，内容如下：

```yaml
spring:
  cloud:
    nacos:
      config:
        # nacos地址
        server-addr: 127.0.0.1:8848
        # 配置Data Id
        name: nacos-config
        # 配置文件类型
        file-extension: yaml
```

如此一来便可以在项目启动时读取配置中心的信息了，在项目中使用和读取时与本地没有区别；

### 简单测试

我们在src目录下创建IndexController，编写代码如下：

```java
@RestController
public class IndexController {
    @Value("${website.name}")
    private String name;

    @Value("${website.info}")
    private String info;

    @GetMapping("/")
    public String index(){
        return info + ", " + name;
    }
}
```

启动项目，在浏览器中访问localhost:8080，得到结果如下：

![image-20210309171118190](https://gitee.com/vozrr/blog-img/raw/master/image-20210309171118190.png)

### 动态刷新配置信息

​	在默认情况下，springboot程序在初始化时读取配置中心的配置绑定到属性字段上后便不会更新配置信息（如使用@Value绑定配置信息），这样当配置中心配置修改时，属性无法更新值为最新的配置信息，可以通过`@RefreshScope`注解实现动态刷新，只需将其标在需要获取配置信息的bean类上即可，例如前面的IndexController；

```java
@RestController
@RefreshScope
public class IndexController {
    //...
}
```

通过设置该注解，配置中心修改相关配置后，程序能够获取到新的配置。

这里注意程序默认是能获取到配置中心信息的变化的，只是绑定的属性值没有更新，这一点可以在修改配置信息后从程序控制台日志中看出来，因为配置中`spring.cloud.nacos.config.refresh-enabled`这项配置默认为true。

### 命名空间与分组

​	一个系统中可能存在多个不同类型的配置文件，例如一个论坛系统存在文章服务，评论服务等，此时多个服务的配置文件应该区分开来，同时，一个服务可能存在开发环境、测试环境、生产环境，不同环境的配置文件也应该区分，此时我们可以利用nacos提供的命名空间与分组功能，针对不同微服务，我们为它分配一个命名空间，同一个服务，对于不同环境提供不同的分组，这样我们的配置文件就易于管理了。

​	在nacos中如图创建命名空间：例如此处我们将命名为nacos-config;

![image-20210309173430842](https://gitee.com/vozrr/blog-img/raw/master/image-20210309173430842.png)

这样，我们就添加了一个新的命名空间，回到配置列表，将开始的nacos-config配置文件克隆至刚才的命名空间，同时设置分组为dev;

![image-20210309173752614](https://gitee.com/vozrr/blog-img/raw/master/image-20210309173752614.png)

点击克隆，我们可以看到命名空间为nacos-config命名空间中多了一个配置文件，它的Group为dev：

![image-20210309173935445](https://gitee.com/vozrr/blog-img/raw/master/image-20210309173935445.png)

​	这样不同类型的配置文件得以分开，便于管理，我们重新调整spring boot项目中的相关信息，让它能够正确获取到配置信息，为保证获取的是新的命名空间的配置，我们暂时删除原来public命名空间中的配置信息，修改bootstrap.yaml文件为：

```yaml
spring:
  cloud:
    nacos:
      config:
        # nacos地址
        server-addr: 127.0.0.1:8848
        # 配置Data Id
        name: nacos-config
        # 配置文件类型
        file-extension: yaml
        # 指定命名空间和分组
        namespace: nacos-config
        group: dev
```

即可。

### 读取多个配置信息

有时一个配置文件过长，导致我们无法快速定位到某一处配置，同时我们可能希望不同类型的配置能够单独成不同的配置文件，比如关于数据库信息配置、日志配置等，这样一来我们一个程序需要加载多个配置项。

现在模拟这种情况，我们在nacos-config命名空间下的dev组中新建nacos-config-mysql.yaml、nacos-config-logging.yaml:

nacos-config-mysql.yaml:

```yaml
mysql:
    info: "mysql"
```

nacos-config-logging.yaml:

```yaml
logging:
  info: "logging"
```

信息如下:

![image-20210309195939017](https://gitee.com/vozrr/blog-img/raw/master/image-20210309195939017.png)

注意此处两个配置Data ID都需有后缀结尾，指定配置文件类型。

我们在IndexController中使用@Value读取这两个配置项的信息：

```java
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
```

最重要的是bootstrap.yaml中的配置信息：

```yaml
spring:
  cloud:
    nacos:
      config:
        # nacos地址
        server-addr: 127.0.0.1:8848
        # 配置Data Id
        name: nacos-config
        # 配置文件类型
        file-extension: yaml
        # 指定命名空间和分组
        namespace: nacos-config
        group: dev
        # 此处是一个set集合，可以ctrl+点击extension-configs查看，可以配置多个文件，
        # 每一个Config中包含data-id,group,refresh,refresh默认为false
        extension-configs:
          -
            data-id: nacos-config-mysql.yaml
            group: dev
            refresh: true
          -
            data-id: nacos-config-logging.yaml
            group: dev
            refresh: true
```

注意如果需要实现动态刷新需要将refresh设置为true，在这个基础上@RefreshScope才生效，同时如上文所说，extension-configs中每个data-id需显式指定后缀，以顺利读取配置信息。

-----

以上就是nacos作为配置中心的简单使用，如果存在出入后续再做修正。