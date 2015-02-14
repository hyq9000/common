package com.common.dbutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义注解一个 mybatis的实体类；
 * 用于DaoMybatisImpl实现中，反省对对实体对象的增、删、改、查常规操作SQL，在mybastis配置中的映射ID；
 * <br/>创建时期:2014-09-22
 * @author hyq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyBatisEntity{
	/**
	 * 该值应与<mapper namespace="xxxx">中的xxxx保持一致,如未设置值,则默认为"";
	 * @return 
	 */
	public String namespace() default "";
	
	/**
	 * 新增实体对象时所需SQL在mybatis中映射ID值;
	 * 也就是<select id="id值" resultType="xxxx"></select>中的id值；
	 */
	public String insertMapId() default "";
	/**
	 * 修改实体对象时所需SQL在mybatis中映射ID值;
	 * 也就是<select id="id值" resultType="xxxx"></select>中的id值；
	 */
	public String updateMapId()default "";
	/**
	 * 删除实体对象时所需SQL在mybatis中映射ID值;
	 * 也就是<select id="id值" resultType="xxxx"></select>中的id值；
	 */
	public String deleteMapId()default "";
	/**
	 * 根据实体对象主键ID查询对象时所需SQL在mybatis中映射ID值;
	 * 也就是<select id="id值" resultType="xxxx"></select>中的id值；
	 */
	public String queryByIdMapId()default "";
	/**
	 * 查询所有实体对象时所需SQL在mybatis中映射ID值;
	 * 也就是<select id="id值" resultType="xxxx"></select>中的id值；
	 */
	public String queryAllMapId()default "";
	/**
	 * 根据实体对象名称来查询实体对象时所需SQL在mybatis中映射ID值;
	 * 也就是<select id="id值" resultType="xxxx"></select>中的id值；
	 */
	
	public String getByNameMapId()default "";
}
