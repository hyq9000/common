/***********************************************************************
 * Module:  DaoJpaImpl.java
 * Author:  yuqing
 * Purpose: Defines the Class DaoJpaImpl
 * Date：2012-5-21 
 ************************************************************************/

package com.common.dbutil;


import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.hibernate.Criteria;
import com.common.cache.ApplicationCache;
import com.common.cache.NewsmyCache;
import com.common.cache.NewsmyCacheUtil;
import com.common.tools.TestTool;

/**
 * 采用JPA来实现数据访问接口（DA0);
 * 该实现不针对具体的实体类型，只是提供了一个抽象的默认实现；
 * 对所有数据的管理操作，都增加了缓存处理机制；
 * 创建时间：2012-7-26
 * @author yuqing
 */
public abstract class DaoJpaImpl<T> implements Dao<T> {
	//jpa Entitymanager;
	 @PersistenceContext private EntityManager em;
	//protected EntityManager em=null;	
	//获取缓存全局变量;
	private ApplicationCache appCache = NewsmyCache.getInstance(); 
	private Class cls=(Class)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	
	public DaoJpaImpl(){
		//从配置文件中获得unitName值,以创建EntityManager
		//XmlReader reader = new  XmlReader(this.getClass().getResource("/config.xml"));	
		//String unitName=reader.readString("//tns:persistence-unit-name");
		//if(unitName==null || unitName.trim().equals("")){
			//throw new RuntimeException("连接单元未配置!");
		//}else
			//em=new PersistenceContextReferenceMetaDat
	}
		
	/**
	 * 返回给定QL查询条件所对应的总行数
	 * @param queryStr 查询语句
	 * @param key 缓存key
	 * @param parameters 查询参数
	 * @return 返回查询出来的总数
	 */
	 private int setPagingTotalCount(String queryStr, 
			Serializable key,Object... parameters) {
		//如果该查询条件的总数不存在，则解析QL，并生成统计总数的ql,查得总数；
		//最后总数赋给paging.totalCount,否则，如果存在，则直接取出返回；
		Integer totalCount = appCache.getPagingCache(key);
		if(null == totalCount) {
			String queryStr_=queryStr.trim().toUpperCase();
			//queryStr的格式可能是"from      where ....，也可能是select ... from ..where);
			if(queryStr_.startsWith("FROM")){
				queryStr="SELECT count(*) "+queryStr;
			}else if(queryStr_.startsWith("SELECT")){
				int formIndex=queryStr_.lastIndexOf("FROM");
				String selectPrefix="SELECT count(*) ";
				queryStr=selectPrefix+queryStr.substring(formIndex);
			}
			//queryStr当中可能存在order by xxxxx desc 或order by  xxxx 语句，需要去掉；
			queryStr=queryStr.replaceAll("(?i)order\\s+by\\s+[a-zA-Z0-9_.]+\\s*(desc|asc)?", " ");
			Query queryTotal=em.createQuery(queryStr);
			//遍历参数列表，填充参数
			for(int i=0;parameters!=null &&i<parameters.length;i++){
				queryTotal.setParameter(i+1,parameters[i]);
			}
			totalCount = ((Number)queryTotal.getSingleResult()).intValue();	
			/* 将count存入缓存中 */
			appCache.putPagingCache(key, totalCount);
		}
		
		return totalCount;
	}
	
	/**
	 * 返回给定sql查询条件所对应的总行数(hyq:2014-03-22)
	 * @param queryStr 查询语句
	 * @param key 缓存key
	 * @param parameters 查询参数
	 * @return 返回查询出来的总数
	 */
	private int getPagingTotalCount(String queryStr, 
			Serializable key,final Object... parameters) {
		//如果该查询条件的总数不存在，则解析QL，并生成统计总数的ql,查得总数；
		//最后总数赋给paging.totalCount,否则，如果存在，则直接取出返回；
		Integer totalCount = null;//TODO:此处的缓存暂时没有用，appCache.getPagingCache(key);
		
		String queryStr_=queryStr.trim().toUpperCase();
		int fromIndex=fromIndex=DaoUtils.getFirstFrom(queryStr_); //SQL中第一个有效“FROM”的下标位置
		//删除第一个有效“FROM"之前的所有？对应的参数；
		final Object[] newParams=DaoUtils.getCountFrontOfFirstForm(queryStr_, fromIndex,parameters);
		
		if(null == totalCount) {
			//queryStr的格式可能是select ... from ..where);
			if(queryStr_.startsWith("SELECT")){
				String selectPrefix="SELECT count(*) ";
				queryStr=selectPrefix+queryStr.substring(fromIndex);
			}
			//queryStr当中可能存在order by xxxxx desc 或order by  xxxx 语句，需要去掉；
			queryStr=queryStr.replaceAll("(?i)order\\s+by\\s+[a-zA-Z0-9_.]+\\s*(desc|asc)?", " ");
			org.hibernate.Session session=(org.hibernate.Session)em.getDelegate();
			org.hibernate.SQLQuery queryTotal=session.createSQLQuery(queryStr);
			//Query queryTotal=em.createSQLQuery(queryStr);
			//遍历参数列表，填充参数
			for(int i=0;newParams!=null &&i<newParams.length;i++){
				queryTotal.setParameter(i,newParams[i]);
			}
			totalCount = ((Number)queryTotal.uniqueResult()).intValue();	
			/* 将count存入缓存中 */
			//TODO:appCache.putPagingCache(key, totalCount);
		}				
		return totalCount;
	}
	
