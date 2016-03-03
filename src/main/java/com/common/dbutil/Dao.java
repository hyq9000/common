/***********************************************************************
 * Module:  Dao.java
 * Author:  yuqing
 * Purpose: Defines the Interface Dao
 ***********************************************************************/

package com.common.dbutil;

import java.io.Serializable;
import java.util.*;

/**
 * 提供访问数据库的公共泛型接口； 该接口可以由hibernate,jap,jdbc,mybatis实现；
 * 创建时间：2012-7-26
 * @author yuqing
 * @param <T>代表着某数据表的实体类类型；
 */
public interface Dao<T> {
	/**
	 * 0: 等于 
	 */
	int OP_EQUALS=0;
	/**1:大于*/
	int	OP_GREAT_THAN=1;
	/**2:小于*/
	int	OP_LITTER_THAN=2;
	/**10:大于等于*/
	int	OP_GREAT_EQUAL=10;
	/**20:小于等于*/
	int	OP_LITTER_EQUAL=20;
	/*** 3: like 条件值需要自行拼% */
	int	OP_LIKE =3;
	/**4:不等于 */
	int	OP_NOT_EQUAL=4;
	
	/** 0:and */
	int CONDITION_AND=0;
	/**
	 * 1:or
	 */
	int CONDITION_OR=1;
	
	/**
	 * 新增实体对象到数据库；
	 * @param entity  实体类对象
	 * @exception 如果操作失败，则抛出异常；
	 */
	void add(T entity) throws Exception;

	/**
	 * 从数据库中删除实体对象的数据行；	
	 * @param entity 给定的实体对象,它必须有OID值；
	 * @exception 如果操作失败，则抛出异常；
	 */
	void delete(T entity) throws Exception;

	/**
	 * 修改对应的实体对象；
	 * @param entity 待修改的实体对象，它必须有OID值
	 * @exception 如果操作失败，则抛出异常；
	 */
	int update(T entity) throws Exception;

	/**
	 * 返回给定ID的实体对象实例
	 * @param id 实体对象ID，其实就是数据库的主键值；
	 * @return 如果存在主键为id，则返回其实体对象，否则返回null;
	 */
	T getById(Serializable id) throws Exception;

	/**
	 * 查询出所有数据行的实体对象；
	 * @return 实体对象的集合
	 */
	List<T> getAll() throws Exception;
	
	/**
	 * 分页查询出所有数据行的实体对象；
	 * @param paging 分页信息实例；
	 * @return 实体对象的集合<br/>
	 * <font color='red'>特别说明：</font>该查询条件下的记录总行数，会封装到一个Paing实例，放在List的最后；<br/>
	 * 控制层可以通过Paging.getTotaoCount()方法获得总行数值;
	 */
	List<T> getAll(Paging paging) throws Exception;

