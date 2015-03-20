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
	Object get(Serializable key);
	
	/**
	 * 从缓存中删除给定的KEY所对应的值对象；
	 * @param key 键
	 * @param value 值对象
	 */
	void remove(Serializable key);	
	
	/**
	 * 将值对象放入缓存中;
	 * @param key 键
	 * @param value 值对象
	 * @param cacheType 操作类型
	 */
	void put(Serializable key, Object value);
}
