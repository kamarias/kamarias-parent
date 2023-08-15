package io.github.kamarias.aspect;

import com.alibaba.fastjson2.JSON;
import io.github.kamarias.annotation.WebLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 日志切面
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/28 17:20
 */
@Aspect
public class WebLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);

    /**
     * 记录请求开始时间（毫秒）
     */
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**
     * 记录自定义日志内容
     */
    private final ThreadLocal<String> logContent = new ThreadLocal<>();

    @Pointcut("@annotation(io.github.kamarias.annotation.WebLog)")
    public void webLog() {
    }

    @Before(value = "webLog()")
    public void beforeRun(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        WebLog webLog = AnnotationUtils.getAnnotation(method, WebLog.class);
        String content = webLog.value();
        logContent.set(content);
        LOGGER.info("--------------> {} start <--------------", content);
        // 记录下请求内容
        String url = request.getRequestURL().toString();
        String args = Arrays.toString(joinPoint.getArgs());
        LOGGER.info("URL : {}", url);
        LOGGER.info("HTTP_METHOD : {}", request.getMethod());
        LOGGER.info("IP : {}", request.getRemoteAddr());
        LOGGER.info("CLASS_METHOD : {}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        LOGGER.info("ARGS : {}", args);
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void afterRun(Object ret) {
        // 处理完请求，返回内容
        LOGGER.info("RESPONSE : {}", JSON.toJSON(ret));
        LOGGER.info("SPEND TIME : {}ms", System.currentTimeMillis() - startTime.get());
        LOGGER.info("--------------> {} end <--------------", logContent.get());
        startTime.remove();
        logContent.remove();
    }

}
