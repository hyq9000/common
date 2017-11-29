package com.common.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import com.common.log.ExceptionLogger;

import redis.clients.jedis.Jedis;

/**
 * 类型描述:一个redis缓存机制实现
 * </br>创建时期: 2016年1月20日
 * @author hyq
 */
public class RedisCache implements ICache {
	private Jedis redis;
	public RedisCache(String host,int port){
		this.redis=new Jedis(host, port);
	}
	
	@Override
	public Object get(Serializable key) throws Exception {
		byte[] _key=this.getKey(key);			
		byte[] result=redis.get(_key);
		if(result==null || new String(result).equals("nil"))
			return null;
		else
			return this.byte2Object(result);
		
	}

	@Override
	public void remove(Serializable key) throws Exception {
		byte[] _key=this.getKey(key);		
		if(redis.del(_key)<1)
			ExceptionLogger.writeLog("缓存中没有找到待删除的："+key);		
	}

	@Override
	public void put(Serializable key, Object value) throws Exception {		
		byte[] result=this.object2Byte(value);
		byte[] _key=this.getKey(key);
		redis.set(_key, result);		
	}

	@Override
	public void put(Serializable key, Object value, long timeLength)  throws Exception{	
		byte[] _key=this.getKey(key);	
		byte[] result=this.object2Byte(value);
		
		redis.set(_key, result);
		redis.pexpire(_key, timeLength);
	}
	
	
	@Override
	public void put(Serializable key, String fieldName, Object value, long timeLength) throws Exception {
		byte[] _key=this.getKey(key);
		byte[] _fieldName=this.getKey(fieldName);
		byte[] _value=this.object2Byte(value);
		
		redis.hset(_key, _fieldName, _value);
		redis.pexpire(_key, timeLength);
			
	}
	
	@Override
	public Object get(Serializable key, String fieldName)  throws Exception{
		byte[] _key=this.getKey(key);
		byte[] _fieldName=this.getKey(fieldName);
		byte[] value=redis.hget(_key, _fieldName);
		if(value==null || new String(value).equals("nil"))
			return null;
		else
			return this.byte2Object(value);	
	}
	
	/**
	 * 将对象序列化成字节数组
	 * @param value 
	 * @return
	 * @throws Exception
	 */
	private byte[] object2Byte(Object value) throws IOException{
		ByteArrayOutputStream bo=new ByteArrayOutputStream();
		ObjectOutputStream oo=new ObjectOutputStream(bo);
		oo.writeObject(value);
		byte[] result=bo.toByteArray();
		bo.close();
		oo.close();	
		return result;
	}
	
	/**
	 * 将对象序列化成字节数组
	 * @param value 
	 * @return
	 * @throws Exception
	 */
	private Object byte2Object(byte[] value) throws Exception{
		ByteArrayInputStream bi=new ByteArrayInputStream(value);
		ObjectInputStream oi=new ObjectInputStream(bi);			
		Object result=oi.readObject();
		bi.close();
		oi.close();	
		return result;
	}
	
	/**
	 * 将给定的key转换成byte数组
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private byte[] getKey(Serializable key) throws Exception{
		byte[] _key=null;
		if(key instanceof String)
			_key=((String) key).getBytes();
		else{				
			_key=this.object2Byte(key);				
		}
		return _key;
	}

}
