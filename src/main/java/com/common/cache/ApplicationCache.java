package com.common.cache;

import java.io.Serializable;

import com.common.dbutil.Paging;

/**
 * 系统应用缓存统一隔离接口；<br/>
 * 上层应用通过此接口来访问系统应用缓存；
 * 应用缓存可以采用自实现或其他三方缓存系统实现如（本地缓存encache,分布式缓存memcached)；
 * 所有缓存对象都是以键/值对来存储的;缓存的实现思路见详细设计文档；	
 * <BR/>创建时间：2012年05月25日
 * @author yuqing
 */
public interface ApplicationCache extends ICache{
	/**
	 * 缓存操作类型:代表新增缓存
	 */
	public static final int CACHE_TYPE_ADD = 0;
	/**
	 * 缓存操作类型:代表修改缓存
	 */
	public static final int CACHE_TYPE_UPDATE = 1;
	/**
	 * 缓存操作类型:代表删除缓存
	 */
	public static final int CACHE_TYPE_DELETE = 2;
	/**
	 * 缓存操作类型:代表查询缓存，此时不需要对相关缓存进行操作
	 */
	public static final int CACHE_TYPE_QUERY = 3;
	
	
	/**
	 * 根据key取得对应的值对象；
	 * @param key  键
	 * @return 如果有key对应的值，则返回，否则返回null;
	 */
	Object get(Serializable key) throws Exception;
	
	
	/**
	 * 根据key和paging取得对应的分页缓存值对象；
	 * @param key  键
	 * @param paging 分页对象
	 * @return 如果有key对应的分页缓存，则返回，否则返回null;
	 */
	Object get(Serializable key, Paging paging) throws Exception;
	
	
	/**
	 * 将值对象放入缓存中;
	 * @param key 键
	 * @param value 值对象
	 * @param cacheType 操作类型
	 */
	void put(Serializable key, Object value, int cacheType)  throws Exception;
	
	
	/**
	 * 将值对象放入缓存中;
	 * @param key 键
	 * @param value 值对象
	 * @param paging 分页对象
	 */
	void put(Serializable key, Object value, Paging paging) throws Exception;
	
	/**
	 * 从缓存中删除给定的KEY所对应的值对象；
	 * @param key 键
	 * @param value 值对象
	 */
	void remove(Serializable key, Object value) throws Exception;
	
	/**
	 *清理缓存；
	 */
	void clear();
	
	/**
	 * 实始化缓存；
	 */
	void init();
	
	/**
	 * 根据key取得paging缓存对应的值对象；
	 * @param key  键
	 * @return 如果有key对应的值，则返回，否则返回null;
	 */
	Integer getPagingCache(Serializable key);
	
	/**
	 * 将值对象放入paging缓存中;
	 * @param key 键
	 * @param totalCount 值对象
	 */
	void putPagingCache(Serializable key, Integer totalCount);
}
