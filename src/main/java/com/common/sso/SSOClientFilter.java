package com.common.sso;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.common.tools.XORString;
import com.common.web.WebContextUtil;

/**
 * NEWSMY SSO 框架之客户端,目前尚未验证正确性
 * 要求在配置filter时，在initParam为为checkUserURL，loginURL两个值赋值；
 * <li>checkUserURL:SSO框架服务端的用户登陆验证URL
 * <li>loginURL;服务端用户登陆页URL；
 * Date 2012-04-10
 * @author hyq
 */
public class SSOClientFilter implements Filter {	
	String checkUserURL;//SSO框架服务端的用户登陆验证URL；
		//loginURL;//服务端用户登陆页URL；
	String userKey=WebContextUtil.USER;//会话中标识用户对象的key;通过在web.xml配置过滤器时指定；
	
	public void doFilter(ServletRequest paramServletRequest,
			ServletResponse paramServletResponse, FilterChain paramFilterChain)
			throws IOException, ServletException {
		HttpServletRequest request=(HttpServletRequest)paramServletRequest;	
		HttpServletResponse response=(HttpServletResponse)paramServletResponse;
		HttpSession session=request.getSession();
		//如果会话没有用户数据，则提交到SSO服务端去；
		
		if(session.getAttribute(userKey)==null){
			//判断是否为sso服务端过来的请求，如果不是，则将请过发到sso服务端去验证用户；
			//否则就获得SSO发过来的数据保存到sessio中去；
			//TODO:判断是否为sso服务端过的的请法语；
			String qs=request.getQueryString();
			
			qs=URLDecoder.decode(qs, "utf-8");
			qs=new XORString().deciphering(qs);
			qs=URLDecoder.decode(qs, "utf-8");
			int flagIndex=qs.indexOf("checkFlag");
			String flag=qs.substring(flagIndex+10,flagIndex+11);
			//String flag=request.getParameter("checkFlag");
			//如果还没有验证是否登陆，则跳到SSO服务器，否则取出cookie中的用户数据；保存到session中；
			if(flag==null || !flag.equals("1")){
				String targetUrl=request.getRequestURL().toString();
				response.sendRedirect(checkUserURL+"?target="+targetUrl);
			}else{
				//如果是同一个域名的不同应用， 则可以采用cookie作为SSO服务端与客户端的共享中心
				//否则只能通过url参数来传了；
				int userInfoIndex=qs.indexOf("userInfo");
				String userInfo=qs.substring(userInfoIndex+9);
				//String userInfo=request.getParameter("userInfo");				
				if(userInfo!=null){//如果传过来的用户数据，则保存到session
					//userInfo=new String(userInfo.getBytes("iso8859-1"),"utf-8");
					session.setAttribute(userKey, userInfo);
					paramFilterChain.doFilter(request, response);
				}else{				
					Cookie[] cookies=request.getCookies();
					if(cookies!=null){
						//循环检cookies，检索名为UDATA的cookie值；
						for(Cookie c :cookies){
							if(c.getName().equals("UDATA")){
								userInfo=c.getValue();
								break;
							}
						}
					}					
					if(userInfo!=null){
						//将用户的JSON型字符串放入session中；
						userInfo=URLDecoder.decode(userInfo);
						session.setAttribute(userKey, userInfo);
						paramFilterChain.doFilter(request, response);
					}					
				}
			}
		}else
			paramFilterChain.doFilter(request, response);
	}

	public void init(FilterConfig paramFilterConfig) throws ServletException {
		checkUserURL=paramFilterConfig.getInitParameter("checkUserUrl");
		//loginURL=paramFilterConfig.getInitParameter("loginURL");
	}
	public void destroy() {

	}

}
