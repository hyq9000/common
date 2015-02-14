package com.common.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

/**
 * 类的说明：该类封装所有struts2中自定义actoin中的公共功能逻辑及变量；
 * <br/>创建时间：2014年1月30日
 * @author hyq
 */
public abstract class Struts2Action extends ActionSupport{
	private static final long serialVersionUID = 2511452188633681328L;
	/**
	 * HttpServletRequest请求对象
	 */
	protected HttpServletRequest request;
	/**
	 * HttpServletResponse请求对象
	 */
	protected HttpServletResponse response;
	/**
	 * PrintWriter字符输出器
	 */
	protected PrintWriter out;
	
	/**
	 * HttpSession会话对象
	 */
	protected HttpSession session;
	
	/**
	 * ServletContext上下文对象
	 */
	protected ServletContext application;
	/**
	 * 方法说明：响应数据修改请求 
	 * @return 
	 * @throws Exception
	 */
	public String update() throws Exception{
		//dosomething
		return null;
	}
	
	/**
	 * 方法说明：响应数据新增请求；
	 * @return
	 * @throws Exception
	 */
	public String add() throws Exception{
		//dosomething
		return null;
	}
	
	/**
	 * 方法说明：响应数据删除请求
	 * @return
	 * @throws Exception
	 */
	public String delete() throws Exception{
		//dosomething
		return null;
	}
	
	
	/**
	 * 方法说明：响应查取所有数据的请求
	 * @return
	 * @throws Exception
	 */
	public String list() throws Exception{
		//dosomething
		return null;
	}
	
	/**
	 * 方法说明：响应条件查询的数据请求
	 * @deprecated 不建议使用了;
	 * @return
	 * @throws Exception
	 */
	public String queryList() throws Exception{
		//dosomething
		return null;
	}
	
	/**
	 * 构造时，将一些常用环境变量初始化；
	 */
	public Struts2Action() {
		request=ServletActionContext.getRequest();
		response=ServletActionContext.getResponse();
		session=request.getSession();
		application=request.getSession().getServletContext();
		try {
			out=response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
