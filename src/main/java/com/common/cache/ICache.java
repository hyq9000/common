package com.common.cache;

import java.io.Serializable;

/**
 * 抽象定义业务缓存统一接口;用以隔离具体缓存机制，其实现类可以采用redis,
 * memercache服务器来实现，也可以自实现；
 * </br>创建时间：2015-3-19 
 * @author hyq
 */
public interface ICache {

	/**
	 * 根据key取得对应的值对象；
	 * @param key  键
	 * @return 如果有key对应的值，则返回，否则返回null;
	 */
	Object get(Serializable key) throws Exception;
	
	/**
	 * 从缓存中删除给定的KEY所对应的值对象；
	 * @param key 键
	 * @param value 值对象
	 */
	void remove(Serializable key)  throws Exception;	
	
	/**
	 * 将值对象放入缓存中;如已存储，则替换
	 * @param key 键
 	 */
	void put(Serializable key, Object value) throws Exception;
	
	/**
	 * 将值对象放入缓存中;如已存储，则替换
	 * @param key 
	 * @param value 值对象
	 * @param timeLength 放多长时间，单位:ms
	 */
	void put(Serializable key,Object value,long timeLength) throws Exception;
	
	/**
	 * 将一个fieldname:value的键值对放入一个”MAP"，该"MAP"以key为名，存入缓存;
	 * @param key 放入缓存的KEY
	 * @param fieldName 键值对的KEY
	 * @param value 键值对的值
	 * @param timeLength 放多长时间，单位:ms
	 */
	void put(Serializable key,String fieldName,Object value,long timeLength)  throws Exception;
	
	
	
	/**
	 * 从缓存中取出给定key的MAP对象中，key为fieldName的值；
	 * @param key 放入缓存的KEY
	 * @param fieldName 键值对的KEy
	 */
	Object get(Serializable key,String fieldName) throws Exception;
	
}
