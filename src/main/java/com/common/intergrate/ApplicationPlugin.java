
/***********************************************************************
 * Module:  ApplicationPlugin.java
 * Author:  yuqing
 * Purpose: Defines the Interface ApplicationPlugin
 ***********************************************************************/
package com.common.intergrate;

import javax.servlet.ServletContext;

/**
 * 提供外围应用的插件接口；<br/>
 * 	无论外围应用是同构（JAVA），异构，或是可控，不可控，
 * 它们都是以实现插件的方式加入到平台中来；
 * 它与平台工作方式是：
 * 		<li>平台有一个.xml的配置文件，专门配置外围应用的插件；具体可以配置外围应用的
 * 		前台服务URL，后台管理URL等；
 * 		<li>外围应用可以通过重写init()方法，来定义初始化的工作；
 * <br/>创建时间：2012-6-4
 * @author yuqing
 */
public interface ApplicationPlugin {

	/**
	 * 
	 * 定义应用的初始化工作；
	 * @return 如果初始化正确，是返回0,不正确返回-1;
	 * 	也可以返回其他大于0的数值，在实现类中来说明这些数值的具体意义；
	 */
	int init(ServletContext application);
}
