package com.common.rpc;

/**
 * 远程访问的接口，抽象了当前存在的一些远程高访问框架的緢节，以隔离应用与这些框架的藕合；
 * @author yuqing </br>
 * date:2014-05-05
 */
public interface Rpc<T> {
	/**
	 * 根据协议、主机、端口及URI获得RPC远程对象
	 * @param protocol 远程调用协议，如http；
	 * @param host 远程主机地址，可以是IP,域名；
	 * @param port 远程主机提供服务的端口号
	 * @param resourceUrl 远程资源的URL,如jndi的path，hession的servlet路径,restful的uri等；
	 * @return 成功则返回一个对象,否则返回null;
	 */
	public T getRemoteObject(String protocol,String host,int port,String resourceUri);
	
	/**
	 * 根据协议、主机、端口及URI获得RPC远程对象的代理,该方法主要用于在调用与被调用之间，需要定义额外逻辑时；
	 * @param protocol 远程调用协议，如http；
	 * @param host 远程主机地址，可以是IP,域名；
	 * @param port 远程主机提供服务的端口号
	 * @param resourceUrl 远程资源的URL,如jndi的path，hession的servlet路径,restful的uri等；
	 * @return 成功则返回一个对象,否则返回null;
	 */
	public T getProxyRemoteObject(String protocol,String host,int port,String resourceUri,String proxyId,int proxyPort);
}
