# giants-feign

[![Maven Central](https://img.shields.io/maven-central/v/com.github.vencent-lu/giants-feign.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.github.vencent-lu/giants-feign)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![JDK](https://img.shields.io/badge/JDK-1.8%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
[![Spring Cloud OpenFeign](https://img.shields.io/badge/Spring%20Cloud%20OpenFeign-3.0.x-green.svg)](https://spring.io/projects/spring-cloud-openfeign)

giants-feign 是对 Spring Cloud OpenFeign 的功能扩展，用于满足 Spring Cloud 微服务体系下的 RPC 调用需求。
它基于 `spring-cloud-starter-openfeign` 的自动配置能力，在其之上补充了 **多版本隔离**、**异常透传**、**复杂对象 GET 参数编码**、**调用耗时统计** 等能力。

引入依赖后，相关配置类会通过 Spring Boot 的自动装配机制自动生效，无需额外的注解或代码。

## 核心特性

| 特性 | 说明 | 涉及组件 |
| --- | --- | --- |
| 客户端版本号隔离 | 为不同 Feign 客户端指定版本号，实现环境隔离与本地调试 | `GiantsFeignTargeter` / `DefaultTargeter` |
| 异常解码透传 | 将服务端序列化后的异常在调用端还原为原始异常对象 | `FeignExceptionDecoder` |
| 复杂对象 GET 编码 | 支持嵌套对象、集合作为 GET 请求参数传递 | `BeanQueryMapNestEncoder` |
| 调用耗时统计 | 记录 Feign 接口执行时间并输出调用堆栈耗时 | `GiantsInvocationHandler` |
| 关闭默认重试 | 默认使用 `Retryer.NEVER_RETRY`，避免重复请求 | `GiantsFeignConfiguration` |

## 快速开始

### 1. 引入 Maven 依赖

```xml
<dependency>
    <groupId>com.github.vencent-lu</groupId>
    <artifactId>giants-feign</artifactId>
    <version>1.1.3</version>
</dependency>
```

依赖引入后，`GiantsFeignConfiguration` 会被 Spring Boot 自动装配（见 `META-INF/spring.factories`），
自动注册 `Feign.Builder`、`QueryMapEncoder`、`ErrorDecoder`、`Targeter` 等 Bean。

### 2. 配置说明

支持 `yaml` 与 `properties` 两种配置方式，所有配置以 `giants.feign` 为前缀。以 yml 为例：

```yaml
giants:
  feign:
    # FeignExceptionDecoder 异常反序列化对应的 HTTP 状态码，默认值 600
    response-exception-status: 600
    # 指定 Feign 客户端对应的版本号，Map 类型 -> key: Feign 服务名(name/value)，value: 版本号
    client-version-map:
      giants-auth-server: 1.0.0
      giants-user-server: 2.1.0
```

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `giants.feign.response-exception-status` | Integer | `600` | 触发异常反序列化的响应状态码 |
| `giants.feign.client-version-map` | Map<String,String> | 空 | Feign 服务名到版本号的映射，配置后启用版本隔离 |

> 说明：当 `client-version-map` 为空时，自动装配 `DefaultTargeter`（等价于原生 Feign 行为）；配置了映射时则装配 `GiantsFeignTargeter` 启用版本隔离。

## 扩展功能详解

### 客户端版本号隔离

为不同环境提供隔离能力，同时便于本地调试：本地 local 环境只需启动待调试的服务，
无需启动全部依赖服务，也不会影响公用测试环境的调用链路。

工作原理：通过 `giants.feign.client-version-map` 为某个 Feign 客户端指定版本号后，
`GiantsFeignTargeter` 会在目标服务的 `url` 后拼接 `-<version>`（版本号中的 `.` 会被替换为 `-`）。
例如服务名 `giants-auth-server` 配置版本 `1.0.0`，最终目标地址后缀为 `-1-0-0`，
从而路由到对应版本的实例（需配合注册中心的实例元数据/分组策略使用）。

- 已配置版本号的客户端：使用带版本后缀的目标地址。
- 未配置版本号的客户端：保持原始地址，行为与原生 Feign 一致。

### 异常解码器（Error Decoder）

`com.giants.feign.codec.FeignExceptionDecoder` — 调用 Feign 接口出错时进行异常解码。

当响应状态码等于 `response-exception-status`（默认 `600`）时，解码器会将响应体反序列化为原始的
`Exception` 对象并抛出，实现服务端异常向调用端的透明传递；其余状态码则回退到 Feign 默认解码器。
该功能配合服务端的 `com.giants.web.springmvc.resolver.JsonExceptionResolver` 使用（负责将异常序列化为约定状态码的 JSON 响应）。

### GET 请求参数编码器（Query Map Encoder）

`com.giants.feign.querymap.BeanQueryMapNestEncoder` — 嵌套复杂属性编码器。

在原生 Feign 的 `QueryMapEncoder` 基础上增强，支持将复杂对象通过 GET 请求参数传递：

- 简单属性：直接编码为 `key=value`。
- 嵌套对象：递归展开为点分层级，如 `parent.child=value`。
- 简单类型集合：拼接为逗号分隔的字符串，如 `ids=1,2,3`。
- 支持 `@feign.Param` 注解自定义参数别名。

在 Feign 接口中通过 `@SpringQueryMap`（或 `@QueryMap`）标注对象参数即可使用。

### 调用处理器（Invocation Handler）

`com.giants.feign.proxy.GiantsInvocationHandler` — 记录 Feign 接口执行时间。

配合 `com.giants.analyse.profiler.ExecutionTimeProfiler` 打印调用方法堆栈的耗时统计。
仅当开启 `logCallStackTimeAnalyse` 且当前线程存在 `ExecutionTimeProfiler` 上下文时才进行统计；
`logArguments` 为 `true` 时会将方法入参一并记录到堆栈信息中。

> 注意：`GiantsInvocationHandlerFactory` 默认未在自动配置中注册。如需启用调用耗时统计，
> 需自行声明一个 `feign.InvocationHandlerFactory` Bean：

```java
@Bean
public InvocationHandlerFactory giantsInvocationHandlerFactory() {
    // 参数依次为：logCallStackTimeAnalyse、logArguments
    return new GiantsInvocationHandlerFactory(true, true);
}
```

## 使用手册

从零开始的完整实操示例（定义客户端、复杂对象 GET 参数、异常透传、多版本隔离、调用耗时统计）见独立文档：

📖 **[使用手册 docs/USER_GUIDE.md](docs/USER_GUIDE.md)**

## 自动装配的 Bean

`GiantsFeignConfiguration` 默认注册以下 Bean：

- `Feign.Builder`：关闭重试（`Retryer.NEVER_RETRY`）。
- `QueryMapEncoder`：`BeanQueryMapNestEncoder`。
- `ErrorDecoder`：`FeignExceptionDecoder`（读取 `response-exception-status`）。
- `Targeter`：根据 `client-version-map` 是否为空，选择 `DefaultTargeter` 或 `GiantsFeignTargeter`（`@ConditionalOnMissingBean`，可被自定义覆盖）。

## 环境要求

- JDK 1.8+
- Spring Boot 2.5.x（编译基于 2.5.6）
- Spring Cloud OpenFeign 3.0.x（编译基于 3.0.5）

## License

基于 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) 开源。

