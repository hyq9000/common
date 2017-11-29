package com.common.utils;

import java.io.Serializable;
import java.util.List;
import com.common.dbutil.Paging;

/**
 * 将数据库应用最通用的几个服务接口，抽象出封在这，以免每个服务对象都要定义相同的接口这种繁锁；
 * </br> 创建时间：201412-18
 * @author hyq
 */
public interface EntityService<T> {
	/**
	 * 新增
	 * @param entity
	 * @return
	 */
	void add(T entity) throws Exception;
	/**
	 * 修改
	 * @param entity
	 * @return 如果修改成功，则返回1
	 */
	int update(T entity)  throws Exception;
	/**
	 * 删除
	 * @param entity
	 * @return
	 */
	void delete(T entity)  throws Exception;
	/**
	 * 根据ID主键取对象
	 * @param id
	 * @return
	 */
	T getById(Serializable id) throws Exception;
	/**
	 * 取全部对象实例
	 * @return
	 */
	List<T> getAll() throws Exception;
	/**
	 * 分页取全部对象实例
	 * @return
	 */
	List<T> getAll(Paging paging) throws Exception;
	/**
	 * 根据对象约定的"名称"属性,取得该对象
	 * @param name
	 * @return
	 */
	T getByName(String name) throws Exception;
}
