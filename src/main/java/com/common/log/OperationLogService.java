package com.common.log;

/***********************************************************************
 * Module:  OperationLogService.java
 * Author:  zhangfan 
 * Purpose: Defines the Interface OperationLogService
 ***********************************************************************/

import java.util.List;
import java.util.Map;
import java.math.BigInteger;
import java.sql.Timestamp;

import com.common.dbutil.Dao;
import com.common.dbutil.Paging;

/**
 * 定义了操作日志的接口,包括
 * 新增，查询等操作；
 * @author zhouya
 * 创建时间： 2012年7月4日
 */
public interface OperationLogService extends Dao<OperationLog> {
	
	
	
	/**
	 * 批量添加操作日志
	 * @param log
	 * @throws Exception
	 */
	public void addOperationLog(List<OperationLog> logs) throws Exception;
	

	
	/**
	 * 删除最早的N条操作日志
	 * @param delNum 要删除的日志条数
	 * @throws Exception
	 */
	public void delOperationLogByNum(int num) throws Exception;
	
	
	
	/**
	 * 日志查询(通过sql方式)
	 * @param ccoreOperationLog 传入日志对象，
	 * logIP 要查询的日志IP，可以是某一具体IP，也可以是null，是null表示查询条件中没有IP
	 * startTime 要查询的日志开始时间，可以是具体时间，也可以是null，是null表示查询条件中没有IP
	 * endTime 要查询的日志结束时间，可以是具体时间，也可以是null，是null表示查询条件中没有IP
	 * logUser 要查询的产生日志的用户姓名，可以是具体姓名，也可以是null，是null表示查询条件中没有姓名
	 * logContent 要查询的日志内容，可以是具体内容，可以是内容的一部分，也可以是null，是null表示查询条件中没有内容
	 * @return 所有符合条件的日志List集合
	 */
	public List<Map<String,String>> queryLog(OperationLog ccoreOperationLog,String startTime,String endTime,Paging paging) throws Exception;
	
	/**
	 * 日志查询(通过sql方式)
	 * @param ccoreOperationLog 传入日志对象，
	 * logIP 要查询的日志IP，可以是某一具体IP，也可以是null，是null表示查询条件中没有IP
	 * logUser 要查询的产生日志的用户姓名，可以是具体姓名，也可以是null，是null表示查询条件中没有姓名
	 * logContent 要查询的日志内容，可以是具体内容，可以是内容的一部分，也可以是null，是null表示查询条件中没有内容
	 * @return 所有符合条件的日志List集合
	 */
	public List<Map<String,String>> queryLog(OperationLog ccoreOperationLog,Paging paging) throws Exception;
	
	/**
	 * 查询日志的总记录数(通过sql方式)
	 * @param ccoreOperationLog 传入日志对象
	 * startTime 要查询的日志开始时间，可以是具体时间，也可以是null，是null表示查询条件中没有IP
	 * endTime 要查询的日志结束时间，可以是具体时间，也可以是null，是null表示查询条件中没有IP
	 * @return 返回一个int的总数
	 */
	public BigInteger queryLogCount(OperationLog ccoreOperationLog,String startTime,String endTime) throws Exception;
}