	public void add(T entity)  throws Exception{
		/* 新增实体类对象插入数据库 */
		em.persist(entity);
		/* 根据实体类对象获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(entity);
		/* 如果key存在，则更新缓存 */
		if(key!=null)
			appCache.put(key, entity, ApplicationCache.CACHE_TYPE_ADD);		
	}

	
	public void delete(T entity)  throws Exception{
		/* 实体类对象从数据库中删除 */
		em.remove(em.merge(entity));
		/* 根据实体类对象获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(entity);
		/* 如果key存在，则删除缓存 */
		if(key!=null)
			appCache.remove(key, entity);
	}

	
	public int update(T entity)  throws Exception{
		//TODO:在修改前，EM总是会自动的根据OID查询一下；
		/* 根据实体类对象更新数据库 */
		em.merge(entity);
		/* 根据实体类对象获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(entity);
		/* 如果key存在，则更新缓存 */
		if(key!=null)
			appCache.put(key, entity, ApplicationCache.CACHE_TYPE_UPDATE);
		return 1;
	}
	
	public T getById(Serializable id)  throws Exception{
		/* 获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(id,cls);
		/* 根据key，先从缓存中获取对象 */
		Object tmp=appCache.get(key);
		/* 缓存中没有此对象，则从数据库中查找 */
		if(tmp==null){
			/* 从数据库中查找对象 */
			T o = (T) em.find(cls, id);
			/* 如果找到对象，则将对象添加到缓存中 */
			if (o != null){
				appCache.put(key, o, ApplicationCache.CACHE_TYPE_QUERY);
			}
			return o;
		}
		/* 缓存中有此对象，则直接返回 */
		else
			return (T)tmp;
	}

	
	public List<T> getAll()  throws Exception {
		/* 获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(cls);
		/* 根据key，先从缓存中获取结果集 */
		Object tmp=appCache.get(key);
		/* 缓存中没有结果集时，再从数据库中查找结果集 */
		if(tmp==null){
			/* 根据QL语句从数据库中查找结果 */
			List<T> list=(List<T>)em.createQuery("FROM "+cls.getName()).getResultList();
			/* list合法时，进行后续list处理 */
			if(list!=null && list.size()>0){
				/* 使list线程安全 */
				list=Collections.synchronizedList(list);
				appCache.put(key, list, ApplicationCache.CACHE_TYPE_QUERY);
				return list;
			}else {
				return null;
			}
		}
		/* 缓存中有结果时，直接返回缓存中的结果集 */
		else
			return (List<T>)tmp;
	}

	
	public T getByName(String name) {
		return null;
	}	
	
	
	/**
	 * 执行给定的QL查询,返回查询结果；
	 * @param queryStr 合法的QueryLanguage的查询QL；
	 * @param parameters 查询中的动态参数集
	 * @return 如果查询成功返回List结果，否则返回null;
	 * @throws 执行出错，则抛出异常
	 */
	protected List query(String queryStr,Object... parameters)throws Exception{
	    /* 获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(queryStr,parameters);
		/* 根据key从缓存中查找 */
		Object tmp=appCache.get(key);
		/* 缓存中没有结果集时，再查询数据库 */
		if(tmp==null){			
			Query query=em.createQuery(queryStr);
			/* 拼接QL参数 */
			for(int i=0;parameters!=null &&i<parameters.length;i++){
				query.setParameter(i+1,parameters[i]);
			}
			List list = query.getResultList();
			/* list合法，则放入缓存并返回 */
			if(list != null && list.size() > 0){
				/* 使list线程安全 */
				list = Collections.synchronizedList(list);
				appCache.put(key, list, ApplicationCache.CACHE_TYPE_QUERY);
				return list;
			}
			/* list不合法，则返回null */
			else {
				return null;
			}
			
		}
		/* 缓存中有结果集时，直接返回 */
		else
			return (List)tmp;		
	}
	
	
	//TOD0:缓存没有实现----------------------------------------------------------
	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值数组，查取符合条件的实体集合</br>
	 * propertyNames,opflags,values三个数组的长度必须保持一致；
	 * @param propertyNames  查询条件的属性名称集
	 * @param opFlags 操作符集，可以是“=,!=,<>,>=,<=,<,>,like"这几种，其他不支持
	 * @param values 查询条件属性值集；
	 * @return
	 */
	public  List<T> queryByPropertys(String[] propertyNames, int[] opFlags,int[] conditionFlag,
			Object[] values) throws Exception {
		if(propertyNames==null || propertyNames.length==0 || values==null ||values.length==0 
				|| conditionFlag==null || conditionFlag.length==0
				|| opFlags==null || opFlags.length==0){			
			throw new Exception("条件、参数、值个数不能为空!");
		}else{
			
			if(propertyNames.length!=values.length || propertyNames.length!=opFlags.length ||propertyNames.length!=conditionFlag.length+1){
				throw new Exception("查询条件名、条件值、操作符个数不彼配!");
			}else{
				String hql="FROM "+cls.getName()+" WHERE ";	
				String where="";
				where+=propertyNames[0]+DaoUtils.getOperatedFlagString(opFlags[0])+" ? ";
				for(int i=0;i<conditionFlag.length;i++){
					where+=DaoUtils.getConditionFlagString(opFlags[i])+propertyNames[i+1]+DaoUtils.getOperatedFlagString(opFlags[i+1])+" ? ";
				}
				hql+=where;
				for(int i=0;i<values.length;i++){
					values[i]=DaoUtils.stringToTypes(propertyNames[i], values[i],this.cls);
				}
				return ( List<T> )this.query(hql, values);
			}					
		}
	}
	

	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值数组，分页查取符合条件的实体集合</br>
	 * propertyNames,opflags,values三个数组的长度必须保持一致；
	 * @param propertyNames  查询条件的属性名称集
	 * @param opFlags 操作符集，可以是“=,!=,<>,>=,<=,<,>,like"这几种，其他不支持
	 * @param values 查询条件属性值集；
	 * @param paging 分页对象实例
	 * @return
	 */
	public List<T> queryByPropertys(String[] propertyNames, int[] opFlags,int[] conditionFlag,
			Object[] values,Paging paging) throws Exception {
		if(propertyNames==null || propertyNames.length==0 || values==null ||values.length==0 
				|| conditionFlag==null || conditionFlag.length==0
				|| opFlags==null || opFlags.length==0){			
			throw new Exception("条件、参数、值个数不能为空!");
		}else{
			if(propertyNames.length!=values.length || propertyNames.length!=opFlags.length ||propertyNames.length!=conditionFlag.length+1){
				throw new Exception("查询条件名、条件值、操作符个数不彼配!");
			}else{
				String hql="FROM "+cls.getName()+" WHERE ";
				String where="";
				where+=propertyNames[0]+DaoUtils.getOperatedFlagString(opFlags[0])+" ? ";
				for(int i=0;i<conditionFlag.length;i++){
					where+=DaoUtils.getConditionFlagString(opFlags[i])+propertyNames[i+1]+DaoUtils.getOperatedFlagString(opFlags[i+1])+" ? ";
				}
				hql+=where;
				for(int i=0;i<values.length;i++){
					values[i]=DaoUtils.stringToTypes(propertyNames[i], values[i],this.cls);
				}
				return ( List<T> )this.query(hql, paging,values);
			}
			
		}
	}
	
	
	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值，查取符合条件的实体集合</br>
	 * @param propertyName  查询条件的属性名称集
	 * @param opFlag 操作符集，可以是“=,!=,<>,>=,<=,<,>,like"这几种，其他不支持
	 * @param value 查询条件属性值集；
	 * @return
	 */
	public List<T> queryByProperty(String fieldName, int opFlag,
			Object value) throws Exception {
		if(fieldName==null || fieldName.trim().equals("") || value==null){			
			throw new Exception("条件、参数、值个数不能为空!");
		}else{			
			String op=DaoUtils.getOperatedFlagString(opFlag);
			String hql="FROM "+cls.getName()+" WHERE "+fieldName+op+"?";			
			value = DaoUtils.stringToTypes(fieldName, value,this.cls);
			return ( List<T> )this.query(hql, value);
		}
	}
	
