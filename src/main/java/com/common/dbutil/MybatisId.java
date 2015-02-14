package com.common.dbutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解一个MYBATIS实体BEAN的主键ID的get方法上
 * <br/>日期：2014-09-26
 * @author hyq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MybatisId {
	
}
