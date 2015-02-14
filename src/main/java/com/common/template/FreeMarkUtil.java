package com.common.template;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

/**
 * 为freeMarker框架的使用，提供快捷的API;
 * <br/>时间：2012-8-21
 * @author yuqing
 */
public class FreeMarkUtil {
	
	/**
	 * 根据给定的模板，取得动态内容与模板文件合并后的文本内容；
	 * @param filePath 模板文件的物理路径；
	 * @param root 动态数据；
	 * @return 如果合并成功，则返回合并后的文本内容，否则返回"";
	 */
	public static  String getContent(String filePath,Map root){
		Configuration cfg = new Configuration();
		// 指定模板文件从何处加载的数据源，这里设置成一个文件目录。
		try {
			String fileDir=filePath.substring(0,filePath.lastIndexOf("\\"));
			String fileName=filePath.substring(filePath.lastIndexOf("\\")+1);
			cfg.setDirectoryForTemplateLoading(new File(fileDir));
			Template tmp=cfg.getTemplate(fileName,"UTF-8");
			// 指定模板如何检索数据模型，这是一个高级的主题,但先可以这么来用：
			cfg.setObjectWrapper(new DefaultObjectWrapper());	
			StringWriter writer=new StringWriter();
			tmp.process(root, writer);
			return writer.getBuffer().toString();
		} catch (IOException e) {		
			Logger.getLogger(FreeMarkUtil.class).error("错误:找不到模板文件"+filePath);
		} catch (TemplateException e) {
			Logger.getLogger(FreeMarkUtil.class).error("错误:模板文件语法错误!",e);
		}
		return "";
	}
	
	/**
	 * 根据给定的模板，将动态内容与模板文件合并后输出响应；
	 * @param filePath 模板文件的物理路径；
	 * @param root 动态数据；
	 */
	public static  void flushContent(String filePath,Map root,JspWriter out){
		Configuration cfg = new Configuration();
		// 指定模板文件从何处加载的数据源，这里设置成一个文件目录。
		try {
			String fileDir=filePath.substring(0,filePath.lastIndexOf("\\"));
			String fileName=filePath.substring(filePath.lastIndexOf("\\")+1);
			cfg.setDirectoryForTemplateLoading(new File(fileDir));
			Template tmp=cfg.getTemplate(fileName,"UTF-8");
			// 指定模板如何检索数据模型，这是一个高级的主题,但先可以这么来用：
			cfg.setObjectWrapper(new DefaultObjectWrapper());
			tmp.process(root, out);			
			out.flush();
		} catch (IOException e) {		
			Logger.getLogger(FreeMarkUtil.class).error("错误:找不到模板文件"+filePath);
		} catch (TemplateException e) {
			Logger.getLogger(FreeMarkUtil.class).error("错误:模板文件语法错误!",e);
		}
	}

}
