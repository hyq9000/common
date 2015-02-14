package com.common.xml;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

/**
 * 高度封装XML的解析细节，提供XML文件的常用的读操作
 * <br/>创建时间：2012-6-14
 * @author yuqing
 */
public class XmlReader {
	Document document;

	/**
	 * 采用xml文档的url来构造；
	 * @param url 要求一个合法的URL对象；
	 */
	public XmlReader(URL url) {
		SAXReader reader = new SAXReader();
		try {
			document = reader.read(url);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据XPATH表达式，读取单个节点（属性）的文本内容（值）；
	 * @param xpathExpression 合法的XPATH表达式，该表达式执行后应返回单独的节点；
	 * @return 返回彼配的字符串
	 */
	public String readString(String xpathExpression){
		Object tmp=document.selectObject(xpathExpression);
		//如果该xpath执行的一的结点是个文本元素
		if(tmp instanceof DefaultElement){
			DefaultElement o= (DefaultElement)tmp;
			return o.getText();
		}else
			return tmp.toString();		
	}
	
	/**
	 * 读取多个相同节点的文本内容，并将各结点文本内容组织成一个List<String>实例；
	 * @param xpathExpression 合法的XPATH表达式，该表达式需是一个查询多点节点的有效XPATH;
	 * @return 返回彼配XPATH的节点的文本内容的List<String>实例；
	 */
	public List<String> readList(String xpathExpression){
		List<DefaultElement> rs= (List<DefaultElement> )document.selectNodes(xpathExpression);
		List<String> list=new ArrayList<String>();
		for(DefaultElement de:rs){
			list.add(de.getText());
		}
		return list;
	}
	
	/**
	 * 还不能用；
	 * 读取符合给定XPATH表达式的结点的所有子节点文本，组成一个以子结点名为key,
	 * 子节点内容文本为值的Map<String,String>集合；
	 * @param xpathExpression 合法的xpath表达式；它要求是查询单个结点，且该结点有子结点；
	 * @return 读取成功返回Map,失败返回null;
	 */
	public Map<String,String> readSubNode(String xpathExpression){
		List<Element> list=null;
		try {
			list = document.selectNodes(xpathExpression);
			//TODO:读取相同结点；
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将XML中的同一级中的多个相同结点(NODE)集合解析成一个List<Map<String,String>>集合;
	 * 每个NODE被解析成一个Map<String,String>,它映射NODE结点的所有子结点的，此MAP的
	 * key为子结点名，值为子结点的文本内容；
	 * @param xpathExpression 合法的XPATH表达式,该XPATH要求查询多个相同结点的，且都有子结点的；
	 * @return 有则返回，无则返回null;
	 */
	public List<Map<String,Object>> readStructure(String xpathExpression){
		List<Element> list=null;
		try {
			list = document.selectNodes(xpathExpression);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("错误", e);
		}	
		/*
		 * 如果XPATH查询成功,则将每个节点的封装成一个map<key,Object>对象，
		 *(key为子节点的元素名，value为该子节点的文本值)；再将多个map封装到List中；
		 */
		if(list!=null && list.size()>0){
			List<Map<String,Object>> plugins=new ArrayList<Map<String,Object>>();
			for(Element node:list){
				Map<String,Object> plugin=new HashMap<String, Object>();
				List<Element> subList=node.elements();
				for(Element subNode :subList){
					plugin.put(subNode.getName(),subNode.getStringValue());			
				}
				plugins.add(plugin);	
			}
			return plugins;
		}
		return null;
	} 
	
	public static void main(String[] args){
		XmlReader reader=new  XmlReader(XmlReader.class.getResource("cloud_config.xml"));
		/*List<String> list=reader.readList("//tns:class");
		for(String o: list){
			System.out.println(o);
		}*/
		//String content=reader.readString("//tns:server-port");
		List<Map<String,Object>> list=reader.readStructure("//tns:plugin");
		for(Map<String,Object> map :list){
			for(Map.Entry<String,Object> en:map.entrySet()){
				if(en.getKey().equals("static"))					
					System.out.println(en.getKey()+":"+en.getValue()+":");
				//System.out.println(en.getKey()+":"+en.getValue()+"*");
			}
		}
	}
}
