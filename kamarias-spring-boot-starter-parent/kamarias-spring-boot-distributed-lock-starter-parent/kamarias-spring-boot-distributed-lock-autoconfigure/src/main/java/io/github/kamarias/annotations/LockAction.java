package io.github.kamarias.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 基于Redis的分布式锁
 * @author 王玉星
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LockAction {

	/**
	 * 锁的资源（lockName） 别名 key
	 * spelkey存在时优先使用spelkey spelkey > key = value
	 */
	@AliasFor("key")
	String value() default "";

	/**
	 * 锁的资源（lockName） 别名 value
	 * spelkey存在时优先使用spelkey spelkey > key = value
	 */
	@AliasFor("value")
	String key() default "";

	/**
	 * spel表达式锁资源（lockName）
	 * 当存在时优先使用spel表达式
	 */
	String spelkey() default "";

	/**
	 * 持锁时间, 单位毫秒
	 */
	long keepMills() default 30 * 1000;

	/**
	 * 当获取失败时的动作
	 */
	LockFailAction action() default LockFailAction.CONTINUE;

	enum LockFailAction{
		/**
		 * 放弃
		 */
		GIVEUP,

		/**
		 *  重试
		 */
		CONTINUE
	}

	/**
	 * 重试的间隔时间, 单位毫秒 设置GIVEUP忽略此项
	 */
	long sleepMills() default 200;

	/**
	 * 重试次数 设置GIVEUP忽略此项
	 */
	int retryTimes() default 5;
}
