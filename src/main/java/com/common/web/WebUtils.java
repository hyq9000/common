package com.common.web;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;
import com.common.log.ExceptionLogger;

/**
 * 
 * 类型描述:封装一些与web相关公共逻辑实现
 * </br>创建时期: 2014年12月27日
 * @author hyq
 */
public class WebUtils {
	/**
	 * 将响应到客户端的响应JSON,格式化成以下固定格式:<pre>
	 * {
	 * 	code:一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2,0:无数据,-1:未知服务器异常,-2:会话超时)
	 *  error:"业务相关错误消息文本",//此属性在code为n时方有,
	 *  data:{一个具体的业务json对象}
	 * }
	 * </pre>
	 * @param data
	 * @param errorCode
	 * @param responseCode
	 * @return
	 */
	public static String responseJson(Object data,String errorMessage,int responseCode,long errorLogId){
		try {
			Map<String, Object> map=new HashMap<String, Object>();
			map.put("code", responseCode);
			if(errorMessage!=null && !errorMessage.trim().equals(""))
				map.put("error", errorMessage);
			//errorLogId:为写到错误日志文件中的错误日志标识
			if(errorLogId!=0)
				map.put("errorLogId", errorLogId);
			if(data!=null)
				map.put("data",data);
			return JSONUtil.serialize(map);
		} catch (JSONException e) {
			ExceptionLogger.writeLog(e, WebUtils.class);
			return "{\"code\":-1,\"error:\":\"服务器异常\"}";
		}
	}
	
	/**
	 * 将业务数据响应到客户端,格式:<pre>
	 * {
	 * 	code:一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2,1:操作成功;0:无数据,-1:未知服务器异常,-2:会话超时)
	 *  data:{一个具体的业务json对象}
	 * } 
	 * </pre>
	 * @param data
	 * @return
	 */
	public static String responseData(int responseCode,Object data){
		return responseJson(data, null,responseCode,0);
	}
	
	/**
	 * 将业务数据响应到客户端,格式:<pre>
	 * {
	 * 	code:一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2,1:操作成功;0:无数据,-1:未知服务器异常,-2:会话超时)
	 *  data:{一个具体的业务json对象}
	 * } 
	 * </pre>
	 * @param data
	 * @return
	 */
	public static String responseData(Object data){
		return responseJson(data, null,1,0);
	}
	
	
	/**
	 * 将业务相关异常,响应到客户端,格式:<pre>
	 * {
	 * 	code:一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2,1:操作成功;0:无数据,-1:未知服务器异常,-2:会话超时)
	 *  error:"业务相关错误消息文本",//此属性在code为n时方有,
	 * } 
	 * </pre>
	 * @param errorCode
	 * @param responseCode
	 * @return
	 */
	public static String responseError(String errorMessage,int responseCode){
		return responseJson(null, errorMessage,responseCode,0);
	}
	
	/**
	 * 将"未知服务器异常"响应到客户端格式: {code:错误日志中的唯一标识号} 
	 * @param exceptonCode
	 * @return
	 */
	public static String responseServerException(long exceptonCode){
		return responseJson(null, "服务器发生异常!",-1,exceptonCode);
	}
	
	/**
	 * 将"输入检验错误"响应到客户端格式: {code:-14} 
	 * @return
	 */
	public static String responseInputCheckError(String errorText){
		return responseJson(null, errorText,-14,0);
	}
	
	
	/**
	 * 将"会话超时"响应到客户端格式: {code:-2} 
	 * @return
	 */
	public static String responseSessionTimeout(){
		return responseJson(null, "会话超时,请重新登录!",-2,0);
	}
	
	
	/**
	 * 将操作"状态码"响应到客户端格式: {code:n} 
	 * n不可以为-1,-2;
	 * @return
	 */
	public static String responseCode(int responseCode) throws RuntimeException{
		if(responseCode==-1 || responseCode==-2 || responseCode==-14){
			throw new RuntimeException("-1、-2,-14为系统预留值!");
		}
		return responseJson(null, null,responseCode,0);
	}
	
	/**
	 * 生成一个给定的KEY、VALUE键值数组的MAP；
	 * @param keys 要放到map里边的key的数组
	 * @param values 要放到与map里value的数组，该数组每个值与keys里对应位置的key，是一个键-值对；
	 * @return 生成好的map对象
	 */
	public static Map generateMapData(String[] keys,Object[] values){
		Map map=new HashMap<String,Object>();
		for(int i=0;i<keys.length;i++){
			map.put(keys[i],values[i]);
		}
		return map;
	}
	
