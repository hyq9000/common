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
import com.common.web.WebContextUtil;

/**
 * 定义过滤未登陆用户的访问；凡访问受限的资源(action)，都是要求用户登陆，否则跳回登陆页；<br/>
 * 当然也有例外：<init-param>配置了ignoredRequest参数时，此参数中包含的方法名，<br/>
 * 在验证是否登陆时，直接通过；
 * 通过<init-param>loginUri配置登陆页URI;
 * <br/>时间：2012-8-28
 * @author yuqing
 */
public class UserAccessFilter implements Filter {
	
	/**
	 * 一个由","分隔的，多个action方法的字符串；此字符串中包含的方法名，
	 * 在验证是否登陆的过滤时，直接通过；
	 */
	private String ignoredRequest,//action中的哪些方法是不需要进行用户登录状态控制的，
		loginUri;//用户登陆页的URI;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		ignoredRequest=filterConfig.getInitParameter("ignoredRequest");
		loginUri=filterConfig.getInitParameter("loginUri");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req=(HttpServletRequest)request;
		HttpServletResponse resp=(HttpServletResponse)response;
		/*手动处理特定请求，这些请法语不需要用户登陆即可以访问；*/
		
		//判断是否为action请求:
		String uri=req.getRequestURI();
		int endIndex=uri.indexOf(".action");
		int subffixIndex=uri.indexOf(".");	
		//请求uri是以.action，或无后辍即视为action请求；
		boolean isAction=endIndex!=-1 || subffixIndex==-1;
		
		//如果是action的请求，则取得请求action的方法名；
		String requestName="";//请求action的方法名；
		if(isAction){//如果请求是action或servlet；
			/* 
			 * 取得请求action的方法名,有以下三种情况：
			 * 		1,以.action为后辍: userAction!regist.action,得到regist;
			 * 		2,无action为后辍：userAction!login 得到"login";
			 * 		3,没有带方法名:userAction.action ，userAction,得到"";
			 */			
			int startIndex=uri.indexOf("!");//！位置
			//如果有"!"，则取后边的方法名；否则为方法名为"";
			if(startIndex!=-1){
				if(endIndex==-1){//如果没有后辍(.action)，如：userAction!login，则取"!"到? 或 !到末字符，否则取!到.action为方法名；
					//如果没有带请求参数（也就是没有?)则 endIndex为uri之长度，否则endIndex为"?"字符所在index;
					int flagIndex=uri.indexOf("?");
					endIndex=flagIndex==-1?uri.length():flagIndex;
				}
				requestName=uri.substring(startIndex+1,endIndex);
			}
		}
		
		//取得uri中.action或?之前的部分字符串；如/ccore/customer/ttttActin!test.action?aa=xx,截得/ccore/customer/ttttActin!test
		int uriEndIndex=uri.indexOf(".");
		uriEndIndex=uriEndIndex==-1?uri.indexOf("!"):uriEndIndex;
		uriEndIndex=uriEndIndex==-1?uri.length():uriEndIndex;
		String uriTmp=uri.substring(0,uriEndIndex);//取得uri中.action或?之前的部分字符串；
		
		Object user=req.getSession().getAttribute(WebContextUtil.USER);		
		String referred=req.getHeader("referer");
		String proHostPort=req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort();
		
		//如果会话中有用户实例，或者 是action的请求且请求action方法是忽略方法，则过滤通过，否则不通过；
		//如果requestName为"",则ignoredRequest.contains(requestName)也返回true,帮requestName为""时，也算通过
		if(user!=null || (isAction && !requestName.equals("") && ignoredRequest.contains(requestName))){
			chain.doFilter(request, response);
		}else {	
			/*
			 * 为了与firefox兼容而加此行；原因为：
			 * 	<script>js脚本</script>这样的内容被响应到ie时，无论contextType设置为什么，Ie都自动将之看作html
			 *  而到了firefox，则被视为dom Node对象，；故需将contextType要设置成text/html,到前台才能自动执行，或eval来执行；
			 */
			resp.setContentType("text/html");//为了与firefox兼容而加此行；				
			resp.getWriter().print("<html><head><script>if(window.parent) window.parent.location.assign('"+req.getContextPath()+loginUri+"');else window.location.assign('"+req.getContextPath()+loginUri+"')</script></head></html>");
			//resp.sendRedirect(req.getContextPath()+loginUri);	
			resp.getWriter().flush();
		}
	}

	@Override
	public void destroy() {}

}
