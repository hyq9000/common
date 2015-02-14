package com.common.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

/**
 * 定义一个程序，可以按照料依赖顺序来启动所有服务；
 * 云平台依赖顺序为:
 * <ol>
 *	<li>主数据库；</li>
 * 	<li>从数据库；</li>
 *  <li>amoeba服务器</li>
 *  <li>jboss应用服务器</li>
 *  <li>tomcat服务器</li>
 * </ol>
 * <br/>时间：2012-11-5
 * @author yuqing
 */
public class OrderStartServiceTool {

	/**
	 * 加上方法的详细功能及调用说明；
	 * @param args
	 */
	public static void main(String[] args)  {
		for(String arg: args)
			startService(arg);
	}
	
	
	private static void startService(String args) {
		if(args!=null){
				try {
					int lastIndex=args.lastIndexOf("\\");
					String curdir=args.substring(0,lastIndex),							
							fileName=args.substring(lastIndex+1);
					String cmd="cmd /c pushd "+curdir+" && "+fileName;
					Process pro=Runtime.getRuntime().exec(cmd);
					InputStream ins=pro.getInputStream();	
					Scanner reader=new Scanner(ins);
					while(true){
						String tmp=reader.next();
						System.out.println(tmp);
						if(tmp.indexOf("Server startup in 2537 ms")!=-1)
							break;
					};
					reader.close();
					System.out.println(fileName+"启动成功");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		
	}

}
