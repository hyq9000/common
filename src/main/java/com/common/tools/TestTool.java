package com.common.tools;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * 性能压力测试时，用于统计各层执行所花时间；
 * <br/>时间：2012-8-1
 * @author yuqing
 */
public class TestTool {
	private static  long startn,startm;	

	/**
	 * 计时开始
	 */
	public  static void start(){
		//记下开始时的纳秒数及hao秒数
		startn=System.nanoTime();
		startm=System.currentTimeMillis();
	} 
	
	/**
	 * 计时结束，并向日志输出此次计时结果；
	 * @param utilName 计时目录模块的名称；
	 */
	public  static void end(String utilName){
		long ns=System.nanoTime()-startn,ms=System.currentTimeMillis()-startm;
		String msg=utilName+"花了:\t\t\t"+ns+"ns\t\t\t"+ms+"ms\n";
		Logger.getLogger(TestTool.class.getName()).info(msg);
	}
}
