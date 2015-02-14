package com.common.dbutil;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.mybatis.spring.SqlSessionTemplate;

import com.common.cache.ApplicationCache;
import com.common.cache.NewsmyCache;
import com.common.cache.NewsmyCacheUtil;
import com.common.log.ExceptionLogger;
/**
 * Mybatis之DAO实现版,几点说明:
 * 1),如果是分页查询,在SQL映射文件中,将分页的相关sql写清楚;
 * 2),所有分页查询, 在SQL映射文件中都需要有一个查找"当前条件下总条数的"sql映射,该映射的ID格式为,查询数据映射id_COUNT_TOTAL
 * 3),所有多条件查询时,多个条件值须封装到map中去,
 * <br/>创建日期：2014-09-22
 * @author hyq
 * @param <T>
 */
public  class DaoMybatisImpl<T> implements Dao<T>{
	static final int INSERT=0,
			UPDATE=2,
			DELETE=1,
			QUERY_BY_ID=4,
			QUERY_ALL=3;			
	
	//获取缓存全局变量;
	private ApplicationCache appCache =new ApplicationCache() {		
		public void remove(Serializable key, Object value) {}		
		public void putPagingCache(Serializable key, Integer totalCount) {}		
		public void put(Serializable key, Object value, Paging paging) {}		
		public void put(Serializable key, Object value, int cacheType) {}		
		public void init() {}		
		public Integer getPagingCache(Serializable key) {return null;}		
		public Object get(Serializable key, Paging paging) {return null;}		
		public Object get(Serializable key) {return null;}		
		public void clear() {}
	};//NewsmyCache.getInstance(); 
	
	private Class cls=null;
	
