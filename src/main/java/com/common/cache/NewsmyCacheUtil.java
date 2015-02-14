package com.common.cache;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Id;

import com.common.dbutil.Paging;

/**
 * 提供一些服务于缓存机制公共逻辑；
 * <br/>创建时间：2012-5-25
 * @author yuqing
 */
public class NewsmyCacheUtil {
	/**
	 * 为缓存给定实体对象而生成key
	 * 如果是实体对象，则生成的缓存KEY格式为："ENTITY:实体类名:OID"
	 * @param entity 实体类对象
	 * @return 如果是实体类对象，则拼接后返回KEY，否则返回null;
	 */
	public static Serializable getKey(Object entity){		
		try {
			Method[] methods= entity.getClass().getMethods();
			//循环entity的所有属性，以找到标用@Id的主键属性，并取出主键值，
			//以拼成一个"ENTITY:实体类名:oid值"模式的字符串；
			for(Method method : methods){				
				if(method.isAnnotationPresent(Id.class)){
					Id id=method.getAnnotation(Id.class);
					return (Serializable)"ENTITY:"+entity.getClass().getSimpleName()+":"+method.invoke(entity,(Object[])null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * 为缓存OID与id相等的给定类型实例而生成KEY；格式为："ENTITY:实体类名:OID"
	 * @param id 实体的OID值；
	 * @param cls 实体类型实例；
	 * @return 如果cls对象不为null，则拼接后返回KEY，否则返回null;
	 */
	public static Serializable getKey(Serializable id,Class cls){
		//如果类型不为空则，取类型短名；否则返回null;
		if(cls!=null)
			return "ENTITY:"+cls.getSimpleName()+":"+id;
		else
			return null;
	}
	
	/**
	 * 为缓存给定类型所有实例的集合key;格式为："QUEYR:FROM 实体类名:ALL"
	 * @param cls 实体类型；
	 * @return 如果cls对象不为null，则返回KEY，否则返回null;
	 */
	public static Serializable getKey(Class cls){
		//如果类型不为空则，取类型短名；否则返回null;
		if(cls!=null)
			return "QUERY:FROM "+cls.getSimpleName()+":ALL";
		else 
			return null;
	}

	/**
	 * 为缓存给定类型所有实例的集合key;格式为："QUEYR:FROM 实体类名:ALL:分页号"
	 * @param cls 实体类型；
	 * @param pageNo 当前页号
	 * @return 如果cls对象不为null，则返回KEY，否则返回null;
	 */
	public static Serializable getKey(Class cls,int pageNo){
		//如果类型不为空则，取类型短名；否则返回null;
		if(cls!=null)
			return "QUERY:FROM "+cls.getSimpleName()+":ALL:"+pageNo;
		else 
			return null;
	}
	
	/**
	 * 为缓存指定查询条件的实例集合生成key;格式为："QUERY:查询QL:参数值1,参数值2,...";
	 * @param ql 查询语句
	 * @param parameters 参数值集
	 * @return 如果ql对象不为null，则返回KEY，否则返回null;
	 */
	public static Serializable getKey(String ql,Object... parameters){
		String pstr="";
		if(ql!=null){
			//如果查询语句不为null,则将查询参数集parameters中的各值取出拼成一个以","号分隔的字符串；
			for(int i=0;parameters!=null&&i<parameters.length;i++){
				//如果不是参数集的最后个，则字符中拼上",",否则不要“,"号
				if(i<parameters.length-1) {
					/* 参数类型是Timestamp时，由于参数本身含有":"，跟key的设计相冲突，所以把参数中的":"替换为"#" */
					if(parameters[i] instanceof Timestamp) {
						pstr+=parameters[i].toString().replaceAll(":", "#")+",";
					}else {
						pstr+=parameters[i]+",";
					}
				}
				/* 最后一个参数，pstr后面需要加"," */
				else {
					if(parameters[i] instanceof Timestamp) {
						pstr+=parameters[i].toString().replaceAll(":", "#");
					}else {
						pstr+=parameters[i];
					}
				}
			}
			return "QUERY:"+ql+":"+pstr;
		}else
			return null;		
	}
	
	/**
	 * 为缓存指定查询条件的实例集合生成key;格式为："QUERY:查询QL:参数值1,参数值2,...:PAGING";
	 * @param ql 查询语句
	 * @param parameters 参数值集
	 * @return 如果ql对象不为null，则返回KEY，否则返回null;
	 */
	public static Serializable getKey(String ql,int pageNo,Object... parameters){
		String pstr="";
		if(ql!=null){
			//如果查询语句不为null,则将查询参数集parameters中的各值取出拼成一个以","号分隔的字符串；			
			for(int i=0;parameters!=null&&i<parameters.length;i++){
				if(i<parameters.length-1) {
					/* 参数类型是Timestamp时，由于参数本身含有":"，跟key的设计相冲突，所以把参数中的":"替换为"#" */
					if(parameters[i] instanceof Timestamp) {
						pstr+=parameters[i].toString().replaceAll(":", "#")+",";
					}else {
						pstr+=parameters[i]+",";
					}
				}
				else {
					if(parameters[i] instanceof Timestamp) {
						pstr+=parameters[i].toString().replaceAll(":", "#");
					}else {
						pstr+=parameters[i];
					}
				}
			}
			//return "QUERY:"+ql+":"+pstr+":"+pageNo;
			/* hyq 2014-06-24  最后放上分页标识 */
			return "QUERY:"+ql+":"+pstr+":PAGING";
		}else
			return null;		
	}

	/**
	 * 根据带分页标识的查询缓存的key,返回无分页标识的key;格式为： "QUERY:查询QL:参数列表"
	 * @param cacheKey key值
	 * @return 如果cacheKey不为空，则返回生成的key值，否则返回null
	 */
	public static Serializable getPagingKey(String cacheKey) {
		if(cacheKey != null) {
			//带分页标识的缓存key格式：QUERY:查询QL:参数列表:分页号，现需要去掉最后一个":"及后面的内容；
			int omitIndex=cacheKey.lastIndexOf(":");
			return cacheKey.substring(0,omitIndex);
		}else {
			return null;
		}
	}
}
