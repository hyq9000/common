package com.common.web;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
/**
 * <ul>定义一些获取web环境常用数据的方法;如：
 * 	<li>当前会话中的已经登陆用户对象；</li>
 *  <li>当前WEB应用的上下文的物理路径；</li>
 *  <li>当前应用的上下文名称；</li>
 *  <li>读写上下文作用域中的数据；
 * <br/>创建时间：2012-6-4
 * @author yuqing
 */
public class WebContextUtil {
	/**
	 * 在web环境的session中存储当前登陆用户实例的KEY值；
	 */
	public  final static String USER="CURRENT_USER",INIT_SYSTEM_DATA="INIT_SYSTEM_DATA";	
	private ServletContext	application;
	private static WebContextUtil instance;
	
	/** 
	 * 方法功能：得到WebContextUtil实例；
	 * @param request 
	 * @return 返回WebContextUtil实例；
	 */
	public static WebContextUtil getIntstance(HttpServletRequest request){
		if(instance==null){
			instance=new WebContextUtil(request);
		}
		return instance;
	}
	
	/** 
	 * 方法功能：得到WebContextUtil实例；
	 * @param application 
	 * @return 返回WebContextUtil实例；
	 */
	public static WebContextUtil getIntstance(ServletContext application){
		if(instance==null){
			instance=new WebContextUtil(application);
		}
		return instance;
	}
	
	
	/** 
	 * 方法功能：得到WebContextUtil实例；
	 * @param pageContext 
	 * @return 返回WebContextUtil实例；
	 * @deprecated 此方法已不建议使用，使用 WebContextUtil(HttpServletRequest request)；
	 */
	public static WebContextUtil getIntstance(PageContext pageContext){
		if(instance==null){
			instance=new WebContextUtil(pageContext);
		}
		return instance;
	}
	
	
	
	private  WebContextUtil(HttpServletRequest request){
		this.application=request.getSession().getServletContext();
	}
	
	private  WebContextUtil(PageContext pageContext){
		this.application=pageContext.getServletContext();
	}
	
	private  WebContextUtil(ServletContext app){
		this.application=app;
	}

	/**
	 * 获得当前web应用的根目录物理路径；
	 * @return 返回带盘符的应用上下文的物理路径；
	 */
	public String getWebRootRealPath(){
		return this.application.getRealPath("/");
	}
	
	/**
	 * 获得当前web应用的上下文名称；
	 * @return 返加友"/"开头的上下文名称；
	 */
	public String getWebRoot(){
		return this.application.getContextPath();
	}
	
	/**
	 * 获得当前会话已经登陆的用户实例；
	 * @param session
	 * @return 返回当前会话的用户对象
	 */
	public Object getCurrentUser(HttpSession session){
		return (Object)session.getAttribute(USER);
	}
	
	/**
	 * 设置当前会话用户实例；
	 * @param session
	 * @return 返回当前会话的用户对象
	 */
	public void setCurrentUser(HttpSession session,Object user){
		session.setAttribute(USER,user);
	}
	
	/**
	 * 从系统上下文作用域中取出缓存的业务数据
	 * @param key 唯一key值
	 * @return 返回给定key所对应的数据值;
	 */
	public Object getInitSystemData(String key){
		return ((Map<String,Object>)application.getAttribute(INIT_SYSTEM_DATA)).get(key);
	}
	
	/**
	 * 将业务数据放到系统上下文（Application)作用域中，以缓存起来;
	 * @param key 唯一key值
	 * @param value 要放的数据
	 */
	public void setInitSystemData(String key,Object value){
		((Map<String,Object>)application.getAttribute(INIT_SYSTEM_DATA)).put(key, value);
	}
}
