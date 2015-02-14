package com.common.rpc;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * JBOSS JNDI远程对象获取实现
 * @author Administrator
 *
 */
public class RPCJndi<T> implements Rpc<T> {

	@Override
	public T getRemoteObject(String protocol,String host,int port,String resourceUri) {
		Properties props = new Properties();
        props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        props.setProperty("java.naming.provider.url", host+":"+port);
        props.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming");
        try {
			InitialContext ctx = new InitialContext(props);
			return (T)ctx.lookup(resourceUri);
		} catch (NamingException e) {
			Logger.getLogger(this.getClass()).debug(e);
			return null;
		}
	}
	
	@Override
	public T getProxyRemoteObject(String protocol,String host,int port,String resourceUri,String proxyId,int proxyPort) {
		return null;
	}

}
