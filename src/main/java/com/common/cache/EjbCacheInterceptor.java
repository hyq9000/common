package com.common.cache;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
/**
 * 拦截所有数据库操作，根据不同的操作类型，操作缓存；
 * 再尚未实现；不能使用；
 * @author yuqing
 */
public class EjbCacheInterceptor {
	@EJB(beanName="NewsmyCache") ApplicationCache appCache;
	@AroundInvoke
	public Object doCache(InvocationContext ctx){	
		Object rs=null;		
		try {
			if(ctx.getMethod().isAnnotationPresent(Cache.class)){
				Cache cc=ctx.getMethod().getAnnotation(Cache.class);
				String key=cc.key();
				rs=appCache.get(key);
				if(rs==null)
					rs=ctx.proceed();				
			}
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
}
