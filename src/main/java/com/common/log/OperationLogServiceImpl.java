package com.common.log;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.common.dbutil.DaoHibernateImpl;
import com.common.dbutil.DaoJpaImpl;
import com.common.dbutil.Paging;

/**
 * 操作日志接口的实现，包括日志的添加，查询，删除等操作； <br/>
 * 创建时间：2012-7-4 
 * @author zhouya
 */
public class OperationLogServiceImpl extends DaoHibernateImpl<OperationLog> 
        implements OperationLogService {

	public void addOperationLog(OperationLog log) throws Exception {
		super.add(log);
	}


	public void delOperationLogByNum(int num) throws Exception {
		// TODO Auto-generated method stub
		String sql = "delete from CLOUD_OPERATION_LOG limit ?";
		
		super.executeUpdate(sql, num);
	}

	
	@Override
	public List<Map<String, String>> queryLog(
			OperationLog ccoreOperationLog, Paging paging)  throws Exception{
		try {
			StringBuffer sql=new StringBuffer();
			int i=0;
			Object[] argsTmp=null;
			List<Object> listTmp = null;
			//判断ccoreOperationLog是不是空的true构建带条件的sql false执行优化的sql
			if(ccoreOperationLog!=null){
				listTmp=new ArrayList<Object>();
				sql.append("select * From cloud_operation_log ");
				//ip地址是不是为空
				if(null != ccoreOperationLog.getLogIP()&&!"".equals(ccoreOperationLog.getLogIP())) {
					sql.append(" where log_ip=? ");
					listTmp.add(ccoreOperationLog.getLogIP().trim());
					i++;
				}
				//用户名称是不是为空
				if(null != ccoreOperationLog.getLogUser()&&!"".equals(ccoreOperationLog.getLogUser())) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where log_user like ? ");
					}else {
						sql.append(" and log_user like ? ");
					}
					listTmp.add("%"+ccoreOperationLog.getLogUser().trim()+"%");
					i++;
				}
				//日志内容是不是为空
				/* logContent应该符合like的表达形式，如:%add% */
				if(null != ccoreOperationLog.getLogContent()&&!"".equals(ccoreOperationLog.getLogContent())) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where log_content like ? ");
					}else {
						sql.append(" and log_content like ? ");
					}
					listTmp.add("%"+ccoreOperationLog.getLogContent().trim()+"%");
					i++;
				}
				//如果所有条件都没有添加true执行优化后的sql false继续构建sql
				if(listTmp==null||listTmp.size()<=0){
					listTmp=new ArrayList<Object>();
					sql=new StringBuffer();
					sql.append("select * From cloud_operation_log Where log_id <=(");
					sql.append("select log_id From cloud_operation_log  Order By log_id desc limit ?,1");
					sql.append(") ORDER BY  log_id desc limit ?");
					listTmp.add((paging.getPageNo()-1)*paging.getpageSize());
					listTmp.add(paging.getpageSize());
					argsTmp= listTmp.toArray();
				}else{
					sql.append(" Order By log_id desc limit ?,?");
					listTmp.add((paging.getPageNo()-1)*paging.getpageSize());
					listTmp.add(paging.getpageSize());
					argsTmp= listTmp.toArray();
				}
			}else{
				listTmp=new ArrayList<Object>();
				sql.append("select * From cloud_operation_log Where log_id <=(");
				sql.append("select log_id From cloud_operation_log  Order By log_id desc limit ?,1");
				sql.append(") ORDER BY  log_id desc limit ?");
				listTmp.add((paging.getPageNo()-1)*paging.getpageSize());
				listTmp.add(paging.getpageSize());
				argsTmp= listTmp.toArray();
			}
			long time=System.currentTimeMillis(); 
			List<Map<String,String>> list = this.executeQuery(sql.toString(), argsTmp);
			return list;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public List<Map<String, String>> queryLog(
			OperationLog ccoreOperationLog, String startTime,
			String endTime, Paging paging) throws Exception {
		try {
			StringBuffer sql=new StringBuffer();
			int i=0;
			Object[] argsTmp=null;
			List<Object> listTmp = null;
			//判断ccoreOperationLog是不是空的true构建带条件的sql false执行优化的sql
			if(ccoreOperationLog!=null){
				listTmp=new ArrayList<Object>();
				sql.append("select * From cloud_operation_log ");
				//ip地址是不是为空
				if(null != ccoreOperationLog.getLogIP()&&!"".equals(ccoreOperationLog.getLogIP())) {
					sql.append(" where log_ip=? ");
					listTmp.add(ccoreOperationLog.getLogIP().trim());
					i++;
				}
				//用户名称是不是为空
				if(null != ccoreOperationLog.getLogUser()&&!"".equals(ccoreOperationLog.getLogUser())) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where log_user like ? ");
					}else {
						sql.append(" and log_user like ? ");
					}
					listTmp.add("%"+ccoreOperationLog.getLogUser().trim()+"%");
					i++;
				}
				//检测开始时间和结束时间不为空
				if(null != startTime && null != endTime) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where date_format(log_time,'%Y-%m-%d') >= ? and date_format(log_time,'%Y-%m-%d') <= ? ");
					}else {
						sql.append(" and date_format(log_time,'%Y-%m-%d') >= ? and date_format(log_time,'%Y-%m-%d') <= ? ");
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					listTmp.add(sdf.format(sdf.parse(startTime)));
					listTmp.add(sdf.format(sdf.parse(endTime)));
					i++;
				}
				//日志内容是不是为空
				/* logContent应该符合like的表达形式，如:%add% */
				if(null != ccoreOperationLog.getLogContent()&&!"".equals(ccoreOperationLog.getLogContent())) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where log_content like ? ");
					}else {
						sql.append(" and log_content like ? ");
					}
					listTmp.add("%"+ccoreOperationLog.getLogContent().trim()+"%");
					i++;
				}
				sql.append(" Order By log_id desc limit ?,?");
				listTmp.add((paging.getPageNo()-1)*paging.getpageSize());
				listTmp.add(paging.getpageSize());
				argsTmp= listTmp.toArray();
			}else{
				listTmp=new ArrayList<Object>();
				sql.append("select * From cloud_operation_log Where log_id <=(");
				sql.append("select log_id From cloud_operation_log  Order By log_id desc limit ?,1");
				sql.append(") ORDER BY  log_id desc limit ?");
				listTmp.add((paging.getPageNo()-1)*paging.getpageSize());
				listTmp.add(paging.getpageSize());
				argsTmp= listTmp.toArray();
			}
			long time=System.currentTimeMillis(); 
			List<Map<String,String>> list = this.executeQuery(sql.toString(), argsTmp);
			return list;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public BigInteger queryLogCount(OperationLog ccoreOperationLog,
			String startTime, String endTime)  throws Exception{
		try {
			StringBuffer sql=new StringBuffer();
			int i=0;
			Object[] argsTmp=null;
			List<Object> listTmp = null;
			sql.append("select count(*) counts from cloud_operation_log ");
			//判断ccoreOperationLog是不是空的true构建带条件的sql false执行优化的sql
			if(ccoreOperationLog!=null){
				listTmp=new ArrayList<Object>();
				//ip地址是不是为空
				if(null != ccoreOperationLog.getLogIP()&&!"".equals(ccoreOperationLog.getLogIP())) {
					sql.append(" where log_ip=? ");
					listTmp.add(ccoreOperationLog.getLogIP().trim());
					i++;
				}
				//用户名称是不是为空
				if(null != ccoreOperationLog.getLogUser()&&!"".equals(ccoreOperationLog.getLogUser())) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where log_user like ? ");
					}else {
						sql.append(" and log_user like ? ");
					}
					listTmp.add("%"+ccoreOperationLog.getLogUser().trim()+"%");
					i++;
				}
				//检测开始时间和结束时间不为空
				if(null != startTime && null != endTime) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where date_format(log_time,'%Y-%m-%d') >= ? and date_format(log_time,'%Y-%m-%d') <= ? ");
					}else {
						sql.append(" and date_format(log_time,'%Y-%m-%d') >= ? and date_format(log_time,'%Y-%m-%d') <= ? ");
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					listTmp.add(sdf.format(sdf.parse(startTime)));
					listTmp.add(sdf.format(sdf.parse(endTime)));
					i++;
				}
				//日志内容是不是为空
				/* logContent应该符合like的表达形式，如:%add% */
				if(null != ccoreOperationLog.getLogContent()&&!"".equals(ccoreOperationLog.getLogContent())) {
					//检测是不是第一个参数 true 添加带where的字符串 false 添加带and的字符串
					if(0 == i) {
						sql.append(" where log_content like ? ");
					}else {
						sql.append(" and log_content like ? ");
					}
					listTmp.add("%"+ccoreOperationLog.getLogContent().trim()+"%");
					i++;
				}
			}
			if(listTmp!=null&&listTmp.size()>0){
				argsTmp= listTmp.toArray();
			}
			long time=System.currentTimeMillis(); 
			List<Map> list = this.executeQuery(sql.toString(), argsTmp);
			return BigInteger.valueOf(Long.parseLong(list.get(0).get("counts").toString()));
		} catch (Exception e) {
			throw e;
		}
	}


}
