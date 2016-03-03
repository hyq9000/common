package com.common.dbutil;

import java.lang.reflect.Method;
import javax.persistence.Id;

/**
 * 	为所有实体类型的提供基本的功能逻辑实现及属性； *   
 * 	@author yuqing
 */
public class Entity {
	@Override
	/**
	 *比较两个实体对象的主键值；
	 */
	public boolean equals(Object obj) {
		//比较对象的ID属性值是否想等
		if(obj!=null){			
			Method methodObj=null,methodThis=null;
			try {
				//取得被比较对象的标记为ID的方法；
				Method[] ms=obj.getClass().getMethods();
				for(Method m: ms){
					if(m.getAnnotation(Id.class)!=null){
						methodObj=m;
						break;
					}
				}
				//取得比较对象的标记为ID的方法；
				ms=this.getClass().getMethods();
				for(Method m: ms){
					if(m.getAnnotation(Id.class)!=null){
						methodThis=m;
						break;
					}
				}	
				//获得双方的ID值，进行比较
				if(methodThis!=null && methodObj!=null && methodObj.getClass().equals(methodThis.getClass())){
					return methodThis.invoke(this, null).equals(methodObj.invoke(obj, null));
				}
			} catch (Exception e) {} 
		}
		return super.equals(obj);
	}	
	
	public static void main(String[] args){
		System.out.println(System.currentTimeMillis());
	}
}
