package com.common.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import com.common.dbutil.Paging;

/**
 * 定义一个JAVA集合数据到Jtable中组件所需的分页json格式数据的通用适配器；
 * <br/>时间：2013-12-20
 * @author yuqing
 */
public class JtableGridDataAdpter {

	/**
	 * 根据extjs4中组件Ext.grid.Panel分页组件发过的分页数据，封装成一个Paging对象，
	 * 并返回；
	 * @param request 
	 * @return 成功则返回实际分页对象，否则返回第1页，每页10行的，总行数为50的分页对象；
	 */
	public static Paging getPaging(HttpServletRequest request){
		//此两值是由页面(EXTjs）自动传过来的分页参数（
		String pageNoStr=request.getParameter("page");
		String limitStr=request.getParameter("limit");
		int pageNo=pageNoStr==null?1:Integer.parseInt(pageNoStr);
		int pageSize=limitStr==null?10:Integer.parseInt(limitStr);	
		Paging paging=new Paging(pageSize, pageNo);
		return paging;
	}
	
	/**
	 * 
	 * 将JAVA集合对象list，转换成
	 * 符合jtable中组分页显示所需要特殊的json数据格式；<br/>;	
		{
		 "Result":"OK",//result可以为"OK"或者"ERROR"，
		  Message":"错误描述",//如为后者，可另带Message:;
		 "Records":[ //JSON数据对象的数组；
		  {"PersonId":1,"Name":"Benjamin Button","Age":17,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":2,"Name":"Douglas Adams","Age":42,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":3,"Name":"Isaac Asimov","Age":26,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":4,"Name":"Thomas More","Age":65,"RecordDate":"\/Date(1320259705710)\/"}
		 ]
	 **/
	public static String adapter(List list,Paging paging){			
		//取得该查询条件下的总行数信息；后将该分页对象从list中删除；
		if(list!=null && list.get(list.size()-1) instanceof Paging){
			paging=(Paging)list.get(list.size()-1);
			list.remove(list.size()-1);
		}			
		try {
			//为适应UI组件特性，将结果封装成协议结构； 
			Map map=new HashMap<String, Object>();
			map.put("Result","OK");
			map.put("Records", list);
			String jsonStr=JSONUtil.serialize(map);
			return jsonStr;
		} catch (JSONException e) {
			Logger.getLogger(JtableGridDataAdpter.class).error("错误:",e);
		}
		return "";
		
	}
	
	
	/**
	 * 
	 * 将实体转成JTable要求的格式；
	 * {
 	 * "Result":"OK",
     * "Record":{"PersonId":5,"Name":"Dan Brown","Age":55,"RecordDate":"\/Date(1320262185197)\/"}
	 * }
	 * @param entity
	 * @return
	 */
	public static String adapter(Object entity){			
		//取得该查询条件下的总行数信息；后将该分页对象从list中删除；		
		try {
			//为适应UI组件特性，将结果封装成协议结构； 
			Map map=new HashMap<String, Object>();
			map.put("Result","OK");
			map.put("Record", entity);
			String jsonStr=JSONUtil.serialize(map);
			return jsonStr;
		} catch (JSONException e) {
			Logger.getLogger(JtableGridDataAdpter.class).error("错误:",e);
		}
		return "";
		
	}
}
