package com.common.rpc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.net.MalformedURLException;
import java.net.URL;

import com.caucho.hessian.client.HessianProxyFactory;
import com.common.log.ExceptionLogger;

/**
 * hession过程调用的PRC实现,底层
 * </br>Date 2014-05-24
 * @author hyq 
 * @param <T> 远程调用时，返回的存根对象类型
 */
public abstract class RpcHessian<T> implements Rpc<T> {
	private Class clazz;//=(Class)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	
	
	public RpcHessian(){
		Object superClass=this.getClass().getGenericSuperclass();
		ParameterizedType pt=null;		
		 /* 当加上SPRING的事务管理后，spring采用CGLIB为每个SERVICE实现类自动生成其子类以代理此实现类，
		  * 而：“cls=(Class)((ParameterizedType)(this.getClass().getGenericSuperclass())).getActualTypeArguments()[0];”
		 * 这段代码成功获取泛型参数，前提是'this"得是其SERVICE实现类的实例，所以要获得得spring所产生代理子类的的泛型参数类型时，则需要先获该（SERVICE实现类）后，再执行上面那段代码，故有以下这段；
		 */
		pt=(ParameterizedType)superClass;
		if(superClass instanceof ParameterizedType){
			pt=(ParameterizedType)superClass;
		}else
		{
			Object superClassSuperClass=((Class)superClass).getGenericSuperclass();
			pt=(ParameterizedType)superClassSuperClass;
		}		
		clazz=(Class)pt.getActualTypeArguments()[0];
	}
	
	@Override
	public T getProxyRemoteObject(String protocol, String host, int port,String resourceUri,String proxyIp,int proxyPort) {
		try {
			String url = protocol+"://"+host+":"+port+resourceUri;
			url=this.encodeProxyURl(url,proxyIp, proxyPort);
			HessianProxyFactory factory = new HessianProxyFactory();
			return (T)factory.create(clazz,url);
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, this);
			return null;
		}
	}
	
	
	/**
	 *  生成代理URL,过程如下：
	 *  将真实IP及端口附在url后,用"-REQUEST-"分隔
	 * 	原生URL如：http://209.234.20.45:45678/hotel/dosomething
	 *  生成代理URL如:http://localhost:8888/hotel/dosomething-REDIRECT-209.234.20.45:45678
	 * @param nativeContent 原生Hessian调用的URL；
	 * @param ip 远程资源应用的真实IP
	 * @param port 远程资源应用的真实端口
	 * @return 返回一个新的URI；
	 */
	private  String encodeProxyURl(String nativeUrl,String proxyIp,int proxyPort){
		try {
			URL url=new URL(nativeUrl);
			String host=url.getHost();
			int port =url.getPort();
			String path=url.getPath();
			String newUrl=url.getProtocol()+"://"+proxyIp+":"+proxyPort+path+"-REQUEST-"+host+":"+port;
			return newUrl;
		} catch (MalformedURLException e) {
			ExceptionLogger.writeLog(e, RpcHessian.class);
			return null;
		}
	}
	
	
	@Override
	public T getRemoteObject(String protocol, String host, int port,
			String resourceUri) {
		try {
			String url = protocol+"://"+host+":"+port+resourceUri;
			HessianProxyFactory factory = new HessianProxyFactory();
			return (T)factory.create(clazz,url);
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, this);
			return null;
		}
	}
	

}
