package com.common.dbutil;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.common.cache.ApplicationCache;
import com.common.cache.ICache;
import com.common.cache.NewsmyCacheUtil;
import com.common.log.ExceptionLogger;
/**
 * Mybatis之DAO实现版,实现自动实现add,update,delete,getById,getByName,getAll；
 * <br/>创建日期：2016-04-28
 * @author hyq
 * @param <T>
 */
public  class DaoMybatisEntityImpl<T> extends  DaoMybatisImpl<T>{
	
	@Override
	public void add(T entity) throws Exception {
		// TODO Auto-generated method stub
		super.add(entity);
	}

	@Override
	public void delete(T entity)throws Exception {
		// TODO Auto-generated method stub
		super.delete(entity);
	}

	@Override
	public int update(T entity) throws Exception{
		// TODO Auto-generated method stub
		return super.update(entity);
	}

	@Override
	public T getById(Serializable id) throws Exception{
		// TODO Auto-generated method stub
		return super.getById(id);
	}

	@Override
	public List<T> getAll()throws Exception {
		// TODO Auto-generated method stub
		return super.getAll();
	}

	@Override
	public T getByName(String name) throws Exception{
		// TODO Auto-generated method stub
		return super.getByName(name);
	}

	@Override
	public List getAll(Paging paging)throws Exception {
		// TODO Auto-generated method stub
		return super.getAll(paging);
	}
	

	
}