	/**
	 * 方法说明：根据给定查询条件的<b>属性</b>名及操作符、条件值，分页查取符合条件的实体集合</br>
	 * @param propertyName  查询条件的属性名称集
	 * @param opFlag 操作符，可以是“=,<>,>=,<=,<,>,like"这几种，其他不支持
	 * @param value 查询条件属性值集；
	 * @param paging 分页对象实例
	 * @return
	 */
	public List<T> queryByProperty(String fieldName, int opFlag,
			Object value,Paging paging) throws Exception{
		if(fieldName==null || fieldName.trim().equals("") || value==null){
			throw new Exception("条件、参数为空或值个数不正确!");
		}else{			
			String op=DaoUtils.getOperatedFlagString(opFlag);			
			String hql="FROM "+cls.getName()+" WHERE "+fieldName+op+"?";
			value = DaoUtils.stringToTypes(fieldName, value,this.cls);
			return(List<T>)this.query(hql, paging,value);
		}		
	}
	//TOD0:缓存没有实现----------------------------------------------------------	
	/**
	 * 执行给定的QL查询,分页返回查询结果；
	 * @param queryStr 合法的QueryLanguage的查询QL；
	 * @param paging 分页信息实例
	 * @param parameters 查询中的动态参数集
	 * @throws 执行出错，则抛出异常
	 * @return 如果查询成功返回List结果，否则返回null;<br/>
	 * <font color='red'>特别说明：该查询条件下的记录总行数，会封装到一个Paing实例，放在List的最后;
	 * 控制层可以通过Paging.getTotaoCount()方法获得总行数值;</font>
	 */
	protected List query(String queryStr,Paging paging,Object... parameters)throws Exception{
		int totalCount;
		List list = null;
		
		/* 获取缓存key */
		Serializable key = NewsmyCacheUtil.getKey(queryStr,paging.getPageNo(),parameters);
		/* 根据key和paging获取缓存结果集 */
		Object tmp = appCache.get(key, paging);
		/* 没有缓存时，从数据库里面查询结果集 */
		if(tmp==null){
			Query query=em.createQuery(queryStr);
			/* 拼接QL参数 */
			for(int i = 0; parameters != null && i < parameters.length; i++){
				query.setParameter(i+1, parameters[i]);
			}
			/* 根据paging分页信息查询结果 */
			query.setFirstResult((paging.getPageNo()-1)*paging.getpageSize());
			query.setMaxResults(paging.getpageSize());
			list = query.getResultList();
			list = Collections.synchronizedList(list);
			if(null != list && list.size() > 0) {
				/* 获取符合条件的数据总数 */
				totalCount = setPagingTotalCount(queryStr, key, parameters);
				paging.setTotalCount(totalCount);
				/* 将总数及分页实体放到本次查询list的最后 */
				list.add(paging);
				/* list放入缓存中 */
				appCache.put(key, list, paging);
				return list;
			}
			else {
				return null;
			}
		}else {
			list=(List)tmp;
			/* 有缓存时，则取出缓存，并且更新缓存中的paging对象，
			 * 此时需要对list加锁，保证多线程并发安全 */
			synchronized (list) {
				totalCount = setPagingTotalCount(queryStr,key,parameters);
				/* list存放的最后一个对象要是paging对象 */
				if(list.get(list.size() - 1) instanceof Paging){
					/* list存放的最后一个对象要是paging对象,则更新paging的totalCount属性值,
					 * 如果最后一个不是paging，这种情况可能是前台应用把它给删除了，那就把它重新加到最好 */
	                ((Paging)list.get(list.size() - 1)).setTotalCount(totalCount);
			    }else{
			    	paging.setTotalCount(totalCount);
			    	list.add(paging );
			    }
			}
			return list;
		}
		/*
		 * 封装查询总行数值到paging中，并加到List最后；
		 * 该设计是为分布式应用一个不得以而为之的方案，些行："paging.setTotalCount(totalCount);"
		 * 可以将总行数传回给调用者，但因调用者与“我”不在同一JVM中，故只能取此下策，以图解决问题；
		 * 后边的getAll(Paging paging)为同理；
		 */
	}


