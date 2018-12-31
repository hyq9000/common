package com.common.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.common.log.ExceptionLogger;

import redis.clients.jedis.Jedis;

/**
 * 类型描述:一个redis缓存机制实现 </br>创建时期: 2016年1月20日
 * 
 * @author hyq
 */
public class SpringRedisCache implements ICache {
	@Autowired
	private RedisTemplate<Serializable, Serializable> redisTemplate;
	/** redis 订阅消息时的队列*/
	//private BlockingQueue subscribeMesageQueuen=new LinkedBlockingDeque();
	
	public void setRedisTemplate(
			RedisTemplate<Serializable, Serializable> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Object get(Serializable key) throws Exception {
		return redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					byte[] result = connection.get(SpringRedisCache.this
							.getKey(key));
					if (result == null || new String(result).equals("nil"))
						return null;
					else
						return SpringRedisCache.this.byte2Object(result);
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
					return null;
				}
			}
		});

	}

	@Override
	public void remove(Serializable key) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					return connection.del(SpringRedisCache.this.getKey(key));
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
					return null;
				}
			}
		});

	}
	
	
	@Override
	public void remove(Serializable key,String fieldName) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					byte[] _key=SpringRedisCache.this.getKey(key);
					byte[] _fieldName=SpringRedisCache.this.getKey(fieldName);
					return connection.hDel(_key,_fieldName);
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
					return null;
				}
			}
		});

	}

	@Override
	public void put(Serializable key, Object value) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					connection.set(SpringRedisCache.this.getKey(key),
							SpringRedisCache.this.object2Byte(value));
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
				}
				return null;
			}
		});

	}

	@Override
	public void put(Serializable key, Object value, long timeLength)
			throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					byte[] _key = SpringRedisCache.this.getKey(key); 
					connection.set(_key,
							SpringRedisCache.this.object2Byte(value));
					connection.pExpire(_key, timeLength);
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
				}
				return null;
			}
		});
	}

	@Override
	public void put(Serializable key, String fieldName, Object value,
			long timeLength) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					byte[] _key = SpringRedisCache.this.getKey(key);
					byte[] _fieldName = SpringRedisCache.this.getKey(fieldName);
					byte[] _value = SpringRedisCache.this.object2Byte(value); 
					connection.hSet(_key, _fieldName, _value);
					connection.pExpire(_key, timeLength);
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
				}
				return null;
			}
		});

	}
	
	
	@Override
	public void put(Serializable key, String fieldName, Object value) throws Exception {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					byte[] _key = SpringRedisCache.this.getKey(key);
					byte[] _fieldName = SpringRedisCache.this.getKey(fieldName);
					byte[] _value = SpringRedisCache.this.object2Byte(value); 
					connection.hSet(_key, _fieldName, _value);
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
				}
				return null;
			}
		});

	}

	@Override
	public Object get(Serializable key, String fieldName) throws Exception {
		return redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					byte[] _key = SpringRedisCache.this.getKey(key);
					byte[] _fieldName = SpringRedisCache.this.getKey(fieldName);
					
					
					if(fieldName.contains(ICache.FILED_NAME_ALL)) {
						//将Map<byte[],byte[]> 转换成Map<Object,Object>返回；
						Map<byte[],byte[]> _value=connection.hGetAll(_key);
						if(_value == null) {
							return null;
						}
						Map<Object,Object> value=new HashMap<Object,Object>();
						for(Map.Entry<byte[],byte[]> entry:_value.entrySet()) {
							Object entrykey=new String(entry.getKey());
							Object entryValue=SpringRedisCache.this.byte2Object(entry.getValue());
							value.put(entrykey, entryValue);
						}
						return value;
					}else {
						byte[] value=connection.hGet(_key,_fieldName);
						
						if (value == null || new String(value).equals("nil")) {
							return null;
						}else {
							return SpringRedisCache.this.byte2Object(value);
						}
					}
					
				} catch (Exception e) {
					ExceptionLogger.writeLog(e, this.getClass());
				}
				return null;
			}
		});
	}
	
	/**
	 * 消息订阅
	 * @param chanelName 频道名称
	 * @return
	 */
	public Object subscribe(BlockingQueue blockQueue, Serializable ... chanelName) {
		try {
			byte[][] chanelList=new byte[chanelName.length][];
			for(int i=0;i<chanelName.length;i++) {
				chanelList[i]=SpringRedisCache.this.getKey(chanelName[i]);
			}
			return redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection)throws DataAccessException {
					connection.subscribe(new MessageListener() {
						@Override
						public void onMessage(Message message, byte[] pattern) {
							try {
								
								byte[] _body=message.getBody();
								Object body=SpringRedisCache.this.byte2Object(_body);
								//加入队列；
								blockQueue.put(body);
							} catch (Exception e) {
								ExceptionLogger.writeLog(e, this.getClass());
							}
						}
					 }, chanelList);
					return null;
				}
			});
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, this.getClass());
		}
		return null;
	}

	/**
	 * 消息发布
	 * @param chanelName 频道名称
	 * @param message 消息数据
	 * @return
	 */
	public Object publish(Serializable chanelName,Object message) {
		return redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
					try {
						byte[] _key=SpringRedisCache.this.getKey(chanelName);
						byte[] _msg=SpringRedisCache.this.object2Byte(message);
						return connection.publish(_key,_msg);
					} catch (Exception e) {
						ExceptionLogger.writeLog(e, this.getClass());
					}
					return null;
			}	
		});
	}
	/**
	 * 将对象序列化成字节数组
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private byte[] object2Byte(Object value) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = new ObjectOutputStream(bo);
		oo.writeObject(value);
		byte[] result = bo.toByteArray();
		bo.close();
		oo.close();
		return result;
	}

	/**
	 * 将对象序列化成字节数组
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private Object byte2Object(byte[] value) throws Exception {
		
		Object result=null;
		ByteArrayInputStream bi = null;
		ObjectInputStream oi = null;
		try {
			bi = new ByteArrayInputStream(value);
			oi = new ObjectInputStream(bi);
			result = oi.readObject();	
		} catch (StreamCorruptedException e) {
			result=new String(value); 		
		}catch (EOFException e) {
			result=new String(value); 		
		}finally{
			try {
				bi.close();
				oi.close();
			} catch (Exception e) {
				ExceptionLogger.writeLog(e,this.getClass());
			}
		}
		return result;
	}

	/**
	 * 将给定的key转换成byte数组
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private byte[] getKey(Serializable key) throws Exception {
		byte[] _key = null;
		if (key instanceof String)
			_key = ((String) key).getBytes();
		else {
			_key = this.object2Byte(key);
		}
		return _key;
	}

}
