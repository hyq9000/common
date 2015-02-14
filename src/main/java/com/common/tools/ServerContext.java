package com.common.tools;

import java.util.Date;

/**
 * 提供获得服务器上下文相关信息的通用的功能接口
 * @author yuqing</br>
 * Date 2014-05-09
 */
public class ServerContext {
	/**
	 * 获取服务器当前时间 
	 * @return 返回系统当前时间的毫秒数
	 */
	public static long getCurrentDateTime(){
		return System.currentTimeMillis();
	}
}
