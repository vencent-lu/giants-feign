# giants-feign
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.vencent-lu/giants-feign/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.vencent-lu/giants-feign)

feign 功能扩展，满足spring cloud RPC 调用需求

## Error Decoder
错误解码器，当调用Feign接口出现错误时调用解码器 进行解码，解码器列表如下：
* com.giants.feign.codec.FeignErrorDecoder : 当状态码返回 600 时进行异常解码，配合 com.giants.web.springmvc.resolver
.JsonExceptionResolver 使用。

## Query Map Encoder
GET请示参数编码器，编码器列表如下：
* com.giants.feign.querymap.BeanQueryMapNestEncoder : 嵌套复杂属性编码器，支持复杂对象通过GET请求参数转递。

## Invocation Handler
调用处理器，Feign代理实例调用接口时执行 调用处理器，调用处理器列表如下：
* com.giants.feign.proxy.GiantsInvocationHandler : 记录Feign接口执行时间，配合 com.giants.analyse.profiler.ExecutionTimeProfiler
 打印调用方法堆栈时间统计使用。
