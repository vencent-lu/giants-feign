# giants-feign 使用手册

本手册从零开始，演示一个「订单服务」调用「用户服务」的完整场景，覆盖 giants-feign 的全部扩展能力。
概览与配置项说明见 [README](../README.md)。

## 目录

- [前置准备](#前置准备)
- [步骤 1：启用 Feign](#步骤-1启用-feign)
- [步骤 2：定义 Feign 客户端](#步骤-2定义-feign-客户端)
- [步骤 3：使用复杂对象 GET 参数](#步骤-3使用复杂对象-get-参数)
- [步骤 4：跨服务异常透传](#步骤-4跨服务异常透传)
- [步骤 5：多版本隔离（本地调试）](#步骤-5多版本隔离本地调试)
- [步骤 6（可选）：开启调用耗时统计](#步骤-6可选开启调用耗时统计)
- [常见问题](#常见问题)

## 前置准备

引入 Maven 依赖，配置类会随 Spring Boot 自动装配，无需额外注解。

```xml
<dependency>
    <groupId>com.github.vencent-lu</groupId>
    <artifactId>giants-feign</artifactId>
    <version>1.1.3</version>
</dependency>
```

## 步骤 1：启用 Feign

在 Spring Boot 启动类上开启 Feign 客户端扫描。giants-feign 的配置类已随依赖自动装配，无需额外注解。

```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.order.feign")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

## 步骤 2：定义 Feign 客户端

```java
@FeignClient(name = "giants-user-server")
public interface UserFeignClient {

    // 普通 POST 调用
    @PostMapping("/user/create")
    UserVO create(@RequestBody UserCreateDTO dto);

    // 简单参数 GET 调用
    @GetMapping("/user/get")
    UserVO getById(@RequestParam("id") Long id);

    // 复杂对象作为 GET 参数：使用 @SpringQueryMap 触发 BeanQueryMapNestEncoder
    @GetMapping("/user/search")
    List<UserVO> search(@SpringQueryMap UserQueryDTO query);
}
```

`@FeignClient` 的 `name` 需要与配置文件 `client-version-map` 中的 key 保持一致，版本隔离才能命中。

## 步骤 3：使用复杂对象 GET 参数

`BeanQueryMapNestEncoder` 已被自动注册为全局 `QueryMapEncoder`，所有用 `@SpringQueryMap` 标注的对象参数都会经它编码。假设查询对象定义如下：

```java
public class UserQueryDTO {
    private String keyword;
    private List<Long> deptIds;      // 简单类型集合
    private PageDTO page;            // 嵌套对象

    @Param("kw")                     // 使用 feign.Param 自定义参数名
    public String getKeyword() { return keyword; }
    // 其余 getter/setter 省略
}

public class PageDTO {
    private int pageNo;
    private int pageSize;
    // getter/setter 省略
}
```

调用 `search(query)` 时，对象会被编码为如下查询串（属性为 `null` 时自动忽略）：

```
/user/search?kw=张三&deptIds=10,20,30&page.pageNo=1&page.pageSize=20
```

编码规则：

- 简单属性：直接编码为 `key=value`。
- 简单类型集合：拼接为逗号分隔字符串，如 `deptIds=10,20,30`。
- 嵌套对象：递归展开为 `父属性.子属性`，如 `page.pageNo=1`。
- `@feign.Param` 注解：覆盖默认参数名（默认取属性名）。

## 步骤 4：跨服务异常透传

要让服务端异常在调用端被还原为原始异常对象，需要两端约定同一状态码（默认 `600`）。

**服务端**：将异常序列化为约定状态码的 JSON 响应（配合 `com.giants.web.springmvc.resolver.JsonExceptionResolver`），响应体为异常对象的 JSON。

**调用端**：`FeignExceptionDecoder` 已自动注册。当收到状态码 `600` 的响应时，会把响应体反序列化为 `Exception` 并抛出，因此可以直接 try-catch 目标异常：

```java
try {
    UserVO user = userFeignClient.getById(id);
} catch (BizException e) {
    // 服务端抛出的 BizException 在此被原样还原
    log.warn("远程调用业务异常: {}", e.getMessage());
}
```

如需修改约定状态码，两端同步调整即可：

```yaml
giants:
  feign:
    response-exception-status: 600
```

> 反序列化依赖 fastjson，请确保异常类型可被 fastjson 正常序列化/反序列化（具备无参构造器、标准 getter/setter）。若反序列化失败，会回退到 Feign 默认解码器并打印错误日志。

## 步骤 5：多版本隔离（本地调试）

场景：公用测试环境部署了 `giants-user-server`，你想在本地只启动某个版本的实例进行联调，而不影响他人。

在调用方配置中为目标服务指定版本号：

```yaml
giants:
  feign:
    client-version-map:
      giants-user-server: 1.0.0
```

此时 `GiantsFeignTargeter` 会把目标地址后缀改写为 `-1-0-0`（`.` 替换为 `-`），路由到对应版本的实例；未在 map 中列出的服务保持默认行为。

> 该机制只负责改写目标地址后缀，实际的实例路由需配合注册中心的分组/元数据策略。若你需要自定义路由逻辑，可自行声明一个 `org.springframework.cloud.openfeign.Targeter` Bean 覆盖默认实现（`createFeignTargeter` 带 `@ConditionalOnMissingBean`）。

## 步骤 6（可选）：开启调用耗时统计

`GiantsInvocationHandlerFactory` 默认不注册。如需统计 Feign 接口耗时，声明如下 Bean：

```java
@Configuration
public class FeignProfilerConfig {

    @Bean
    public InvocationHandlerFactory giantsInvocationHandlerFactory() {
        // 第一个参数 logCallStackTimeAnalyse：是否统计调用堆栈耗时
        // 第二个参数 logArguments：是否将方法入参记录进堆栈信息
        return new GiantsInvocationHandlerFactory(true, true);
    }
}
```

统计仅在当前线程存在 `ExecutionTimeProfiler` 上下文时生效（由 giants-analyse 提供）。耗时数据会在 profiler 的调用堆栈中输出，方法名格式为 `全限定类名.方法名(入参...)`。

## 常见问题

**Q：引入依赖后没有生效？**
确认启动类已加 `@EnableFeignClients`，且未通过 `spring.autoconfigure.exclude` 排除 `GiantsFeignConfiguration`。

**Q：版本隔离配了但没命中？**
检查 `client-version-map` 的 key 是否与 `@FeignClient` 的 `name` 完全一致，并确认注册中心侧存在对应版本后缀的实例。

**Q：异常透传后 catch 不到原始类型？**
确认服务端返回的状态码与 `response-exception-status` 一致，且异常类在调用端 classpath 中存在、可被 fastjson 反序列化。

**Q：如何覆盖框架默认的某个 Bean？**
`Targeter` 带 `@ConditionalOnMissingBean`，声明同类型 Bean 即可覆盖；其余 Bean（`Feign.Builder`、`QueryMapEncoder`、`ErrorDecoder`）如需自定义，可在自己的配置类中声明同名/同类型 Bean 或调整装配顺序。

