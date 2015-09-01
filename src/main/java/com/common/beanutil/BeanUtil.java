package com.common.beanutil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.common.log.ExceptionLogger;

/**
 * 提供一个对bean的转换操作；Class类型的相关快捷操作；
 * 此类还不能使用
 * <br/>时间：2012-7-31
 * @author yuqing
 */
public class BeanUtil {
	
	public Object jsonToBean(String jsonStr){
		/*try {
			Set<String> names=jo.keySet();
		  for(String fn : names) {
			  PropertyDescriptor pd = new PropertyDescriptor(fn,CcoreAccountInfo.class);
			  Method setor = pd.getWriteMethod();//获得写方法
			  if(pd.get)
			  setor.invoke(custemer,jo.get(fn).toString());//因为知道是int类型的属性，所以传个int过去就是了。。实际情况中需要判断下他的参数类型
		  }
		} catch (Exception e) {
			e.printStackTrace();
		} */
		return null;
	}
	
	
	/**
	 * 方法功能描述：将dest对象中的属性与orig的对象属性合并：将orig中所有不为null,数字不为0的值，赋值给dest对象同名属性；
	 * @param dest
	 * @param orig
	 */
	public static void mergeProperty(Object dest, Object orig){
		if(orig instanceof Map) {
            Iterator entries = ((Map) orig).entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                String name = (String)entry.getKey();
                Object value=entry.getValue();
                if(value!=null)
                	((Map)dest).put(name, value);
            }
        } else { /* if (orig is a standard JavaBean) */
            PropertyDescriptor[] origDescriptors =PropertyUtils.getPropertyDescriptors(dest);
            for (int i = 0; i < origDescriptors.length; i++) {
                String name = origDescriptors[i].getName();
                if ("class".equals(name)) {
                    continue; //No point in trying to set an object's class
                }
                if (PropertyUtils.isReadable(orig, name) && PropertyUtils.isWriteable(dest, name)) {
                    try {
                        Object value =PropertyUtils.getSimpleProperty(orig, name);
                        if(value!=null){
                        	if((value instanceof Number && ((Number)value).doubleValue()!=0.0) 
                        		|| (value instanceof String && ((String)value).equals(""))
                        		|| !(value instanceof Number)){
                        		PropertyUtils.setProperty(dest, name, value);
                        	}
                        }
                    } catch (Exception e) {
                    	ExceptionLogger.writeLog(e, BeanUtil.class);
                    }
                }
            }
        }
	}

}
