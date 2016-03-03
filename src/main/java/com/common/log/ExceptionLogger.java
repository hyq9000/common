package com.common.log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * 提供统一写异常日志的功能
 * @author yuqing
 * </br>Date 2014-05-10
 */
public class ExceptionLogger{	
	/**
	 * 致命错误
	 */
	public final static  int FATAL=5;
	/**
	 * 系统错误
	 */
	public final static int ERROR=4;
	/**
	 * 功能警告
	 */
	public final static int WARN=3;
	/**
	 * 运行信息
	 */
	public final static int INFO=2;
	/**
	 *  调试信息
	 */
	public final static int DEBUG=1;
	
	/**
	 * 根据指定日志等级，写日志,如果日志级别为ERROR、FATAL，则打印错误日志的ID号
	 * @param grad 日志等级，包括ExceptionLogger.DEBUG、INFO、WARN、ERROR、FATAL
	 * @param logContent
	 * @param exception 导常对象，如没有，可设置为null
	 * @param source 调用日志的类型
	 */
	public synchronized static long writeLog(int grad,String logContent,Exception exception,Class clazz){
		long eid=0-System.currentTimeMillis();
		if(grad==ERROR || grad==FATAL)
			Logger.getLogger("").error("LogId: @"+eid+"||||||"+logContent,exception);
		//LOG4J实现
		else 
			switch(grad){
				case 1:Logger.getLogger(clazz).debug(logContent,exception);break;
				case 2:Logger.getLogger(clazz).info(logContent,exception);break;
				case 3:Logger.getLogger(clazz).warn(logContent,exception);break;
			}
		return eid;
	}
	
	
	/**
	 * 写错误(bug级别为：ERROR)日志,隔离具体体的log实现组件:
	 * @param e 异常实例
	 * @param source 发生异常时对象实例
	 * @return 返回该异常的唯一编号
	 */
	public static long writeLog(Exception e,Object source){
		return writeLog(4, "异常",e, source.getClass());
	}
	
	/**
	 * 写错误(bug级别)为：ERROR日志,隔离具体体的log实现组件:
	 * @param e 异常实例
	 * @param clazz 发生异常时类型实例
	 */
	public static long writeLog(Exception e,Class clazz){
		return writeLog(4, "异常",e, clazz);
	}
	
	/**
	 * 写错误(bug级别info)为：隔离具体体的log实现组件:
	 */
	public static long writeLog(String content){
		return writeLog(2,content,null,ExceptionLogger.class);
	}
	
	
	/**
	 * 写错误(bug级别)为：ERROR日志,隔离具体体的log实现组件:
	 * @param grad	日志等级，包括ExceptionLogger.DEBUG、INFO、WARN、ERROR、FATAL
	 * @param logContent 日志内容
	 */
	public static long writeLog(int grad,String content){
		return writeLog(grad,content,null,ExceptionLogger.class);
	}
	

}
