# kamarias-parent



​	`kamarias`的愿景是让 Java 程序员在开放项目时能更少的引入maven依赖，来完成项目的更多特性的功能。

提供特性如下：

`kamarias-common模块`

一个不基于Spring的一些 公共模块功能封装

1、提供常用的接口响应dto，`AjaxResult` , `ResultDTO`

2、在继承`SpringUtil`和`apache-utils`的工具类的前提下提供增强的工具类方法，如`StringUtils`，`BeanUtils`，`ListUtils`.....

3、提供基础的翻页响应实体`PageVO`，翻页处理工具`PageUtil`

4、在开放中经常使用的工具类等

`kamarias-spring-common模块`

基于Spring的一个公共模块，提供一些开发常使用的公共方法合注解。

1、`@AccessLimit`限流访问注解，常用于接口访问频次现在

2、`@CacheableResponse`缓存响应注解，常用于统计模块。

3、`@TokenBucketLimit`令牌桶限流注解，常用于访问流量突发的情况，支持冷热启动模式（实现方法基于google的`RateLimiter`）。

4、`@WebLog`应用服务日志切面实现。

5、`RedisCache`工具bean,提供对redis的操作工具类

6、`RedisSerializerConfig` redis的序列化配置，不在关注redis序列化后值乱码问题

7、`后端的跨域处理器`，在开发时不在关注跨域出现的问题

8、`@validation`校验异常全局处理

9、`TokenUtils` 工具类实现，token工具类实现

10、异步线程池配置

11、重复提交注解支持`@RepeatSubmit`，只支持post请求

12、......



`kamarias-spring-boot-distributed-lock-starter模块`

基于`redis`和`zk`的分布式锁启动Start，让实现分布式锁更加简单。

1、基于Redis的自动续期

2、`@LockAction`基于注解实现锁

3、`DistributedLock` 基于bean实现锁

4、....



`kamarias-spring-security-auth-starter模块`

是基于SpringSecurity的二次封装，让开发不在关注SpringSecurity的复杂配置，只需要实现`/login`的方法即可实现登录授权细节。

1、`@RequiresPermissions`校验用户权限的注解

2、`@RequiresRoles` 校验角色的注解

3、`SecurityProperties`授权访问的配置类

4、`LoginUtils`基于SpringBean的方式获取登录用户

5、`SecurityContextUtils`基于静态方法获取当前登录用户的方法





`kamarias-spring-cloud-parent模块`

在当前的开发中很多项目都是基于微服务开发，这个是基于微服务的一个启动配置父模板，使用该项目，同时也是用来管理项目版本号的一个依赖管理器。



更多使用细节可参考各个模块下的`readme`



该项目的功能已在  `https://github.com/kamarias/architecture-template` 使用。
