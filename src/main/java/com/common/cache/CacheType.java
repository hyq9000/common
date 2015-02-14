package com.common.cache;

/** 
 * 方法执行的数据操作类型；
 * <br/>创建时间：2012-5-25
 * @author yuqing
 */
public enum CacheType{
	/**
	 * 新增
	 */
	INSERT,
	/**
	 * 删除
	 */
	DELETE,
	/**
	 * 查询
	 */
	QUERY,
	/**
	 * 修改
	 */
	UPDATE
}