	public int executeUpdate(String sql,Object... parameters){
		Query query=em.createNativeQuery(sql);
		/* 拼接sql语句参数 */
		for(int i=0;parameters!=null &&i<parameters.length;i++){
			query.setParameter(i+1,parameters[i]);
		}
		/* 执行sql语句 */
		int rs=query.executeUpdate();	
		return rs;
	}
	

	public List executeQuery(String sql, Object... parameters) throws Exception {
		//TODO: 原生的SQL查询尚未实现完全（缓存）；
		//以下代码须在基于hibernate的JPA实现上才有效；
		List<String> alias=DaoUtils.getAliasName(sql);
		org.hibernate.Session session=(org.hibernate.Session)em.getDelegate();
		org.hibernate.SQLQuery query=session.createSQLQuery(sql);
		for(String alia :alias )
			query.addScalar(alia);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		
		/* 拼接sql语句参数 */
		for(int i=0;parameters!=null && i<parameters.length;i++){
			query.setParameter(i,parameters[i]);
		}	
		List rs=query.list();
		rs=Collections.synchronizedList(rs);
		//session.close();不可以关闭，由容器来管理
		return rs;
	}	

	public List getAll(Paging paging)  throws Exception{
		int totalCount;
		//性能测试监时处理程序；
		TestTool tool=new TestTool();
		tool.start();
		/* 获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(cls,paging.getPageNo());
		Object tmp=appCache.get(key, paging);
		tool.end("缓存取操作");
		List list=null;
		String queryStr="FROM "+cls.getName();
		/* 如果没有缓存，则从数据库中查找 */
		if(tmp==null){
			//性能测试监时处理程序；
			TestTool tools=new TestTool();
			tools.start();
		
			Query query=em.createQuery(queryStr);
			query.setFirstResult((paging.getPageNo()-1)*paging.getpageSize());
			query.setMaxResults(paging.getpageSize());
			/* 根据paging查询数据库 */
			list=(List<T>)query.getResultList();
			list=Collections.synchronizedList(list);
			//性能测试监时处理程序；
			tools.end("数据查询操作");
			
			/* list合法，则进行后续操作 */
			if(null != list && list.size() > 0) {
				//性能测试监时处理程序；
				TestTool toolss=new TestTool();
				toolss.start();
                /* 获取查询结果总数，存放到list最后 */
				totalCount = setPagingTotalCount(queryStr, key);
				paging.setTotalCount(totalCount);
				list.add(paging);
				
				/* 数据库查询结果存入缓存 */
				appCache.put(key, list, paging);
				toolss.end("查询缓存刷新操作");
			}
			/* list不合法，则返回null */
			else {
				return null;
			}
		}
		/* 如果有缓存，则从缓存中获取结果 */
		else{
			list = (List<T>)tmp;
			/* 加锁，防止多线程并发操作 */
			synchronized (list) {
				totalCount = setPagingTotalCount(queryStr,key);
				/* list存放的最后一个对象要是paging对象 */
				if(list.get(list.size() - 1) instanceof Paging){
					/* list存放的最后一个对象要是paging对象,则更新paging的totalCount属性值,
					 * 如果最后一个不是paging，这种情况可能是前台应用把它给删除了，那就把它重新加到最好 */
	                ((Paging)list.get(list.size() - 1)).setTotalCount(totalCount);
			    }else{
			    	paging.setTotalCount(totalCount);
			    	list.add(paging );
			    }
			}
		}		
		return list;
	}

	
	public List executeQuery(String sql, Paging paging, Object... parameters)
			throws Exception {
		//TODO：分页查询还未完成（缓存）
		//以下代码须在基于hibernate的JPA实现；
		List<String> alias=DaoUtils.getAliasName(sql);
		org.hibernate.Session session=(org.hibernate.Session)em.getDelegate();
		org.hibernate.SQLQuery query=session.createSQLQuery(sql);
		for(String alia :alias )
			query.addScalar(alia);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		
		/* 拼接sql语句参数 */
		for(int i=0;parameters!=null && i<parameters.length;i++){
			query.setParameter(i,parameters[i]);
		}
		query.setFirstResult((paging.getPageNo()-1)*paging.getpageSize());
		query.setMaxResults(paging.getpageSize());
		/* 执行sql语句，并返回查询结果 */
		List list = query.list();
		
		if(list!=null && list.size()>0){
			if(paging.isGenerateTotalCount()){ //如果需要生成总行数 2014-05-17 hyq
				//计算出该条件下的总数据行数，放在LIST最后；
				int total=this.getPagingTotalCount(sql, sql, parameters);
				paging.setTotalCount(total);
			}
			list.add(paging);		
			list = Collections.synchronizedList(list);
		}
		//session.close();不可以关闭，由容器来管理
		return list;
	}
	
	@Override
	public List executeQuery(String sql) throws Exception {
		return this.executeQuery(sql,(Object)null);
	}
	@Override
	public List executeQuery(String sql, Paging paging) throws Exception {
		return this.executeQuery(sql, paging, null);
	}
	
}