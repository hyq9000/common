package com.common.cache;

import java.io.Serializable;
import java.lang.reflect.Method;
import javax.ejb.EJB;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

/**
 * EJB实体bean持久化过程生命周期中的各阶段的监听器；
 * 主要实现同步缓存；
 * 可以很好的解决增删改的问题；但对于ql查询却无能为力！
 * 再尚未实现；不能使用；
 * <br/>创建时间：2012-5-25
 * @author yuqing
 */
public class EntityListener {
	@EJB(beanName="newsmyCache") ApplicationCache appCache;
	
	/**
	 * 删除后，也将缓存对应的内容删除；
	 * @param entity
	 */
	@PostRemove
	public void update(Object entity){		
		try {
			Method[] methods= entity.getClass().getMethods();
			for(Method method : methods){				
				if(method.isAnnotationPresent(Id.class)){
					Id id=method.getAnnotation(Id.class);
					Serializable idValue=(Serializable)method.invoke(entity,(Object[])null);
					appCache.remove(idValue, entity);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 修改，新增后，将刷新缓存内容；
	 * @param entity
	 */
	@PostPersist
	@PostUpdate
	public void persist(Object entity){
		System.out.println(entity.toString()+"正在更新中...");
		try {
			Method[] methods= entity.getClass().getMethods();
			for(Method method : methods){
				if(method.isAnnotationPresent(Id.class)){
					Id id=method.getAnnotation(Id.class);
					Serializable idValue=(Serializable)method.invoke(entity,(Object[])null);
					System.out.println(idValue+".....");
					appCache.put(idValue, entity, ApplicationCache.CACHE_TYPE_UPDATE);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(entity.toString()+"正在更新中2...");
	}
}
