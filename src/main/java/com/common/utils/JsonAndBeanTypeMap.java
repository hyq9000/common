package com.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonValueProcessor;

/**
 * 自实现JSON_lib.jar中的不支持的JSON与BEAN类型转换实现 该类的提供的功能简要说明； <br/>
 * 创建时间：2012-6-28
 * 现在暂时不能用；
 * @author yuqing
 */
public class JsonAndBeanTypeMap implements JsonBeanProcessor,JsonValueProcessor{
	public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
		JSONObject jsonObject = null;
		Logger.getLogger(this.getClass()).info("processor class name:" + bean.getClass().getName());
		if (bean instanceof java.sql.Date) {
			bean = new Date(((java.sql.Date) bean).getTime());
		}
		if (bean instanceof java.sql.Timestamp) {
			Logger.getLogger(this.getClass()).info("bean timestamp");
			bean = new Date(((java.sql.Timestamp) bean).getTime());
		}
		if (bean instanceof Date) {
			jsonObject = new JSONObject();
			jsonObject.element("time", ((Date) bean).getTime());
		} else {
			jsonObject = new JSONObject(true);
		}
		return jsonObject;
	}

	private final String format = "yyyy-MM-dd hh:mm:ss";

	public Object processObjectValue(String key, Object value, JsonConfig arg2) {
		if (value == null)
			return "";
		if (value instanceof Date) {
			String str = new SimpleDateFormat(format).format((Date) value);
			return str;
		}
		return value.toString();
	}

	public Object processArrayValue(Object value, JsonConfig arg1) {
		return null;
	}
}