	/**
	 * 假设所有实体类都有一<b>name</b>意义的属性，查找彼配该属性的第一个实体对象; 如果实体无此意义的属性，则返回null;
	 * @param name 数据表字段中"名称"的值
	 * @return 找到了则返回该实体的对象实例，否则返回null;
	 */
	T getByName(String name) throws Exception;	
	

	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值数组，查取符合条件的实体集合</br>
	 * propertyNames,opflags,values三个数组的长度必须保持一致；
	 * @param propertyNames  查询条件的属性名称集
	 * @param opFlags 操作符集，支持“=,<>,>=,<=,<,>,like"这几种，其他不支持;这几种操作符要求传给正确的整形常量值，说明如下：
	 *	<li>DAO.OP_EQUALS  等于,值为0;
	 *	<li>DAO.OP_GREAT_THAN  大于,值为1;
	 *	<li>DAO.OP_LITTER_THAN 小于,值为2;
	 *	<li>DAO.OP_GREAT_EQUAL 大于等于,值为10;
	 *	<li>DAO.OP_LITTER_EQUAL小于等于,值为20;
	 *	<li>DAO.OP_LIKE 模糊查询,值为3;	
	 * @param conditionFlag 条件关系标识 ,支持AND ,OR,常量值说明如下：
	 *	<li>DAO.CONDITION_AND AND,值为0;
	 *	<li>DAO.CONDITION_OR OR,值为1;	
	 * @param values 查询条件属性值集;
	 * @return 返回符合条件的对象集合;
	 */
	List<T> queryByPropertys(String[] propertyNames, int[] opFlags,int[] conditionFlag,
			Object[] values) throws Exception;
	

	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值数组，分页查取符合条件的实体集合</br>
	 * propertyNames,opflags,values三个数组的长度必须保持一致；
	 * @param propertyNames  查询条件的属性名称集
	 * @param opFlags 操作符集，支持“=,<>,>=,<=,<,>,like"这几种，其他不支持;这几种操作符要求传给正确的整形常量值，说明如下：
	 *	<li>DAO.OP_EQUALS  等于,值为0;
	 *	<li>DAO.OP_GREAT_THAN  大于,值为1;
	 *	<li>DAO.OP_LITTER_THAN 小于,值为2;
	 *	<li>DAO.OP_GREAT_EQUAL 大于等于,值为10;
	 *	<li>DAO.OP_LITTER_EQUAL小于等于,值为20;
	 *	<li>DAO.OP_LIKE 模糊查询,值为3;	
	 * @param conditionFlag 条件关系标识 ,支持AND ,OR,常量值说明如下：
	 *	<li>DAO.CONDITION_AND AND,值为0;
	 *	<li>DAO.CONDITION_OR OR,值为1;
	 * @param values 查询条件属性值集；
	 * @param paging 分页对象实例
	 * @return 返回符合条件的对象LIST集合;
	 * <font color='red'>特别说明：该查询条件下的记录总行数，会封装到一个Paing实例，放在List的最后;
	 * 控制层可以通过Paging.getTotaoCount()方法获得总行数值;</font>
	 */
	List<T> queryByPropertys(String[] propertyNames, int[] opFlags,int[] conditionFlag,Object[] values,Paging paging) throws Exception;		

	
	
	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值，查取符合条件的实体集合</br>
	 * @param propertyName  查询条件的属性名称集
	 * @param opFlag 操作符集，支持“=,<>,>=,<=,<,>,like"这几种，其他不支持;这几种操作符要求传给正确的整形常量值，说明如下：
	 *	<li>DAO.OP_EQUALS  等于,值为0;
	 *	<li>DAO.OP_GREAT_THAN  大于,值为1;
	 *	<li>DAO.OP_LITTER_THAN 小于,值为2;
	 *	<li>DAO.OP_GREAT_EQUAL 大于等于,值为10;
	 *	<li>DAO.OP_LITTER_EQUAL小于等于,值为20;
	 *	<li>DAO.OP_LIKE 模糊查询,值为3;	
	 * @param value 查询条件属性值集；
	 * @return 返回符合条件的对象集合;
	 */
	List<T> queryByProperty(String fieldName, int opFlag,Object value) throws Exception;
	
	
	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值，分页查取符合条件的实体集合</br>
	 * @param propertyName  查询条件的属性名称集
	 * @param opFlag 操作符集，支持“=,<>,>=,<=,<,>,like"这几种，其他不支持;这几种操作符要求传给正确的整形常量值，说明如下：
	 *	<li>DAO.OP_EQUALS  等于,值为0;
	 *	<li>DAO.OP_GREAT_THAN  大于,值为1;
	 *	<li>DAO.OP_LITTER_THAN 小于,值为2;
	 *	<li>DAO.OP_GREAT_EQUAL 大于等于,值为10;
	 *	<li>DAO.OP_LITTER_EQUAL小于等于,值为20;
	 *	<li>DAO.OP_LIKE 模糊查询,值为3;	
	 * @param value 查询条件属性值集；
	 * @param paging 分页对象实例
	 * @return 返回List集合，<font color='red'>特别说明：该查询条件下的记录总行数，会封装到一个Paing实例，放在List的最后;
	 * 控制层可以通过Paging.getTotaoCount()方法获得总行数值;</font>
	 */
	List<T> queryByProperty(String fieldName, int opFlag,Object value,Paging paging) throws Exception;

	
	/**
	 * 执行更新的SQL；更新包括（新增，删除，修改的DML或DDL）
	 * @param sql 合法的DML及DDL的SQL；如果是在mybatis实现下，则该参数是sqlid;
	 * @param parameters 该更新操作所需要的参数列表；
	 * 	<li>如果是jdbc原始实现：则该参数列表是零到多个对象;
	 *  <li>如果是mybatis实现，则该参数列表应该组织成一个map，或一个T实例；
	 * @return 返回更新所影响的行数；
	 * @exception 如果执行失败
	 */
	int executeUpdate(String sql, Object... parameters) throws Exception;

