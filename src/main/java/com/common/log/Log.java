package com.common.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解是用注解所有要进行操作日志记录的方法；
 * <br/>时间：2012-7-16
 * @author yuqing
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Log {
	/**
	 * 此属性标注方法执行的业务功能说明；
	 * @return 日志事项名的字符串
	 */
	public String content() default "";	
	/**
	 * 在该方法上的执行，如果成功就一定要求会话中有用户实例；
	 * 否则视为该方法执行失败；
	 * @return
	 */
	public boolean requireUser() default true;
}
