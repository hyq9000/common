package com.common.log;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.aop.ThrowsAdvice;

/**
 * 异常发生后，用log4j记录该异常的栈信息
 * <br/>时间：2012-11-9
 * @author yuqing
 */
public class ExceptionAdvice implements ThrowsAdvice {
	/**
	 * 异常发生后，用log4j记录该异常的栈信息
	 * @param method 发生异常的方法
	 * @param args   方法调用参数
	 * @param target 发生异常方法所对象实例
	 * @param e 异常的实例；
	 */
	public void afterThrowing(Method method, Object[] args, Object target,Exception e){
		Logger.getLogger(target.getClass()).error("错误:",e);
	}

}