	/**
	 * 执行查询的SQL；返回查询的结果集；
	 * @param sql 合法的查询SQL；如果是在mybatis实现下，则该参数是sqlid
	 * @param parameters 该查询操作所需要的参数列表；
	 * 	<li>如果是jdbc原始实现：则该参数列表是零到多个对象;
	 *  <li>如果是mybatis实现， 则该参数列表应该组织成一个map，或一个实体类实例；
	 * @return 果一返回查询的结果List实例；list集合中的每个元素是查询结行数据，
	 * <br/>行数据一般是一个(列名:值)的map
	 * @exception 如果执行失败
	 */
	List executeQuery(String sql,Object... parameters) throws Exception;
	
	/**
	 * 执行查询的SQL；分页返回查询的结果集；
	 * @param sql 合法的查询SQL；如果是在mybatis实现下，则该参数是sql的映射id
	 * @param paging 分页信息实例
	 * @param parameters 该查询操作所需要的参数列表；
	 * 	<li>如果是jdbc原始实现：则该参数列表是零到多个对象;
	 *  <li>如果是mybatis实现， 则该参数列表应该组织成一个map，或一个实体实例；
	 * @return 返回查询的结果List实例；list集合中的每个元素是查询结果一行数据，
	 * <br/>行数据一般是一个(列名:值)的map,<font color='red'>特别说明：该查询条件下的记录总行数，会封装到一个Paing实例，放在List的最后;
	 * 控制层可以通过Paging.getTotaoCount()方法获得总行数值;</font>
	 * @exception 如果执行失败
	 */
	List executeQuery(String sql,Paging paging,Object... parameters) throws Exception;
	/**
	 * 执行查询的SQL,返回查询结果集;
	 * @param sql 合法的查询SQL；如果是在mybatis实现下，则该参数是sql的映射id
	 * @return 返返回查询的结果List实例；list集合中的每个元素是查询结行数据，
	 * @exception 如果执行失败
	 */
	List executeQuery(String sql) throws Exception;
	/**
	 *  执行查询的SQL；分页返回查询的结果集；
	 * @param sql 合法的查询SQL；如果是在mybatis实现下，则该参数是sql的映射id
	 * @param paging 分页信息实例
	 * @return 返回查询的结果List实例；list集合中的每个元素是查询结果一行数据，
	 * <br/>行数据一般是一个(列名:值)的map,<font color='red'>特别说明：该查询条件下的记录总行数，会封装到一个Paing实例，放在List的最后;
	 * 控制层可以通过Paging.getTotaoCount()方法获得总行数值;</font>
	 * @exception 如果执行失败
	 */
	List executeQuery(String sql,Paging paging) throws Exception;
	
	/**
	 * 执行查询的SQL；返回查询的结果集的第一行；
	 * @param sql 合法的查询SQL；如果是在mybatis实现下，则该参数是sql的映射id
	 * @param parameters 该查询操作所需要的参数列表；
	 * 	<li>如果是jdbc原始实现：则该参数列表是零到多个对象;
	 *  <li>如果是mybatis实现， 则该参数列表应该组织成一个map，或一个实体实例；
	 * @return 返回查询的结果集中的第一行数据对象，
	 * @exception 如果执行失败
	 */
	Object executeQueryOne(String sql,Object... parameters) throws Exception;
	
}