package com.common.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用以注解一个用户实体的哪个属性是真实姓名，要求给getXxx加上该注解；
 * 用以用户操作日志机制;
 * </br>date:2014-01-21
 * @author hyq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UserName {}
