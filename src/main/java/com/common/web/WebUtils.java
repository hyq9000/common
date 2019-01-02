package com.common.web;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import org.apache.commons.lang.reflect.FieldUtils;
import com.common.log.ExceptionLogger;
import net.sf.json.JSONObject;

/**
 * 类型描述:封装一些与web相关公共逻辑实现 
 * </br>创建时期: 2014年12月27日
 * @author hyq
 */
public class WebUtils {
	
	/**
	 * 将给定参数的值，响应成一个json字符串
	 * @param data  要响应的数据对象
	 * @param code   响应码
	 * @param error 错误文本
	 * @param logId  异常日志唯一串
	 * @return 返回一个JSON串,格式如下: 
	 * <pre>
	 * {
	 * 	* code:  一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2；0:无数据,-100:未知服务器异常,-2:会话超时)
	 *  error: "业务相关错误消息文本",//此属性在code为-n时方有,
	 *  data: {一个具体的业务json对象}，
	 *  logId:一个长整型值,用于标记日志文件中的位置，这个值须com.common.log.ExceptionLogger.getId()得；
	 * }
	 * </pre>
	 */
	public static String responseJson(Object data, String error, int code,
			long logId) {
		return responseJson(data, "", error, code, logId);
	}
	
	/**
	 * 将给定参数的值，响应成一个json字符串
	 * @param data  要响应的数据对象
	 * @param code   响应码
	 * @param error 错误文本
	 * @param logId  异常日志唯一串
	 * @return 返回一个JSON串,格式如下: 
	 * <pre>
	 * {
	 * 	* code:  一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2；0:无数据,-100:未知服务器异常,-2:会话超时)
	 *  error: "业务相关错误消息文本",//此属性在code为-n时方有,
	 *  message:"业务相关的文本消息"，//此属性在code为n时方有； 
	 *  data: {一个具体的业务json对象}，
	 *  logId:一个长整型值,用于标记日志文件中的位置，这个值须com.common.log.ExceptionLogger.getId()得；
	 * }
	 * </pre>
	 */
	public static String responseJson(Object data, String message,String error, int code,
			long logId) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("code", code);
			if (error != null && !error.trim().equals(""))
				map.put("error", error);
			// logId:为写到错误日志文件中的错误日志标识
			if (logId != 0)
				map.put("logId", logId);
			if (data != null)
				map.put("data", data);
			if(message!=null && !message.trim().equals("")) {
				map.put("message", message);
			}

			JsonConfig jsonConfig = new JsonConfig();
			final JsonDateValueProcessor dateValueProcessor = new JsonDateValueProcessor();
			jsonConfig.registerJsonValueProcessor(Timestamp.class,
					dateValueProcessor);
			jsonConfig.registerJsonValueProcessor(Date.class,
					dateValueProcessor);