	/**
	 * 生成一个给定的KEY、VALUE键值数组的MAP；
	 * @param keys 要放到map里边的key的数组
	 * @param values 要放到与map里value的数组，该数组每个值与keys里对应位置的key，是一个键-值对；
	 * @return 生成好的map对象
	 */
	public static Map generateMapData(String key,Object value){
		Map map=new HashMap<String,Object>();
		map.put(key,value);		
		return map;
	}
	
	/**
	 * 将实体对象指定的属性(propertyNames)的值放到map里边去,key就是属性名
	 * @param map 
	 * @param entity
	 * @param propertyNames 要复制的属性名称集
	 */
	public static void objectPutToMap(Map map,Object entity,String... propertyNames){
		try {
			for(String pro: propertyNames){
				Field field=FieldUtils.getDeclaredField(entity.getClass(), pro, true);
				Object rs=field.get(entity);
				map.put(pro, rs);
			}
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, WebUtils.class);
		} 
	}

	
	/**
	 * 将实体对象所有属性的值放到map里边去,key就是属性名
	 * @param map 
	 * @param entity 要复制的对象
	 */
	public static void objectPutToMap(Map map,Object entity) throws Exception{
		try {
			Field[] fields=entity.getClass().getDeclaredFields();
			for(Field field:fields){
				field.setAccessible(true);
				Object rs=field.get(entity);
				map.put(field.getName(), rs);
			}			
		} catch (Exception e) {
				ExceptionLogger.writeLog(e, WebUtils.class);
				throw e;
		}
	}
	
	
	/**
	 * 将实体对象指定的属性(propertyNames)的值放到map里边去,key就是属性名
	  * @param destination 目标map 
	 * @param source 源map
	 * @param propertyNames 要复制键值对的的key集
	 */
	public static void mapPutToMap(Map destination,Map source,String... propertyNames){
		try {
			if(propertyNames!=null){
				for(int i=0;i<propertyNames.length;i++){
					destination.put(propertyNames[i],source.get(propertyNames[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	/**
	 * 将一个(source)map里的key-value放到(destination)map里边去
	 * @param destination 目标map 
	 * @param source 源map
	 */
	public static void mapPutToMap(Map destination,Map source){
			destination.putAll(source);
	}

	
	
	/**
	 * 将实体对象没指定属性(propertyNames)的所有其他值放到map里边去,key就是属性名
	 * @param map 
	 * @param entity
	 * @param propertyNames 不要复制的属性名集
	 */
	public static void objectPutToMapEx(Map map,Object entity,String... propertyNames){
		try {
			Field[] allFields=entity.getClass().getDeclaredFields();
			List<Field> list=new ArrayList<Field>();
			/*
			 * 找到那些不包含在propertyNames名称内的字段;
			 */
			boolean isMatch=false;
			for(Field field: allFields){
				for(String fn:propertyNames){
					if(fn.equals(field.getName())){
						isMatch=true;
						break;
					}					
				}
				if(!isMatch){
					list.add(field);
				}
			}
			/*
			 * 将所有不包含在propertyNames内的字段的名-值取出,放到map中去
			 */
			for(Field field: list){
				Object rs=field.get(entity);
				map.put(field.getName(), rs);
			}			
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, WebUtils.class);
		} 
	}
	
	
	/**
	 * 将实体对象没指定的属性(propertyNames)的所有其他值放到map里边去,key就是属性名
	 * @param source 
	 * @param destination
	 * @param propertyNames 不要复制的属性名集
	 */
	public static void mapPutToMapEx(Map destination,Map source,String... propertyNames){
		try {
			if(propertyNames!=null){
				for(int i=0;i<propertyNames.length;i++){
					destination.put(propertyNames[i],source.get(propertyNames[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * 获得条件比较操作符对应于com.common.dbutil.Dao相对应的比较符码;
	 * @param flag 可以是 (=,>,<,>=,<=,like)6种操作
	 * @return flag非 (=,>,<,>=,<=,like),返回-1;
	 */
	public static int getCodeByString(String flag){
		if(flag.equals("="))
			return 0;
		else if(flag.equals(">"))
			return 1;
		else if(flag.equals("<"))
			return 2;
		else if(flag.equals(">="))
			return 10;
		else if(flag.equals("<="))
			return 20;
		else if(flag.equals("like"))
			return 3;
		
		return -1;
	}
	
	
}