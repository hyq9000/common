package com.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 简单网络数据传输工具类，封装TCP，UDP数据传输的底层细节；
 * </br>Date: 2014-07-08
 * @author hyq
 */
public class SimpleNetTool {
	/**
	 * 用指定的连接，TCP发送数据
	 * @param con
	 * @param data
	 * @throws IOException
	 */
	public static void sendData(Socket con,byte[] data) throws IOException {
		OutputStream out=con.getOutputStream();		
		out.write(data);		
		out.flush();
	}
	
	/**
	 * 用指定的连接，UDP发送数据报
	 * @param con
	 * @param data
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public static void sendData(DatagramSocket con,byte[] data,String ip,int port) throws IOException {
		DatagramPacket pkg=new DatagramPacket(data,data.length,InetAddress.getByName(ip),port);
		con.send(pkg);
	}
	/**
	 * TCP接收数据
	 * @param con
	 * @param bufSize
	 * @return
	 * @throws IOException
	 */
	public static byte[] recieveData(Socket con,int bufSize) throws IOException {
		byte[] buf=new byte[bufSize];
		//con.setSoTimeout(3000);
		InputStream in=con.getInputStream();					
		int rs = 0,i=1;
		buf[0]=(byte)in.read();
		while(in.available()>0){			
			rs=in.read();
			buf[i++]=(byte)rs;
		}
		byte[] data=new byte[i];
		System.arraycopy(buf,0,data, 0, i);
		return data;
	}
	
	/**
	 * UDP接收数据
	 * @param con
	 * @param bufSize
	 * @return
	 * @throws IOException
	 */
	public static byte[] recieveData(DatagramSocket con,int bufSize) throws IOException {
		byte[] buf=new byte[bufSize];
		final DatagramPacket pkg=new DatagramPacket(new byte[bufSize],bufSize);
		con.receive(pkg);
		return pkg.getData();
	}
	
	
	/**
	 * 判断力给定网络地址是否可达；
	 * @param host 主机网络地址，可以是ip,域名；
	 * @return 可达则返回true,否则返回false
	 */
	public static boolean isReachable(String host){
		return true;
		/*
		try {
			return InetAddress.getByName(host).isReachable(100000);
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}*/		
	}
	
	public static void main(String[] args) {
		System.out.println(isReachable("baidu.com"));
	}
}
