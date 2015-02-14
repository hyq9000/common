package com.common.interceptors;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.common.web.ContentSecurityFunnel;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 *  将页面提交内容进行安全处理的漏斗，将提交内容中含用非法html
 * ,css,javascript，sql脚本转换成合法内容
 * <br/>时间：2012-11-19
 * @author yuqing
 */
public class ContentSecurityInterceptor extends AbstractInterceptor {	
	private String method;//要拦截action方法的方法名的列表，各名称用","隔开	
	public void setMethod(String method) {
		this.method = method;
	}


	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		String tmp=invocation.getProxy().getMethod();
		if(method.indexOf(invocation.getProxy().getMethod())!=-1){
			HttpServletRequest request=ServletActionContext.getRequest();
			//获得所有的请求参数,以Map形式存储
			Map map=request.getParameterMap();
			//将所有提交过来文本内容进行脚本安全转换；
			for(Object obj:map.entrySet()){
				Map.Entry entry=(Map.Entry)obj;
				String[] str=(String[])entry.getValue();
				for(int i=0;i<str.length;i++){
					str[i]=ContentSecurityFunnel.getSecurityString(str[i]);
				}
				entry.setValue(str);
			}
		}
		return invocation.invoke();
	}

}
