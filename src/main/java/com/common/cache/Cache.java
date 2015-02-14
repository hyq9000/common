package com.common.cache;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识哪些方法是需要缓存，以及该方法执行的是什么（包括四种新增，删除，修改，查询）操作；
 * <br/>创建时间：2012-5-25
 * @author yuqing
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
	/**
	 * 操作类型
	 * @return 
	 */
	CacheType type();
	/**
	 * 缓存的KEY值
	 * @return
	 */
	String key();
}


