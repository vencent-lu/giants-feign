# giants-feign
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.vencent-lu/giants-feign/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.vencent-lu/giants-feign)

feign 功能扩展，满足spring cloud RPC 调用需求, giants-feign 依赖于 spring-cloud-starter-openfeign 的自动配置基本功能。

## 扩展功能说明

#### 增加Feign客户端版本号支持
增加版本号功能，对不同环境进行隔离，同时也便于本地调试，本地local环境只需要启动需要调试的服务，同时不影响公用测试环境调用链。
通过 giants.feign.client-version-map 指定feign client 对应该版本号

#### Error Decoder
错误解码器，当调用Feign接口出现错误时调用解码器 进行解码，解码器列表如下：
* com.giants.feign.codec.FeignExceptionDecoder : 根据返回http状态码进行异常解码，默认异常状态码为600，配合 com.giants.web.springmvc.resolver
.JsonExceptionResolver 使用。

#### Query Map Encoder
GET请示参数编码器，编码器列表如下：
* com.giants.feign.querymap.BeanQueryMapNestEncoder : 嵌套复杂属性编码器，支持复杂对象通过GET请求参数转递。

#### Invocation Handler
调用处理器，Feign代理实例调用接口时执行 调用处理器，调用处理器列表如下：
* com.giants.feign.proxy.GiantsInvocationHandler : 记录Feign接口执行时间，配合 com.giants.analyse.profiler.ExecutionTimeProfiler
 打印调用方法堆栈时间统计使用。
 
## 项目集成

#### 在项目中集成 giants-feign
引入maven 依赖，项目会自动加载 giants-feign 相关配置类，maven依赖配置如下：
```xml
<dependency>
    <groupId>com.github.vencent-lu</groupId>
    <artifactId>giants-feign</artifactId>
    <version>1.1.0</version>
</dependency>
```

#### giants-feign 配置说明
支持 yaml 与 properties 两种配置方式，以yml举例，配置说明如下：
```yaml
giants:
  feign:
    # FeignExceptionDecoder 反序列化异常对应状态码 默认值 600
    response-exception-status: 600
    # 指定feign client 对应该版本号
    client-version-map: {
      giants-auth-server: 1.0.0
    }
```