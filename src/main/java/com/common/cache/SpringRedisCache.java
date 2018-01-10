package com.common.cache;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import com.common.log.ExceptionLogger;

/**
 * 类型描述:一个redis缓存机制实现 </br>创建时期: 2016年1月20日
 * 
 * @author hyq
 */
public class SpringRedisCache implements ICache {
	@Autowired
	private RedisTemplate<Serializable, Serializable> redisTemplate;

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
	public Object get(Serializable key, String fieldName) throws Exception {
		return redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				try {
					byte[] _key = SpringRedisCache.this.getKey(key);
					byte[] _fieldName = SpringRedisCache.this.getKey(fieldName);
					byte[] value = connection.hGet(_key, _fieldName);
					if (value == null || new String(value).equals("nil"))
						return null;
					else
						return SpringRedisCache.this.byte2Object(value);
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
		ByteArrayInputStream bi = new ByteArrayInputStream(value);
		ObjectInputStream oi = new ObjectInputStream(bi);
		Object result = oi.readObject();
		bi.close();
		oi.close();
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
