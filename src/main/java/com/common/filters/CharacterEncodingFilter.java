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
 * 为系统的请求及响应定义统一的编码方式；
 * <br/>创建时间：2012-7-11
 * @author yuqing
 */
public class CharacterEncodingFilter implements Filter {
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding(this.encodeName);
		response.setCharacterEncoding(this.encodeName);
		HttpServletResponse httpResponse=(HttpServletResponse)response;	
		
		String uri=((HttpServletRequest)request).getRequestURI();
		int endIndex=uri.indexOf(".action");
		int subffixIndex=uri.indexOf(".");		
		//请求uri是以.action，或无后辍即视为action请求；
		boolean isAction=endIndex!=-1 || subffixIndex==-1;
		
		/*
		 *为了解决IE/firfox的针对Ajax的兼容性问题,把所有的action响应都设置成plain，
		 *如果某action要设置成html的，自行设置就行了；而且设置成不缓存，当然图片，资
		 *源图片啥的，就都用默认缓存；
		 */

		
		//如果请求的是action，则设置响应类型为文本，不缓存；
		//加了这个true 2015-02-07 意思为所有请求设置字符编码
		if(isAction||true){
			httpResponse.setCharacterEncoding(encodeName);
			httpResponse.setContentType("text/plain;charset="+encodeName);
			//设置响应不缓存
			httpResponse.setHeader("pragma", "no-cache");
			httpResponse.setHeader("cache-control", "no-cache");
			httpResponse.setHeader("expires", "0");	
		}
			
		/*判断提交方式是否为GET 是则 进行 编码转换如果在tomcat.server.xml中定义了编码，则下边代码可去掉-----；
		 (((HttpServletRequest) request).getMethod().equalsIgnoreCase("get")) {
			//获得所有的请求参数,以Map形式存储
			Map map=request.getParameterMap();
			for(Object obj:map.entrySet()){
				Map.Entry entry=(Map.Entry)obj;
				String[] str=(String[])entry.getValue();
				for(int i=0;i<str.length;i++){
					str[i]=new String(str[i].getBytes("iso8859-1"),this.encodeName);
				}
				entry.setValue(str);
			}
		}
		-------------------------------------------------------------------------------------------*/
		chain.doFilter(request, response);
	}
	private String encodeName="UTF-8";
	public void init(FilterConfig arg0) throws ServletException {
		String tmp=arg0.getInitParameter("encode");
		if(tmp!=null && !tmp.equals("")){
			encodeName=tmp;
		}
	}

	public void destroy() {
	}
}
