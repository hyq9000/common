package com.common.listeners;


import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.common.web.WebContextUtil;

/**
 * 定义应用启动时的初始化工作
 * 该类的提供的功能简要说明；
 * <br/>创建时间：2012-7-6
 * @author yuqing
 */
public abstract class ApplicationListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		initSystemData(sce.getServletContext());
		doExtends(sce.getServletContext());
	}
	
	/**
	 * 初始化平台核心模块的一些公共数据；如产品类型；
	 */
	private void initSystemData(ServletContext app){		
		try {			
			ApplicationContext ac=WebApplicationContextUtils.getWebApplicationContext(app);
			Map<String,Object> initSystemData=new HashMap<String, Object>();
			//采用模板模式；
			initSystemData();			
			app.setAttribute(WebContextUtil.INIT_SYSTEM_DATA, initSystemData);			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 将一些全局业务数据对象，放到application作用域；
	 * @param initSystemDataMap
	 */
	public abstract void  initSystemData();
	
	/**
	 * 完成一些自定义的操作
	 * @param application
	 */
	public abstract void doExtends(ServletContext application);
	@Override
	public void contextDestroyed(ServletContextEvent sce) {}

}
