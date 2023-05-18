package io.github.kamarias.aspect;


import io.github.kamarias.annotations.LockAction;
import io.github.kamarias.lock.DistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 分布式锁切面
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/3 21:28
 */
@Aspect
@Configuration
@ConditionalOnBean(DistributedLock.class)
public class DistributedLockAspect {

    private final Logger logger = LoggerFactory.getLogger(DistributedLockAspect.class);

    @Autowired
    private DistributedLock distributedLock;

    private final ExpressionParser parser = new SpelExpressionParser();

    private final  DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Pointcut("@annotation(io.github.kamarias.annotations.LockAction)")
    private void lockPoint() {

    }

    @Around("lockPoint()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        LockAction lockAction = AnnotationUtils.getAnnotation(method, LockAction.class);
        String key = lockAction.value();
        if (StringUtils.isNotBlank(lockAction.spelkey())){
            key = parse(lockAction.spelkey(), method, pjp.getArgs());
        }
        int retryTimes = lockAction.action().equals(LockAction.LockFailAction.CONTINUE) ? lockAction.retryTimes() : 0;
        boolean lock = distributedLock.lock(key, lockAction.keepMills(), retryTimes, lockAction.sleepMills());
        if (!lock) {
            logger.debug("get lock failed : " + key);
            return null;
        }
        // 得到锁,执行方法,释放锁
        logger.debug("get lock success : " + key);
        try {
            return pjp.proceed();
        } catch (Exception e) {
            logger.error("execute locked method occured an exception", e);
        } finally {
            boolean releaseResult = distributedLock.releaseLock(key);
            logger.debug("release lock : " + key + (releaseResult ? " success" : " failed"));
        }
        return null;
    }

    /**
     * 解析SPEL表达式
     *
     * @param key    表达式
     * @param method 方法
     * @param args   方法参数
     * @return 解析后的字符串
     */
    @SuppressWarnings("all")
    public String parse(String key, Method method, Object[] args) {
        // 使用spring的DefaultParameterNameDiscoverer获取方法形参名数组
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        // 解析过后的Spring表达式对象
        Expression expression = parser.parseExpression(key);
        // spring的表达式上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 给上下文赋值
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return expression.getValue(context).toString();
    }

}
