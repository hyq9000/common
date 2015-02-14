package com.common.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 定义外网访问控制过滤器： 所有外网IP不允许访问管理中心功能； * 
 * 要求在web.xml中配置此Filter时，定义<init-param>名为include,值为IP列表:允许访问后台的IP地址列表,也就是IP白名单，各ip间用,号隔开；
 * 如192.168.4.1,202.10 意为192.168.4.1和所有202.10开头的IP 都可以访问后台程序；
 * @author hyq
 * </br>Date:2012-09-09
 */
public class OuterAccessFilter implements Filter {


	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		String ip = request.getRemoteAddr();
		String ips[]=includeIp.split(",");
		boolean pass=false;
		//循环的检索源ip是否为白名单成员；
		for(int i=0;i<ips.length;i++){
			if(ip.startsWith(ips[i])){
				pass=true;
				break;
			}
		}
		if (pass) {//如果请求方的源IP彼配被允许访问IP，则放行，否则转到错误页；
			chain.doFilter(request, response);
		} else {
			request.getRequestDispatcher(errorPageUri).forward(request,response);
		}
	}

	/**
	 * 允许访问后台的IP地址列表,也就是IP白名单，各ip间用,号隔开；如192.168.4.1,202.10 意为192.168.4.1和所有202.10开头的IP
	 * 都可以访问后台程序；
	 * 要求在web.xml中配置此Filter时，定义<init-param>名为include,值为IP列表；
	 */
	private String includeIp;
	/**
	 * 发生错误时，错误显示页的uri;
	 */
	private String errorPageUri;

	public void init(FilterConfig fConfig) throws ServletException {
		includeIp = fConfig.getInitParameter("include");
		errorPageUri=fConfig.getInitParameter("errorUri");
	}


	public OuterAccessFilter() {}
	public void destroy() {}
	public static void main(String[] args){
		String tt="12,34",aa="3";
		System.out.println(tt.split(",").length);
		System.out.println(aa.split(",").length);
	}

}
