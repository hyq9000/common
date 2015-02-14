package com.common.log;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.web.util.WebUtils;

import com.common.log.OperationLog;
import com.common.log.OperationLogService;
import com.common.web.WebContextUtil;

/**
 * 一个系统操作日志的通知，该通知会被织入到所有的Action的标注了@Log的所有方法上去；
 * 以实现为系统的操作加上日志功能；要求被织入的连接点方法上的异常不应处理,而应抛出；
 * <br/>时间：2012-7-16
 * @author yuqing
 */
public class LogAdvice implements MethodInterceptor {	
	private OperationLogService service;
	public void setService(OperationLogService service) {
		this.service = service;
	}	
	
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		Object rs=null;
		Method method=mi.getMethod();
		HttpServletRequest request=ServletActionContext.getRequest();
		HttpSession session=request.getSession();
		Log log=method.getAnnotation(Log.class) ;	
		
		//取得会话的用户实例
		Object user=session.getAttribute(WebContextUtil.USER);
		boolean hasException=false;
		try {
			//目标方法执行成功则写操作成功日志,如果目标方法执行失败则写失败日志；
			rs=mi.proceed();			
		} catch (Exception e) {	
			hasException=true;
			Logger.getLogger(this.getClass()).error("错误:",e);
		}
		
		try {
			//目标方法执行后，再取会话中用户实例；
			user=session.getAttribute(WebContextUtil.USER) ;
		} catch (IllegalStateException e) {
			//如果有会话状态异常（session已经无效），则忽略
			;
		}
		
		String content="";//日志内容
		/*
		 * 如果标为日志的方法，要求会话中要有用户实例，则有视为执行成功，没有则象视为操作失败
		 * 如果不要求，则无异常视成操作成功，有异常视为操作失败
		 */
		if(hasException==false){
			if(log.requireUser()){
				if(user!=null){
					content=log.content();
				}else{
					content=log.content()+"失败";
				}
			}else{
				content=log.content();					
			}	
		}else{
			content=log.content()+"失败";
		}
		
		//取出当前在执行操作的用户姓名；			
		String userName="匿名";
		if(user!=null){			
			//找到用户对象的真实姓名属性并取出真实姓名；
			try {
				Method[] ms=user.getClass().getMethods();
				for(Method m :ms){
					if(m.getAnnotation(UserName.class)!=null){
						userName=m.invoke(user, null).toString();
						break;
					}
				}
			} catch (Exception e) {		
				e.printStackTrace();
			}
			
		}
		new LogThread(request.getRemoteAddr(), userName, content, service).start();
		return rs;
	}

	/**
	 * 每个日志操作都开一个新线程来执行；
	 * <br/>时间：2012-11-9
	 * @author yuqing
	 */
	class LogThread extends Thread{
		private OperationLogService service;;
		private OperationLog opLog=new OperationLog();
		/**
		 * 构造线程执行的必要参数
		 * @param ip 当前发送请求的IP
		 * @param user 当前用户实例；
		 * @param content 日志记录内容；
		 * @param log	日志标注实例；
		 * @param service 日志服务实例；
		 */
		public LogThread(String ip ,String userName,String content,OperationLogService service){			
			this.service=service;
			opLog.setLogIP(ip);
			opLog.setLogTime(new Timestamp(new Date().getTime()));
			opLog.setLogContent(content);
			opLog.setLogUser(userName);			
		}
		
		@Override
		public void run() {	
			try {
				this.service.add(opLog);
			} catch (Exception e) {					
				Logger.getLogger(service.getClass()).error("错误:",e);
			}	
		}			
		
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println(1);
		int i=10,c=0;
		try{
			i=i/0;
			System.out.println(2);
		}catch(Exception e){
			Logger.getLogger("ad").error("");
		}
		System.out.println(3);
	}

}