	private SqlSessionTemplate sqlSessionTemplate;
	/**
	 * 桥接HibernateDaoSupport
	 * @param sessionFactory
	 */	
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate){
		this.sqlSessionTemplate=sqlSessionTemplate;
	}
	
	public 	DaoMybatisImpl(){					
		Object superClass=this.getClass().getGenericSuperclass();
		ParameterizedType pt=null;
		/*
		 * 当加上SPRING的事务管理后，spring采用CGLIB为每个SERVICE实现类自动生成其子类以代理此实现类，而：“cls=(Class)((ParameterizedType)(this.getClass().getGenericSuperclass())).getActualTypeArguments()[0];”
		 * 这段代码成功获取泛型参数，前提是'this"得是其SERVICE实现类的实例，所以要获得得spring所产生代理子类的的泛型参数类型时，则需要先获该（SERVICE实现类）后，再执行上面那段代码，故有以下这段；
		 */
		if(superClass instanceof ParameterizedType){
			pt=(ParameterizedType)superClass;
		}else
		{
			Object superClassSuperClass=((Class)superClass).getGenericSuperclass();
			pt=(ParameterizedType)superClassSuperClass;
		}		
		cls=(Class)pt.getActualTypeArguments()[0];
	}
	/**
	 * 返回给定QL查询条件所对应的总行数
	 * @param mapId 查询总数的SQL语句映射ID
	 * @param key 缓存key
	 * @param parameters 查询参数
	 * @return 返回查询出来的总数
	 */
	private int getPagingTotalCount(String mapId,Object... parameters) {
		//如果该查询条件的总数不存在，则解析QL，并生成统计总数的ql,查得总数；
		//最后总数赋给paging.totalCount,否则，如果存在，则直接取出返回；
		Integer totalCount = appCache.getPagingCache(mapId);
		if(null == totalCount) {
			try {
				totalCount=this.sqlSessionTemplate.selectOne(mapId, parameters[0]);
			} catch (Exception e) {				
				e.printStackTrace();
				throw new RuntimeException("分页的统计总行数的SQL映射ID规格为:'查数据的SQL映射ID_COUNT_TOTAL';如果查数据SQL映身为'getList',则统计总行数的SQL映射为:getList_COUNT_TOTAL");
			}
			/*if(list_count!=null && list_count.size()>0)
				totalCount=((Number)list_count.get(0)).intValue();	*/				
			/* 将count存入缓存中 */
			appCache.putPagingCache(mapId, totalCount);
		}		
		return totalCount;
	}
	
	/*
	 * 该方法要求新增实体类需要注解 @link(com.common.dbutil.MybatisEntity)的并给定insertMapId值
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#add(java.lang.Object)
	 */
	public void add(T entity) {
		String mapId = getSqlMapId(INSERT);		
		/* 新增实体类对象插入数据库 */
		this.sqlSessionTemplate.insert(mapId, entity);
		/* 根据实体类对象获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(entity);
		/* 如果key存在，则更新缓存 */
		if(key!=null)
			appCache.put(key, entity, ApplicationCache.CACHE_TYPE_ADD);		
	}

	/**
	 * 取得新增实体对象的所需的SQL映射ID值;
	 * @return 操作标识整型值,见常量
	 * <li>DaoMybatisImpl.INSERT 新增
	 * <li>DaoMybatisImpl.DELETE 删除
	 * <li>DaoMybatisImpl.UPDATE 修改
	 * <li>DaoMybatisImpl.QUERY_ALL 查询所有
	 * <li>DaoMybatisImpl.QUERY_BY_ID 根据ID查询
	 * 
	 */
	private String getSqlMapId(int flag) {	
		MyBatisEntity an=null;
		try {
			an = (MyBatisEntity)this.cls.getAnnotation(MyBatisEntity.class);
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, this);
		}
		String mapId="";
		switch(flag){
			case 0: mapId=an.insertMapId();break;
			case 1: mapId=an.deleteMapId();break;
			case 2: mapId=an.updateMapId();break;
			case 3: mapId=an.queryAllMapId();break;
			default: mapId=an.queryByIdMapId();
		}
		/*
		 * 如果设置了命名空间,则 sql mapid加上前辍;
		 */
		if(an.namespace()==null || an.namespace().trim().equals(""))
			return mapId;
		else
			return an.namespace()+"."+mapId;
	}

	/*
	 * 该方法要求新增实体类需要注解 @link(com.common.dbutil.MybatisEntity)的并给定deleteMapId值
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#delete(java.lang.Object)
	 */
	public void delete(T entity) {
		//获取SQL映射ID
		String mapId=this.getSqlMapId(DELETE);
		//得到实体主键ID
		Object enId=getPropertyValueByAnnotation(entity,MybatisId.class);		
		/*实体类对象从数据库中删除 */
		this.sqlSessionTemplate.delete(mapId,enId);
		/* 根据实体类对象获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(entity);
		/* 如果key存在，则删除缓存 */
		if(key!=null)
			appCache.remove(key, entity);
	}

	/**
	 * 根据给定的实体对象，取出被注解的属性的值
	 * @param entity 实体对象
	 * @param annotationType 注解的类型
	 */
	private Object getPropertyValueByAnnotation(T entity,Class annotationType){
		try {
			Method[] methods=entity.getClass().getMethods();
			for(Method method :methods){
				if(method.getAnnotation(annotationType)!=null){
					return method.invoke(entity, null);
				}
			}
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, this);
		}
		return null;
	}

	/*
	 * 该方法要求新增实体类需要注解 @link(com.common.dbutil.MybatisEntity)的并给定updateMapId值
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#update(java.lang.Object)
	 */
	public void update(T entity) {
		//获取SQL映射ID
		String mapId=this.getSqlMapId(UPDATE);
		/* 根据实体类对象更新数据库 */
		this.sqlSessionTemplate.update(mapId, entity);
		/* 根据实体类对象获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(entity);
		/* 如果key存在，则更新缓存 */
		if(key!=null)
			appCache.put(key, entity, ApplicationCache.CACHE_TYPE_UPDATE);
	}

	
	public T getById(Serializable id) {
		/* 获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(id,cls);
		/* 根据key，先从缓存中获取对象 */
		Object tmp=appCache.get(key);
		/* 缓存中没有此对象，则从数据库中查找 */
		if(tmp==null){
			//获取SQL映射ID
			String mapId=this.getSqlMapId(this.QUERY_BY_ID);			
			/* 从数据库中查找对象 */
			T o = (T) this.sqlSessionTemplate.selectOne(mapId,id);
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

	
	public List<T> getAll() {
		//获取SQL映射ID
		String mapId=this.getSqlMapId(this.QUERY_ALL);
		/* 获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(cls);
		/* 根据key，先从缓存中获取结果集 */
		Object tmp=appCache.get(key);
		/* 缓存中没有结果集时，再从数据库中查找结果集 */
		if(tmp==null){			
			/* 根据QL语句从数据库中查找结果 */			
			List<T> list=(List<T>)this.sqlSessionTemplate.selectList(mapId);
			
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
		throw new RuntimeException("该方法还没有实现");
	}	
	
	public int executeUpdate(String mapId,Object... parameters){
		Object params=null;
		if(parameters.length>1){
			throw new RuntimeException("给SQL传多个参数,须封装到map对象!");
		}else{
			params=parameters[0];
		}
		/*
		 * 获得sql映射文件中的namespace值,重新拼接一个XX.XX.mapId格式的字符串
		 */
		mapId=getAnnotationNamespaceValue(mapId);
		int rs=this.sqlSessionTemplate.update(mapId,params);
		return rs;
	}
	
	/**
	 * 取得当前实体类的命名空间值
	 * @return
	 */
	private String getAnnotationNamespaceValue(String mapId){
		try {
			MyBatisEntity an = (MyBatisEntity)this.cls.getAnnotation(MyBatisEntity.class);
			/*
			 * 如果设置了命名空间,则 sql mapid加上前辍;
			 */
			if(an.namespace()==null || an.namespace().trim().equals(""))
				return mapId;
			else
				return an.namespace()+"."+mapId;
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, this);
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#executeQuery(java.lang.String, java.lang.Object[])
	 */
	public List executeQuery(String mapId, Object... parameters) throws Exception {
		Object params=null;
		if(parameters.length>1){
			throw new RuntimeException("给SQL传多个参数,须封装到map对象!");
		}else{
			params=parameters[0];
		}
		/*
		 * 获得sql映射文件中的namespace值,重新拼接一个XX.XX.mapId格式的字符串
		 */
		mapId=getAnnotationNamespaceValue(mapId);
		List list=this.sqlSessionTemplate.selectList(mapId,params);
		return 	Collections.synchronizedList(list);
	}
	
	

	public List getAll(Paging paging) {
		int totalCount=0;
		/* 获取缓存key */
		Serializable key=NewsmyCacheUtil.getKey(cls,paging.getPageNo());
		Object tmp=appCache.get(key, paging);
		//获得查询所有对象的mapId;
		String mapId=this.getSqlMapId(this.QUERY_ALL);
		List list=null;
		//final String queryStr="FROM "+cls.getName();
		//final int pn=paging.getPageNo(),ps=paging.getpageSize();
		/* 如果没有缓存，则从数据库中查找 */
		if(tmp==null){
			//将分页参数作为查询参数打包到查询参数中去
			Object[] parameters={paging.getPageNo(),paging.getPageSize()};
			list=this.sqlSessionTemplate.selectList(mapId,parameters);			
			list=Collections.synchronizedList(list);
			/* list合法，则进行后续操作 */
			if(null != list && list.size() > 0) {
				//生成统计总数的MapId;
				String countMapId=this.getCountTotalOfMapId(mapId);
                /* 获取查询结果总数，存放到list最后 */
				totalCount = getPagingTotalCount(countMapId);
				paging.setTotalCount(totalCount);
				list.add(paging);
				
				/* 数据库查询结果存入缓存 */
				appCache.put(key, list, paging);
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
				//生成统计总数的MapId;
				String countMapId=this.getCountTotalOfMapId(mapId);
				/* list中的paging实例需要重新从缓存里面取 */
		    	totalCount = getPagingTotalCount(countMapId);
		    	/* list存放的最后一个对象要是paging对象,则更新paging的totalCount属性值,
				 * 如果最后一个不是paging，这种情况可能是前台应用把它给删除了，那就把它重新加到最好 */
				if(list.get(list.size() - 1) instanceof Paging) {
					((Paging)list.get(list.size() - 1)).setTotalCount(totalCount);
			    }else{
			    	paging.setTotalCount(totalCount);
			    	list.add(paging );
			    }
			}
		}		
		return list;
	}
	

	/**
	 * 执行给定mapId的查询,参数封装在parameters中;
	 * @param mapId map中的映射ID
	 * @param parameters 查询参数的map
	 */
	protected List executeQuery(String mapId, Map parameter) throws Exception {	
		/*
		 * 获得sql映射文件中的namespace值,重新拼接一个XX.XX.mapId格式的字符串
		 */
		mapId=getAnnotationNamespaceValue(mapId);
		List list=this.sqlSessionTemplate.selectList(mapId,parameter);
		
		return 	Collections.synchronizedList(list);
	}
	
	/**
	 * @param mapId sql映射id
	 * @param paging 分页对象
	 * @param parameters 数组的第一个对象类型要求是Map;
	 */
	public List executeQuery(String mapId, Paging paging, Object... parameters)
			throws Exception {
		Map param =null;
		if(parameters.length>1){
			throw new Exception("给SQL传多个参数,须封装成map对象!");
		}else{
			/*
			 * 分页的相关参数附在查询参数的最后
			*/			
			param = appendCountParameter(paging, parameters[0]);
		}
		/*
		 * 获得sql映射文件中的namespace值,重新拼接一个XX.XX.mapId格式的字符串
		 */
		mapId=getAnnotationNamespaceValue(mapId);
		List list=this.sqlSessionTemplate.selectList(mapId, param);
		
		//在查询结果里附上分页相关数据
		if(list!=null && list.size()>0){
			if(paging.isGenerateTotalCount()){ // 如果需要生成总行数 2014-05-17 hyq
				String countMapId=this.getCountTotalOfMapId(mapId);
				int sql_count=this.getPagingTotalCount(countMapId,param);					
				paging.setTotalCount(sql_count);
			}
			list.add(paging);
		}
		return Collections.synchronizedList(list);		
	}

	/**
	 * 将分页号及每页行数,追加到查询参数的最后;
	 * @param paging 分页对象
	 * @param parameters 原始查询参数集
	 * @return 返回追加后的新参数数组
	 */
	private Map appendCountParameter(Paging paging, Object parameter) {
		Map tmp=null;
		if(parameter instanceof Map){
			tmp=(Map)parameter;				
		}else{
			tmp=new HashMap();
			tmp.put("0", parameter);
		}
		tmp.put("PAGE_LENGTH", paging.getpageSize());
		tmp.put("PAGE_START", (paging.getPageNo()-1)*paging.getPageSize());	
		return tmp;
	}

	
	/**
	 * 获得分页数据所需要的当前条件下的，数据总总条数：格式为：queryDataMapId+"_COUNT_TOTAL"
	 * @param queryDataMapId 查询数据的mapId
	 * @return 
	 */
	private String getCountTotalOfMapId(String queryDataMapId){
		/*
		 * 获得sql映射文件中的namespace值,重新拼接一个XX.XX.mapId格式的字符串
		 */
		//queryDataMapId=getAnnotationNamespaceValue(queryDataMapId);
		return queryDataMapId+"_COUNT_TOTAL";
	}
	
	/* 该方法暂未实现
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#queryByPropertys(java.lang.String[], int[], int[], java.lang.Object[])
	 */
	public  List<T> queryByPropertys(String[] propertyNames, int[] opFlags,int[] conditionFlag,
			Object[] values) throws Exception {
		throw new Exception("mybatis暂不支持该方法的调用!");		
	}	
	

	/* 该方法暂未实现
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#queryByPropertys(java.lang.String[], int[], int[], java.lang.Object[], com.common.dbutil.Paging)
	 */
	public List<T> queryByPropertys(String[] propertyNames, int[] opFlags,int[] conditionFlag,
			Object[] values,Paging paging) throws Exception {
		throw new Exception("mybatis暂不支持该方法的调用!");
	}	
	
	/* 该方法暂未实现
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#queryByProperty(java.lang.String, int, java.lang.Object)
	 */
	public List<T> queryByProperty(String fieldName, int opFlag,
			Object value) throws Exception {
		throw new Exception("mybatis暂不支持该方法的调用!");
	}
	/*
	 * 该方法不作实现
	 * (non-Javadoc)
	 * @see com.common.dbutil.Dao#queryByProperty(java.lang.String, int, java.lang.Object, com.common.dbutil.Paging)
	 */
	public List<T> queryByProperty(String fieldName, int opFlag,
			Object value,Paging paging) throws Exception{
		throw new Exception("mybatis暂不支持该方法的调用!");
	}

	@Override
	public List executeQuery(String sql) throws Exception{
		return this.executeQuery(sql,(Object)null);
	}

	@Override
	public List executeQuery(String sql, Paging paging) throws Exception{
		return this.executeQuery(sql, paging, null);
	}	

}
