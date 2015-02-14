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
 * 定义一个JAVA集合数据到extjs4中组件Ext.grid.Panel所需的分页
 * json格式数据的通用适配器；
 * <br/>时间：2012-9-25
 * @author yuqing
 */
public class ExtjsGridDataAdpter {

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
	 * 符合extjs4中组件Ext.grid.Panel分页显示所需要特殊的json数据格式；<br/>
	 * <font color='red'> <b>注意<b></font><pre>要求Ext.grid.Panel的的Store的proxy的reader的root值必段为"root",
	 * totalProperty必须为"total";如:
	 * 	customerData= Ext.create('Ext.data.Store', {
	 	    model: 'UserAccount',
		    autoLoad: false,
		    pageSize: 20,
		    proxy:{	    
		    	type: 'ajax',
		       	url : '/xxxxx.jsp',
		        reader:{
			    	type: 'json',
			    	<font color='red'>root:"root"</font>,
			    	<font color='red'>totalProperty:'total'</font>				    	
			    }
			}	   
		}); </pre>		
	 * @param list 同一实体对象的List集合；如果该list是分页查询情况下，
	 *  该list的最后一个元素是一个由dao封装好的一个paging对象；
	 * @param pageNo  当前页号,从1开始
	 * @param pageSize 每页行数；
	 * @return 返回转成功后的json格式的字符串；否则返回"";
	 */
	public static String adapter(List list,Paging paging){			
		//取得该查询条件下的总行数信息；后将该分页对象从list中删除；
		if(list!=null && list.get(list.size()-1) instanceof Paging){
			paging=(Paging)list.get(list.size()-1);
			list.remove(list.size()-1);
		}			
		try {
			//为适应UI组件特性，将结果封装成协议结构； 
			Map map=new HashMap<String, Object>();
			map.put("total",paging.getTotalCount());
			map.put("root", list);
			String jsonStr=JSONUtil.serialize(map);
			return jsonStr;
		} catch (JSONException e) {
			Logger.getLogger(ExtjsGridDataAdpter.class).error("错误:",e);
		}
		return "";
		
	}
}