			return JSONObject.fromObject(map, jsonConfig).toString();
		} catch (Exception e) {
			long eid = ExceptionLogger.writeLog(e, WebUtils.class);
			return "{\"code\":-1,\"error:\":\"服务器异常\",\"logId\":" + eid + "}";
		}
	}

	/**
	 * 只响应响应码、业务数据到客户端,格式:
	 * <pre>
	 * {
	 * 	code:一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2,1:操作成功;0:无数据,-1:未知服务器异常,-2:会话超时)
	 *  data:{一个具体的业务json对象}
	 * }
	 * </pre> 
	 * @param data 业务数据对象
	 * @return
	 */
	public static String responseData(int code, Object data) {
		return responseJson(data, null, code, 0);
	}
	
	/**
	 * 响应消息文本
	 * @param code
	 * @param data
	 * @return
	 * <pre>
	 * {
	 * 	code:-1
	 *  message:"业务消息文本"
	 * }
	 * </pre>
	 */
	public static String responseMessage(String message) {
		return responseJson(null, null, -1, 0);
	}

	/**
	 * 只将业务数据响应到客户端,格式:
	 * 
	 * <pre>
	 * {
	 * 	code:1
	 *  data:{一个具体的业务json对象}
	 * }
	 * </pre>
	 * 
	 * @param data 
	 * @return
	 */
	public static String responseData(Object data) {
		return responseJson(data, null, 1, 0);
	}



	/**
	 * 只响应错误文本,响应码到客户端>
	 * @param error 错误文本
	 * @param code 响应码
	 * @return json格式:
	 *  <pre>
	 * {
	 * 	code:一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2,1:操作成功;0:无数据,-1:未知服务器异常,-2:会话超时)
	 *  error:"业务相关错误消息文本",//此属性在code为n时方有,
	 * }
	 * </pre>
	 */
	public static String responseError(String error, int code) {
		return responseJson(null, error, code, 0);
	}

	/**
	 * 只响应响应码、错误文本、异常日志唯一串到客户端
	 * 
	 * @param error  错误文本
	 * @param code  响应码
	 * @param logId   异常日志唯一串
	 * @return json格式:
	 *  <pre>
	 * {
	 * 	code:一个整数响应码,(+n:正常响应;-n:业务相关错误码,须少于-2,1:操作成功;0:无数据,-1:未知服务器异常,-2:会话超时)
	 *  error:"业务相关错误消息文本",//此属性在code为n时方有,
	 *  logId:“异常日志唯一串”
	 * }
	 * </pre>
	 */
	public static String responseError(String error, int code, long logId) {
		return responseJson(null, error, code, logId);
	}

	/**
	 * 将响应码"-1"、错误文本"未知服务器异常"、logId响应到客户端
	 * @param logId 错误日志唯一串；
	 * @return json格式:
	 * <pre>
	 * {
	 * 	code:-1，
	 *  error:"业务相关错误消息文本",
	 *  logId:“异常日志唯一串”
	 * }
	 * </pre>
	 */
	public static String responseServerException(long logId) {
		return responseJson(null, "服务器发生异常!", -1, logId);
	}

	/**
	 * 输入验证失败，将错误文本、响应码"-14"响应到客户端:
	 * @return json格式
	 * <pre>
	 * {
	 * 	code:-14,
	 * 	error:"错误文本"
	 * }
	 * </pre>
	 */
	public static String responseInputCheckError(String error) {
		return responseJson(null, error, -14, 0);
	}

	/**
	 * 将错误文本"会话超时,请重新登录!"、响应码"-2"响应到客户端
	 * @return json格式
	 * <pre>
	 * {
	 * 	code:-2,
	 * 	error:"会话超时,请重新登录!"
	 * }
	 * </pre>
	 */
	public static String responseSessionTimeout() {
		return responseJson(null, "会话超时,请重新登录!", -2, 0);
	}

	/**
	 * 将响应码响应到客户端,code不可以为-1,-2,-14;
	 * @param code 响应码
	 * @return json格式
	 *  <pre>
	 * {
	 * 	code:code
	 * }
	 * </pre>
	 */
	public static String responseCode(int code) throws RuntimeException {
		if (code == -1 || code == -2 || code == -14) {
			throw new RuntimeException("-1、-2,-14为系统预留值!");
		}
		return responseJson(null, null, code, 0);
	}

	/**
	 * 生成一个给定的KEY、VALUE键值数组的MAP；
	 * @param keys 要放到map里边的key的数组
	 * @param values 要放到与map里value的数组，该数组每个值与keys里对应位置的key，是一个键-值对；
	 * @return 生成好的map对象
	 */
	public static Map generateMapData(String[] keys, Object[] values) {
		Map map = new HashMap<String, Object>();
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		return map;
	}

	/**
	 * 生成一个给定的KEY、VALUE键值数组的MAP；
	 * 
	 * @param keys
	 *            要放到map里边的key的数组
	 * @param values
	 *            要放到与map里value的数组，该数组每个值与keys里对应位置的key，是一个键-值对；
	 * @return 生成好的map对象
	 */
	public static Map generateMapData(String key, Object value) {
		Map map = new HashMap<String, Object>();
		map.put(key, value);
		return map;
	}

	/**
	 * 将实体对象指定的属性(propertyNames)的值放到map里边去,key就是属性名
	 * 
	 * @param map
	 * @param entity
	 * @param propertyNames
	 *            要复制的属性名称集
	 */
	public static void objectPutToMap(Map map, Object entity,
			String... propertyNames) {
		try {
			for (String pro : propertyNames) {
				Field field = FieldUtils.getDeclaredField(entity.getClass(),
						pro, true);
				Object rs = field.get(entity);
				map.put(pro, rs);
			}
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, WebUtils.class);
		}
	}

	/**
	 * 将实体对象所有属性的值放到map里边去,key就是属性名
	 * 
	 * @param map
	 * @param entity
	 *            要复制的对象
	 */
	public static void objectPutToMap(Map map, Object entity) throws Exception {
		try {
			Field[] fields = entity.getClass().getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object rs = field.get(entity);
				map.put(field.getName(), rs);
			}
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, WebUtils.class);
			throw e;
		}
	}

	/**
	 * 将实体对象指定的属性(propertyNames)的值放到map里边去,key就是属性名
	 * 
	 * @param destination
	 *            目标map
	 * @param source
	 *            源map
	 * @param propertyNames
	 *            要复制键值对的的key集
	 */
	public static void mapPutToMap(Map destination, Map source,
			String... propertyNames) {
		try {
			if (propertyNames != null) {
				for (int i = 0; i < propertyNames.length; i++) {
					destination.put(propertyNames[i],
							source.get(propertyNames[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将一个(source)map里的key-value放到(destination)map里边去
	 * 
	 * @param destination
	 *            目标map
	 * @param source
	 *            源map
	 */
	public static void mapPutToMap(Map destination, Map source) {
		destination.putAll(source);
	}

	/**
	 * 将实体对象没指定属性(propertyNames)的所有其他值放到map里边去,key就是属性名
	 * 
	 * @param map
	 * @param entity
	 * @param propertyNames
	 *            不要复制的属性名集
	 */
	public static void objectPutToMapEx(Map map, Object entity,
			String... propertyNames) {
		try {
			Field[] allFields = entity.getClass().getDeclaredFields();
			List<Field> list = new ArrayList<Field>();
			/*
			 * 找到那些不包含在propertyNames名称内的字段;
			 */
			boolean isMatch = false;
			for (Field field : allFields) {
				for (String fn : propertyNames) {
					if (fn.equals(field.getName())) {
						isMatch = true;
						break;
					}
				}
				if (!isMatch) {
					list.add(field);
				}
			}
			/*
			 * 将所有不包含在propertyNames内的字段的名-值取出,放到map中去
			 */
			for (Field field : list) {
				field.setAccessible(true);
				Object rs = field.get(entity);
				map.put(field.getName(), rs);
			}
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, WebUtils.class);
		}
	}

	/**
	 * 将实体对象没指定的属性(propertyNames)的所有其他值放到map里边去,key就是属性名
	 * 
	 * @param source
	 * @param destination
	 * @param propertyNames
	 *            不要复制的属性名集
	 */
	public static void mapPutToMapEx(Map destination, Map source,
			String... propertyNames) {
		try {
			if (propertyNames != null) {
				for (int i = 0; i < propertyNames.length; i++) {
					destination.put(propertyNames[i],
							source.get(propertyNames[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得条件比较操作符对应于com.common.dbutil.Dao相对应的比较符码;
	 * 
	 * @param flag
	 *            可以是 (=,>,<,>=,<=,like)6种操作
	 * @return flag非 (=,>,<,>=,<=,like),返回-1;
	 */
	public static int getCodeByString(String flag) {
		if (flag.equals("="))
			return 0;
		else if (flag.equals(">"))
			return 1;
		else if (flag.equals("<"))
			return 2;
		else if (flag.equals(">="))
			return 10;
		else if (flag.equals("<="))
			return 20;
		else if (flag.equals("like"))
			return 3;

		return -1;
	}

	static class JsonDateValueProcessor implements JsonValueProcessor {
		private String format = "yyyy-MM-dd";
		private String timeFormat="yyyy-MM-dd hh:mm:ss";

		public JsonDateValueProcessor() {
			super();
		}

		public JsonDateValueProcessor(String format) {
			super();
			this.format = format;
		}

		@Override
		public Object processArrayValue(Object paramObject,
				JsonConfig paramJsonConfig) {
			return process(paramObject);
		}

		@Override
		public Object processObjectValue(String paramString,
				Object paramObject, JsonConfig paramJsonConfig) {
			return process(paramObject);
		}

		private Object process(Object value) {
			if (value instanceof Date) {
				SimpleDateFormat sdf = new SimpleDateFormat(format,
						Locale.CHINA);
				return sdf.format(value);
			}
			if (value instanceof Timestamp) {
				Date d = new Date(((Timestamp) value).getTime());
				SimpleDateFormat sdf = new SimpleDateFormat(timeFormat,
						Locale.CHINA);
				return sdf.format(d);

			}
			return value == null ? "" : value.toString();
		}

	}

}