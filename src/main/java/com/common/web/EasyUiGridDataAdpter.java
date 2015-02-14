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
 * 定义一个JAVA集合数据到EasyUi组件所需的分页json格式数据的通用适配器；
 * <br/>时间：2014-1-11
 * @author yuqing
 */
public class EasyUiGridDataAdpter {

	/**
	 * 根据easyUi中组件分页组件发过的分页数据，封装成一个Paging对象，并返回；
	 * @param request 
	 * @return 成功则返回实际分页对象，否则返回第1页，每页10行的，总行数为50的分页对象；
	 */
	public static Paging getPaging(HttpServletRequest request){
		//此两值是由页面(EXTjs）自动传过来的分页参数（
		String pageNoStr=request.getParameter("page");
		String limitStr=request.getParameter("rows");
		int pageNo=pageNoStr==null?1:Integer.parseInt(pageNoStr);
		int pageSize=limitStr==null?10:Integer.parseInt(limitStr);	
		Paging paging=new Paging(pageSize, pageNo);
		return paging;
	}
	
	/**
	 * 将JAVA集合对象list，转换成
	 * 符合easyUI中组分页显示所需要特殊的json数据格式；<br/>;	
		{
		 "total":100,//总行数，此值由DAO层自动生成，
		 "rows":[ //JSON数据对象的数组；
		  {"PersonId":1,"Name":"Benjamin Button","Age":17,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":2,"Name":"Douglas Adams","Age":42,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":3,"Name":"Isaac Asimov","Age":26,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":4,"Name":"Thomas More","Age":65,"RecordDate":"\/Date(1320259705710)\/"}
		 ]
		 @param list 要转换数据集合
		 @param paging 分页对象 此值可设置为null，因历史问题，故保留此参数
		 @deprecated 此方法不建议使用，可用adapterList方法来代替些方法;
	 **/
	public static String adapter(List list,Paging paging){			
		return adapterList(list);	
	}
	
	
	
	/**
	 * 将JAVA集合对象list，转换成
	 * 符合easyUI中组分页显示所需要特殊的json数据格式；<br/>;	
		{
		 "total":100,//总行数，此值由DAO层自动生成，
		 "rows":[ //JSON数据对象的数组；
		  {"PersonId":1,"Name":"Benjamin Button","Age":17,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":2,"Name":"Douglas Adams","Age":42,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":3,"Name":"Isaac Asimov","Age":26,"RecordDate":"\/Date(1320259705710)\/"},
		  {"PersonId":4,"Name":"Thomas More","Age":65,"RecordDate":"\/Date(1320259705710)\/"}
		 ]
		 @param list 要转换数据集合
		 @param paging 分页对象 此值可设置为null，因历史问题，故保留此参数
	 **/
	public static String adapterList(List list){	
		Paging paging=null;
		//取得该查询条件下的总行数信息；后将该分页对象从list中删除；
		if(list!=null && list.size() >0 ){
			if(list.get(list.size()-1) instanceof Paging){
				paging=(Paging)list.get(list.size()-1);
				list.remove(list.size()-1);
			}
					
			try {
				//为适应UI组件特性，将结果封装成协议结构； 
				Map map=new HashMap<String, Object>();
				if(paging!=null)
					map.put("total",paging.getTotalCount());
				else
					map.put("total",list.size());
				map.put("rows", list);
				String jsonStr=JSONUtil.serialize(map);
				return jsonStr;
			} catch (JSONException e) {
				Logger.getLogger(EasyUiGridDataAdpter.class).error("错误:",e);
			}
		}
		return "{\"total\":0,\"rows\":[]}";		
	}
	
	
	/**
	 * 
	 * 将单个实体对象转成EasyUi要求的格式；
	 * {
 	 * 	"success":true,
     * 	"row":{"PersonId":5,"Name":"Dan Brown","Age":55,"RecordDate":"\/Date(1320262185197)\/"}
	 * }
	 * @param entity
	 * @return
	 */
	public static String adapter(Object entity){	
		if(entity!=null){
			//取得该查询条件下的总行数信息；后将该分页对象从list中删除；		
			try {
				//为适应UI组件特性，将结果封装成协议结构； 
				Map map=new HashMap<String, Object>();
				map.put("success",true);
				map.put("row", entity);
				String jsonStr=JSONUtil.serialize(map);
				return jsonStr;
			} catch (JSONException e) {
				Logger.getLogger(EasyUiGridDataAdpter.class).error("错误:",e);
			}
		}
		return "{\"success:true\",\"row\":{}}";		
	}
}
