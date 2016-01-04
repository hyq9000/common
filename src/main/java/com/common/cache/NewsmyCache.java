package com.common.cache;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.Id;

import com.common.dbutil.Paging;
import com.common.xml.XmlReader;
import com.opensymphony.xwork2.util.finder.ClassFinder.Annotatable;


//TODO ：应用缓存机制完善：a)考查三方缓存产品(jboss)
//TODO :没有缓存时，先查询数据库获得list，再查询数据库获得count，这两部操作之间没有锁，不能保证同步，所以数据可能会出问题
/**
 * 提供系统缓存隔离接口的实现：缓存机制自实现；
 * @author yuqing 
 * 创建时间：2012-5-25
 */
public class NewsmyCache implements ApplicationCache{
	ConcurrentHashMap<String, Object> cache=new ConcurrentHashMap<String, Object>();
	//XbaseCachedAdapter cache=new XbaseCachedAdapter(new String[]{"192.168.3.140:7788"},1);
	List<String> classNameList = new ArrayList<String>();
	List<String> qlList = new ArrayList<String>();
	public long timeout;
	public long interval;
	public int cacheMethod;
	private static NewsmyCache instance;

	private NewsmyCache() {
		/* 解析XML文件，读取配置 */
		//TODO：可配置化
		XmlReader reader = new  XmlReader(this.getClass().getResource("/config.xml"));		
		/* 缓存老化时间，配置文件中的单位是秒，这里转化成毫秒 */
		timeout = Integer.parseInt(reader.readString("//tns:cached-timeout")) * 1000;
		/* 老化线程运行间隔，配置文件中的单位是秒，这里转化成毫秒 */
		interval = Integer.parseInt(reader.readString("//tns:clean-interval")) * 1000;
		/* 
		 * 缓存更新的方法
		 * cacheMethod为0时，表示需要到list里面更新具体的数据，此方法可能导致总数和数据有误差
		 * cacheMethod为1时，表示当更新数据时，直接删除相关缓存，此方法不会有数据不对的问题，但是性能可能相对于前面的方法来说较差 
		 */
		cacheMethod = Integer.parseInt(reader.readString("//tns:cached-method"));
		
		List<String> list;
		/* 将需要缓存的类加入全局变量 */
		list = reader.readList("//tns:class");
		for(String o : list) {
			classNameList.add(o);
		}
		/* 将不需要缓存的QL语句加入全局变量 */
		list = reader.readList("//tns:ql");
		for(String o : list) {
			qlList.add(o);
		}
		
		/* 缓存老化线程，当缓存有一定时间没有用后，老化线程自动将缓存的资源老化，防止资源浪费 */
		Thread t = new Thread() {
		    public void run(){
		    	while(true) {
			    	/* 遍历所有缓存，进行缓存老化 */
			    	for(Serializable queryCacheKey : cache.keySet()) {
			    		NewsmyApplicationCache newsmyCache = (NewsmyApplicationCache)cache.get(queryCacheKey);
			    		if(null == newsmyCache) {
			    			continue;
			    		}
			    		/* 如果到了老化时间，直接删除缓存 */
			    		if(newsmyCache.getLastUseTime().getTime() + timeout <= new Date().getTime()) {
			    			cache.remove(queryCacheKey);
			    		}
			    	}
			    	
			    	try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			    }
		    }
		};
		t.start();
	}

	
	/**
	 * 判断className所表示的类是否需要缓存
	 * @param className 类名
	 * @return true 需要缓存
	 * @return false 不需要缓存
	 */
	public boolean classNameFilter(String className) {
		/* 遍历所有需要缓存的类的列表，判断className是否在其中 */
		for(String list : classNameList){
			if(className.equals(list)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * 判断key中的QL是否是需要缓存的QL
	 * @param key 缓存key
	 * @return true 需要缓存
	 * @return false 不需要缓存
	 */
	public boolean qlFilter(String key) {
		/* 遍历所有QL列表，判断key是否在列表中 */
		for(String list : qlList){
			if(-1 != key.indexOf(list)) {
				return false;
			}
		}

		return true;
	}
	
	
	/**
	 * 取得该缓存对象的全局唯一实例；
	 * @return NewsmyCache实例；
	 */
	public static NewsmyCache getInstance(){
		if(instance==null)
			instance=new NewsmyCache();
		return instance;
	}
	
	
	/**
	 * 根据key，从缓存中获取数据
	 * @param key 缓存key
	 * @return 没有对应缓存时返回null
	 * @return 有对应缓存时，返回缓存数据
	 */
	public Object get(Serializable key) {
		NewsmyApplicationCache newsmyCache = (NewsmyApplicationCache)cache.get(key);
		/* 没有缓存时，返回null */
		if(null == newsmyCache) {
			return null;
		}
		/* 有缓存时，更新下缓存老化时间，并返回缓存结果 */
		else {
			newsmyCache.setLastUseTime(new Date());
			return newsmyCache.getVaule();
		}
	}
	
	
	/**
	 * 根据key，paging，从缓存中获取数据
	 * @param key 缓存key
	 * @param paging 分页信息
	 * @return 没有对应缓存时返回null
	 * @return 有对应缓存时，返回缓存数据
	 */
	public Object get(Serializable key, Paging paging) {
		NewsmyApplicationCache newsmyCache = (NewsmyApplicationCache)cache.get(key);
		/* 没有缓存时，返回null */
		if(null == newsmyCache) {
			return null;
		}else {
			/* 每次查询缓存时，更新一下缓存老化时间 */
			newsmyCache.setLastUseTime(new Date());
			/* 根据key从缓存中取出结果，这里的结果是一个hashmap */
			ConcurrentHashMap<String, Object> pagingCache = (ConcurrentHashMap<String, Object>)newsmyCache.getVaule();
			if(null == pagingCache) {
				return null;
			}
			
			/* 从hashmap中，根据paging取得数据库结果 */
			return pagingCache.get(String.valueOf(paging.getPageNo()));
		}
	}

	
	/**
	 * 根据key，paging，向缓存中存放数据
	 * @param key 缓存key
	 * @param value 要到缓存中的数据
	 * @param paging 分页信息
	 */
	public void put(Serializable key, Object value, Paging paging) {
		NewsmyApplicationCache newsmyCache;
		ConcurrentHashMap<String, Object> pagingCache;
		
		/* 存放数据位空时直接返回 */
		if(null == value) {
			return;
		}

		/* 对key进行过滤，符合条件的key才进行缓存 */
		if(qlFilter(key.toString())) {
			newsmyCache = (NewsmyApplicationCache)cache.get(key);
			if(null == newsmyCache) {
				/* 初始化缓存对象 */
				pagingCache = new ConcurrentHashMap<String, Object>();
				newsmyCache = new NewsmyApplicationCache(new Date(), pagingCache);
				/* 缓存对象加入缓存,key中存放的是一个hashmap */
				cache.put(key.toString(), newsmyCache);
			}else {
				pagingCache = (ConcurrentHashMap<String, Object>) newsmyCache.getVaule();
				/* 缓存中的hashmap为空时，构建hashmap对象 */
				if(null == pagingCache) {
					pagingCache = new ConcurrentHashMap<String, Object>();
					newsmyCache.setValue((Object)pagingCache);
					newsmyCache.setLastUseTime(new Date());
				}
			}
			
			/* hashmap里面再以paging作为key存放数据库结果 */
			pagingCache.put(String.valueOf(paging.getPageNo()), value);
		}
	}
	
	
	/**
	 * 根据key向缓存中存放数据，同时更新其他相关缓存
	 * @param key 缓存key
	 * @param value 要到缓存中的数据
	 * @param cacheType 数据库操作类型
	 */
	public void put(Serializable key, Object value, int cacheType) {
		if(value!=null){
			/* 如果需要缓存，需要同时满足如下条件
			 * 1.缓存类型为查询缓存或者缓存的是某些特定的类
			 * 2.缓存QL不是某些特定的QL
			 * 特定的QL和特定的类在配置文件中可以自己配置
			 */
			if(((CACHE_TYPE_QUERY == cacheType) || classNameFilter(value.getClass().getName())) 
					&& qlFilter(key.toString())) {
				/* 初始化缓存对象 */
				NewsmyApplicationCache newsmyCache = new NewsmyApplicationCache(new Date(), value);
				/* 缓存对象加入缓存 */
				cache.put(key.toString(), newsmyCache);
			}
			
			/* 如果是新增实体类，并且不是查询缓存，则更新相关查询缓存 */
			if(key.toString().startsWith("ENTITY:") && CACHE_TYPE_QUERY != cacheType) {
			   putQueryCache(key, value, cacheType);
			}
		}
	}
	
	
	/**
	 * 根据key从缓存中取出count结果
	 * @param key 缓存key
	 * @return 没有缓存时返回null
	 * @return 有缓存时返回缓存内容
	 */
	public Integer getPagingCache(Serializable key) {
		NewsmyApplicationCache newsmyCache = (NewsmyApplicationCache)cache.get(key);
		if(null == newsmyCache) {
			return null;
		}else {
			/* 每次查询缓存时，更新一下缓存老化时间 */
			newsmyCache.setLastUseTime(new Date());
			/* 取出来的缓存是一个hashmap */
			ConcurrentHashMap<String, Object> pagingCache = (ConcurrentHashMap<String, Object>)newsmyCache.getVaule();
			if(null == pagingCache) {
				return null;
			}
			
			/* 从hashmap中取得count结果 */
			return (Integer)pagingCache.get("count");
		}
	}
	
	
	/**
	 * 根据key从向缓存中存放count结果
	 * @param key 缓存key
	 * @param totalCount 数据库查询结果的个数
	 */
	public void putPagingCache(Serializable key, Integer totalCount) {
		NewsmyApplicationCache newsmyCache;
		ConcurrentHashMap<String, Object> pagingCache;
		
		/* 存放count的对象为空时，直接返回 */
		if(null == totalCount) {
			return;
		}
		
		newsmyCache = (NewsmyApplicationCache)cache.get(key);
		/* 缓存为null时，新建一个缓存对象 */
		if(null == newsmyCache) {
			pagingCache = new ConcurrentHashMap<String, Object>();
			newsmyCache = new NewsmyApplicationCache(new Date(), pagingCache);
			/* 缓存对象加入缓存，此时加入的是一个hashmap */
			cache.put(key.toString(), newsmyCache);
		}
		/* 缓存不为null时，从缓存中获取hashmap */
		else {
			pagingCache = (ConcurrentHashMap<String, Object>) newsmyCache.getVaule();
		}
		
		if(null != pagingCache) {
			/* 向hashmap中加入count结果 */
			pagingCache.put("count", totalCount);
		}
	}

	
	
	/**
	 * 清除所有缓存
	 */
	public void clear() {
		cache.clear();		
	}

	
	
	/**
	 * 此方法暂未实现
	 */
	public void init() {
		
	}

	
	
	/**
	 * 根据key删除缓存
	 * @param key 缓存key
	 * @param value 要删除的对象
	 */
	public void remove(Serializable key, Object value) {
		/* 先删除缓存 */
		cache.remove(key);
		
		/* 如果是实体对象删除则删除相关查询缓存 */
		if(key.toString().startsWith("ENTITY:")) {
			putQueryCache(key, value, CACHE_TYPE_DELETE);
		}
	}
	
	
	
	/**
	 * 根据给定的单实体缓存KEY，查取所有与之相关的查询缓存key的集合；
	 * 目的：当单个实体类发生改变后，则应该清理与之相关的所有查询缓存；
	 * @param entityCacheKey 发生更新的实体对象在缓存中的KEY值；
	 */
	public void clearQueryCache(Serializable  entityCacheKey){
		List<Serializable> list=new ArrayList<Serializable>();
		//单个实体对象都是以"ENTITY:实体类名:OID“为KEY进行缓存的，
		//取得两个":"中间的实体类名；；
		String keyStr=entityCacheKey.toString();
		String className=keyStr.substring(keyStr.indexOf(":")+1,keyStr.lastIndexOf(":"));
		//找到缓存中存在该实体类名相关的所有查询缓存的KEY，放到临时集合中；
		for(Serializable queryCacheKey :cache.keySet()){
			String str=queryCacheKey.toString();
			//以'QUERY:'开头的KEY，就是查询缓存，清理掉；
			if(str.startsWith("QUERY:")&& str.indexOf(className)!=-1){
				list.add(str);
			}
		}
		
		//清理掉所有相关查询缓存；
		for(Serializable ser : list){
			cache.remove(ser);
		}		
	}
	
	
	/**
	 * 解析并返回QL语句中的操作符；
	 * 目前只能解析出">=","<=","!=","=","<",">","like"这几种操作符
	 * @param condition QL语句中的条件；
	 * @return 能解析操作符时返回对应的操作符，不能解析时返回null
	 */
	public String getOpertationSign(String condition) {
		
		/* 由于是采用indexof来判断操作符，所以">=", "<=", "!="这些符号判断要在"="之前 */
		if(-1 != condition.indexOf(">=")) {
			return ">=";
		}
		
		if(-1 != condition.indexOf("<=")) {
			return "<=";
		}
		
		if(-1 != condition.indexOf("!=")) {
			return "!=";
		}
		
		if(-1 != condition.indexOf("=")) {
			return "=";
		}
		
		if(-1 != condition.indexOf("<")) {
			return "<";
		}
		
		if(-1 != condition.indexOf(">")) {
			return ">";
		}
		
        if(-1 != condition.indexOf(" like ")) {
        	return "like";
        }
		
		return null;
	}
	
	
	/**
	 * 根据操作符进行条件判断，看看value是否满足要求；
	 * @param str 对象中对应字段的值
	 * @param arg QL语句中某一具体条件的值，用于个value中对应的条件做比较
	 * @param value 用于比较的对象value
	 * @return true 条件符合
	 * @return false 条件不符合或者操作符类型错误
	 */
	public boolean conditionCompare(String str, String opertationSign, String arg) {
		boolean timeStampFlag = false;
		Date strDate = null;
		Date argDate = null;
		
        if(null == str) {
        	return false;
        }
        
		/* 如果arg包含"#",表示是Timestamp类型的数据，需要还原成Timestamp标准格式 */
		if(-1 != arg.indexOf("#")) {
			timeStampFlag = true;
			
			arg = arg.replaceAll("#", ":");
			
			strDate = new Date();
			argDate = new Date();
			
			try {
				strDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
				argDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(arg);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
        
		/* QL语句中操作符是"="时，用equals比较 */
		if(opertationSign.equals("=")) {
			/* 对象中取出的值str与参数列表中的值arg比较 */
			if(str.equals(arg)) {
				return true;
			}else {
				return false;
			}
		}
		/* 操作符">"比较 */
		else if(opertationSign.equals(">")) {
			/* Timestamp类型和int类型要分开比较 */
			if(timeStampFlag) {
				if(strDate.getTime() > argDate.getTime()) {
					return true;
				}else {
					return false;
				}
			}else {
				/* 不是Timestamp类型时，需要转化为int再比较 */
				if(Integer.parseInt(str) > Integer.parseInt(arg)) {
					return true;
				}else {
					return false;
				}
			}

		}
		/* 操作符">="比较 */
		else if(opertationSign.equals(">=")) {
			if(timeStampFlag) {
				if(strDate.getTime() >= argDate.getTime()) {
					return true;
				}else {
					return false;
				}
			}else {
				if(Integer.parseInt(str) >= Integer.parseInt(arg)) {
					return true;
				}else {
					return false;
				}
			}
		}
		/* 操作符"<"比较 */
		else if(opertationSign.equals("<")) {
			if(timeStampFlag) {
				if(strDate.getTime() < argDate.getTime()) {
					return true;
				}else {
					return false;
				}
			}else {
				if(Integer.parseInt(str) < Integer.parseInt(arg)) {
					return true;
				}else {
					return false;
				}
			}
		}
		/* 操作符"<="比较 */
		else if(opertationSign.equals("<=")) {
			if(timeStampFlag) {
				if(strDate.getTime() <= argDate.getTime()) {
					return true;
				}else {
					return false;
				}
			}else {
				if(Integer.parseInt(str) <= Integer.parseInt(arg)) {
					return true;
				}else {
					return false;
				}
			}
		}
		/* 操作符"!="比较 */
		else if(opertationSign.equals("!=")) {
			/* 这里的操作符是"!=",所以相等时返回false，不等时返回true */
			if(str.equals(arg)) {
				return false;
			}else {
				return true;
			}
		}
		/* 操作符"like>"比较 */
		else if(opertationSign.equals("like")) {
			//TODO QL语句里面的like操作符采用indexOf函数来实现，因此只能解决"%***%"这种形式，"**%**"这种形式的匹配不了，如果真有这种形式，需要改成正则表达式匹配 */
			/* 时间应该不会有like操作符，所以这里不对时间类型进行解析 */
			/* 首先要把参数中的"%"去掉才能匹配 */
			arg = arg.replaceAll("%", "");
			if(-1 != str.indexOf(arg)) {
				return true;
			}else {
				return false;
			}
		}

		return false;
	}
	
	
	/**
	 * 判断对象value是否满足key的条件，当操作实体类的时候，判断这个类是否满足相关查询缓存的要求
	 * 这里只实现了单表QL查询解析，以下是能解析的情况：
	 * 1.key的值为    QUEYR:FROM CcoreManager:ALL:分页号
	 * 其中ALL和分页号可有可无
	 * 2.key的值为    QUERY:FROM CcoreManager where managerLoginName=? and managerPassword=?:参数1，参数2:分页号
	 * 其中where条件，参数，分页号可有可无，操作符可以为"=",">=","<=","<",">","!=","like"
	 * 复杂的QL查询不解析，告知调用者，删除复杂查询的缓存，下次需要用到此缓存时，再从数据库中查询，一下是不能解析的情况
	 * 1.包含join的连接查询
	 * 2.子查询
	 * 3.多表查询
	 * @param key 缓存的KEY值；
	 * @param value 要比较的实体对象
	 * @param argString QL参数字符串，为空时表示没有条件
	 * @return true 满足条件或者不能解析的语句
	 * @return false 不满足条件
	 */
	public int needAddCache(String key, Object value, String argString) {
		int i;
		int j;
		int methodFindFlag;
		int length;
		String str = null;
	    String lowerKey = key.toLowerCase();
		
	    /* key中不含查询条件，不需要解析，直接缓存 */
		if(null == argString || 
				argString.trim().equals("") //2014-4-9 hyq新加的一个条件
				|| -1 == lowerKey.indexOf("where")) {
			return 1;
		}
		
		//TODO 需要讨论哪些是属于复杂QL语句
		/* 复杂QL语句不解析，需要删除缓存重新从数据库中查找 */
		if(-1 != lowerKey.indexOf(" join ") || -1 != lowerKey.indexOf(" in ")) {
			return -1;
		}
		
		/* 获得QL参数列表 */
		String[] args = argString.split(",");
		
		/* 获得简单的条件列表，按照"?"分割 */
		String[] condition = lowerKey.split("\\?");
		
		//if(condition.length >= args.length) {
		if(condition.length == args.length) {
			length = args.length;
		}else {
			/* 参数列表中的参数比QL语句中的"?"多，无法解析，需要删除缓存 */
			return -1;
		}
		
		Method[] m = value.getClass().getDeclaredMethods();
		String[] opertationSign = new String[length];
		
		for(i = 0; i < length; i++) {
			/* 解析QL语句中的操作符，目前支持的操作符种类有限 */
			opertationSign[i] = getOpertationSign(condition[i]);
			if(null == opertationSign[i]) {
				/* 不能解析的操作符，删除缓存 */
				return -1;
			}
			
			/* 从条件列表里面获取类的成员变量名 */
			int index = condition[i].indexOf(opertationSign[i]);
			if(index >= 1) {
				/* 除去操作符以及操作符前面的空格 */
				condition[i] = condition[i].substring(0, index).trim();
			}else {
				return -1;
			}

			/* 最后一个空格到最后的字符串是类的成员变量名， */
			condition[i] = condition[i].substring(condition[i].lastIndexOf(" ") + 1);
			
			methodFindFlag = 0;
			for(j = 0; j < m.length; j++) {
				/* 找到对象value对应的方法，如果找到则进行比较，不符合条件的表示不需要缓存 */
				if(m[j].getName().toLowerCase().equals("get"+condition[i].toLowerCase())) {
					methodFindFlag = 1;
					
					try {
						/* 从value对象中取出对应字段的值 */
						Object tmp=m[j].invoke(value);
						str = tmp!=null ?tmp.toString():null;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
					
					/* 看对象的值是否满足查询条件，如果满足则继续比较下一个条件 */
					if(conditionCompare(str, opertationSign[i], args[i])) {
						break;
					}else {
						/* 有一个条件不满足，表示不需要缓存 */
					    return 0;
					}
				}
			}
			
			/* 没找到对应的方法，说明QL语句复杂，无法解析，这种情况删除缓存重新从数据库查找 */
			if(0 == methodFindFlag) {
				return -1;
			}
		}
		
		return 1;
	}
	
	
	/**
	 * 判断缓存key是不是className所代表类的缓存
	 * @param key 缓存的KEY值；
	 * @param className 要查找的类名
     * @return true key是符合className的缓存
     * @return false key不是符合className的缓存
	 */
	public boolean containsSameClass(String key, String className) {
		/*以下三种情况认为不符合要求
		 * 1.key中不包含className的
		 * 2.key中包含className，但是包含位置为key开始位置的（key中包含className的位置的前一个字符应该为" "）
		 * 3.长度不符合要求的
		 * */
		int index = key.indexOf(className);
		if(-1 == index || 0 == index || index+className.length()+1 > key.length()) {
			return false;
		}
		
		/* key中包含className的位置的前一个字符应该为" ","."或者"," */
		String str = key.substring(index-1, index);
		if(str.equals(" ") || str.equals(".") || str.equals(",")) {
			str = key.substring(index+className.length(), index+className.length()+1);
			/* key中包含className的位置的最后一个字符应该为":"," "或者"," */
			if(str.equals(":") || str.equals(" ") || str.equals(",")) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * 删除缓存中旧的对象，再判断实体类是否满足key的要求，若满足则加入缓存
	 * @param key 缓存key
	 * @param value 实体对象
	 * @param list 缓存的结果
	 * @param i 要删除缓存在list中的位置
	 */
	public void updateQueryCache(String key, Object value, List list, int i) {
		String argString = null;
		int needCache;
		int addFlag = 0;
		
		/* 首先删除缓存中旧的对象 */
		list.remove(i);
		
		/* 如果key中存在参数列表，则解析出参数列表 */
		String[] keyString = key.split(":");
		if(keyString.length >= 3) {
			argString = keyString[2];
		}
		
		needCache = needAddCache(key, value, argString);
		/* value满足key的要求时，将value插入缓存中，无法解析QL语句时，删除缓存 */
		if(1 == needCache) {
			/* 如果全局配置为删除缓存方式，则删除缓存 */
			if(1 == cacheMethod) {
				cache.remove(key);
				return;
			}
			/* 如果全局配置为修改list方式，则修改list */
			else {
				list.add(i, value);
				addFlag = 1;
			}
		}else if(-1 == needCache) {
			cache.remove(key);
			return;
		}
		
		/* 如果删除旧对象后，新对象没有加到list里面，并且list里面有count时，更新一下count */
		/* list.size为0时，表示list里面没有count */
		if((0 == addFlag) && list.size() > 0 &&(list.get(list.size() - 1) instanceof Paging)) {
			Integer totalCount = getPagingCache(key);
			if(null != totalCount) {
				/* count减1，并且更新缓存 */
				totalCount--;
				putPagingCache(key, totalCount);
			}
		}
	}
	
	
	/**
	 * 添加实体类对象时对应分页缓存的处理
	 * @param key 缓存key
	 * @param value 要更新的实体对象
     * @param map 存放分页缓存的hashmap
	 */
	public void addHashMapCache(String key, Object value, ConcurrentHashMap<String, Object> map) {
		String argString = null;
		int needCache;
		List list = null;
		
		/* 从hashmap中取得第一页缓存 */
		Object o = (Object)map.get("1");
		if(o instanceof List) {
			list = (List)o;
		}
		
		if(null == list) {
			return;
		}
		
		/* 如果key中存在参数列表，则解析出参数列表 */
		String[] keyString = key.split(":");
		if(keyString.length >= 3) {
			argString = keyString[2];
		}
		
		/* 判断value是否满足key的要求，如果满足则添加到缓存中，如果不满足，则不添加，如果无法判断，则删除缓存 */
		needCache = needAddCache(key, value, argString);
		if(1 == needCache) {
			/* 如果全局配置为删除缓存方式，则删除缓存 */
			if(1 == cacheMethod) {
				cache.remove(key);
				return;
			}
			/* 如果全局配置为修改list方式，则修改list */
			else {
				synchronized (list) {
					/* 新增的缓存加到第一页最前面 */
					list.add(0, value);
					/* 添加缓存后需要更新一下count结果 */
					if(list.get(list.size() - 1) instanceof Paging) {
						Integer totalCount = getPagingCache(key);
						if(null != totalCount) {
							/* count加1，并且更新缓存 */
							totalCount++;
							putPagingCache(key, totalCount);
						}
					}
				}
			}
		}else if(-1 == needCache) {
			cache.remove(key);
		}
	}
	
	
	/**
	 * 修改实体类对象时对应分页缓存的处理
	 * @param key 缓存key
	 * @param value 要更新的实体对象
     * @param map 存放分页缓存的hashmap
	 */
	public void updateHashMapCache(String key, Object value, ConcurrentHashMap<String, Object> map) {
		int findFlag = 0;
		
		/* 遍历map中的所有分页缓存 */
		for(Serializable queryCacheKey : map.keySet()) {
			/* map中包含一个count结果，此结果不参与比较 */
			if(queryCacheKey.toString().equals("count")) {
				continue;
			}
			
			/* 剩下的结果都是list */
			List list = (List)map.get(queryCacheKey);
			if(null == list) {
				continue;
			}
			/* 考虑到并发操作，这里对list加锁 */
			synchronized (list) {
				for(int i = 0; i < list.size(); i++) {
		    		if(list.get(i).equals(value)) {
		    			findFlag = 1;
						/* 如果缓存中存在相同缓存，则更新缓存 */
						updateQueryCache(key, value, list, i);
						break;
		    		}
				}
			}
			
			/* 如果找到了相同的对象，为了性能考虑，剩下的list不进行查找 */
			if(1 == findFlag) {
				break;
			}
		}
		
		/* map中所有结果都查找后，都没有找到相同对象，则判断是否需要新增到缓存中 */
		if(0 == findFlag) {
			addHashMapCache(key, value, map);
		}
	}
	
	
	/**
	 * 删除实体类对象时对应分页缓存的处理
	 * @param key 缓存key
	 * @param value 要更新的实体对象
     * @param map 存放分页缓存的hashmap
	 */
	public void deleteHashMapCache(String key, Object value, ConcurrentHashMap<String, Object> map) {
		int findFlag = 0;
		
		/* 遍历map中的所有分页缓存 */
		for(Serializable queryCacheKey : map.keySet()) {
			/* map中包含一个count结果，此结果不参与比较 */
			if(queryCacheKey.toString().equals("count")) {
				continue;
			}
			
			List list = (List)map.get(queryCacheKey);
			if(null == list) {
				continue;
			}
			
			synchronized (list) {
				/* 对于每个分页缓存再去查找相同对象 */
				for(int i = 0; i < list.size(); i++) {
					
					//取出集合中实体的ID的值，与给定待处理实体的ID的值；
					Object rs=null,rstmp=null;
					try {
						Object tmp=list.get(i);
						//hyq 2014-07-07 如果不是相同的对象，则不需要比较,因分页时，最后有一个Paging对象						
						if(!tmp.getClass().getName().equals(value.getClass().getName()))
							break;
						
						Method[] methos=tmp.getClass().getMethods();
						
						for(Method method:methos){
							if(method.getAnnotation(Id.class)!=null){
								rs=method.invoke(tmp, null);
								rstmp=method.invoke(value, null);
								break;
							}
						}
					} catch (Exception e) {						
					}
					//如果彼配，则删除缓存对应的实体	
		    		if(rs.equals(rstmp)) {
		    			findFlag = 1;
		    			/* 如果全局配置为删除缓存方式，则删除缓存 */
		    			if(1 == cacheMethod) {
		    				cache.remove(key);
		    			}
		    			/* 如果全局配置为修改list方式，则修改list */
		    			else {
		    				/* 找到后直接删除 */
		    				list.remove(i);
		    				/* 删除缓存后需要更新一下count结果 */
		    				if(list.size() > 0 && list.get(list.size() - 1) instanceof Paging) {
		    					Integer totalCount = getPagingCache(key);
		    					if(null != totalCount) {
			    					totalCount--;
			    					putPagingCache(key, totalCount);
		    					}
		    				}
		    			}
		    			break;
		    		}
				}
			}
			
			if(1 == findFlag) {
				return;
			}
		}
	}
	
	
	/**
	 * 根据数据库操作类型cacheType来对缓存做对应操作
	 * @param key 缓存key
	 * @param value 要更新的实体对象
	 * @param cacheType 缓存操作类型
	 * @param map 存放分页缓存的hashmap
	 */
	public void putHashMapCache(String key, Object value, int cacheType, ConcurrentHashMap<String, Object> map) {
		switch(cacheType) {
		    /* 添加分页缓存 */
		    case CACHE_TYPE_ADD :
		    	addHashMapCache(key, value, map);
		    	break;
		    /* 修改分页缓存 */
		    case CACHE_TYPE_UPDATE :
		    	updateHashMapCache(key, value, map);
		    	break;
		    /* 删除分页缓存 */
		    case CACHE_TYPE_DELETE :
		    	deleteHashMapCache(key, value, map);
		    	break;
		    default :
		    	break;
		}
	}
	
	
	/**
	 * 添加实体类对象时对应缓存的处理
	 * @param key 缓存key
	 * @param value 要更新的实体对象
	 * @param list 缓存结果集
	 */
	public void addListCache(String key, Object value, List list) {
		String argString = null;
		int needCache;
		
		/* 如果key中存在参数列表，则解析出参数列表 */
		String[] keyString = key.split(":");
		if(keyString.length >= 3) {
			argString = keyString[2];
		}
		
		/* 判断value是否满足key的要求，如果满足则添加到缓存中*/
		needCache = needAddCache(key, value, argString);
		if(1 == needCache) {
			/* 如果全局配置为删除缓存方式，则删除缓存 */
			if(1 == cacheMethod) {
				cache.remove(key);
				return;
			}
			/* 如果全局配置为修改list方式，则修改list */
			else {
				synchronized (list) {
					/* 新增的缓存加到最前面 */
					list.add(0, value);
				}
			}
		}
		/* 如果无法判断value是否满足key的要求，则删除缓存 */
		else if(-1 == needCache) {
			cache.remove(key);
		}
	}
	
	
	/**
	 * 修改实体类对象时对应缓存的处理
	 * @param key 缓存key
	 * @param value 要更新的实体对象
	 * @param list 缓存结果集
	 */
	public void updateListCache(String key, Object value, List list) {
		int findFlag = 0;
		
		/* 考虑到并发操作，这里对list加锁 */
		synchronized (list) {
			/* 遍历list，查找相同缓存 */
			for(int i = 0; i < list.size(); i++) {
				if(list.get(i).equals(value)) {
	    			findFlag = 1;
					/* 如果缓存中存在相同缓存，则更新缓存 */
					updateQueryCache(key, value, list, i);
					break;
	    		}
			}
		}
		
		/* 如果没查找到相同缓存，则看value是否应该加入到缓存中 */
		if(0 == findFlag) {
			addListCache(key, value, list);
		}
	}
	
	
	/**
	 * 删除实体类对象时对应缓存的处理
	 * @param key 缓存key
	 * @param value 要更新的实体对象
	 * @param list 缓存结果集
	 */
	public void deleteListCache(String key, Object value, List list) {
		/* 考虑到并发操作，这里对list加锁 */
		synchronized (list) {
			/* 遍历list，找到相同缓存则直接删除 */
			
			for(int i = 0; i < list.size(); i++) {
				//取出集合中实体的ID的值，与给定待处理实体的ID的值；
				Object rs=null,rstmp=null;
				try {
					Object tmp=list.get(i);
					Method[] methos=tmp.getClass().getMethods();
					
					for(Method method:methos){
						if(method.getAnnotation(Id.class)!=null){
							rs=method.invoke(tmp, null);
							rstmp=method.invoke(value, null);
							break;
						}
					}
				} catch (Exception e) {
					
				}
				/* equals方法需要在每个类里面重新实现 */
				if(rs!=null && rs.equals(rstmp)) {
					/* 如果全局配置为删除缓存方式，则删除缓存 */
					if(1 == cacheMethod) {
						cache.remove(key);
					}
					/* 如果全局配置为修改list方式，则修改list */
					else {
						list.remove(i);
					}
					break;
	    		}
			}
		}
	}
	
	
	/**
	 * 根据数据库操作类型cacheType来对缓存做对应操作
	 * @param key 缓存key
	 * @param value 要更新的实体对象
	 * @param cacheType 缓存操作类型
	 * @param list 缓存结果集
	 */
	public void putListCache(String key, Object value, int cacheType, List list) {
		switch(cacheType) {
		    /* 添加非分页缓存 */
		    case CACHE_TYPE_ADD :
		    	addListCache(key, value, list);
		    	break;
		    /* 修改非分页缓存 */
		    case CACHE_TYPE_UPDATE :
		    	updateListCache(key, value, list);
		    	break;
		    /* 删除非分页缓存 */
		    case CACHE_TYPE_DELETE :
		    	deleteListCache(key, value, list);
		    	break;
		    default :
		    	break;
		}
	}
	
	
	
	/**
	 * 实体类更新时，同步更新实体类相关的查询缓存内容
	 * @param entityCacheKey 发生更新的实体对象在缓存中的KEY值；
	 * @param value 要更新的实体对象
	 * @param cacheType 缓存操作类型
	 */
	public void putQueryCache(Serializable entityCacheKey, Object value, int cacheType) {
		String keyStr=entityCacheKey.toString();
		String className=keyStr.substring(keyStr.indexOf(":")+1,keyStr.lastIndexOf(":"));
		
		/* 遍历所有缓存，找到需要更新的缓存 */
		for(Serializable queryCacheKey : cache.keySet()) {
			String key=queryCacheKey.toString();
			/* 判断缓存是否是查询缓存并且是否是className的缓存 */
			if(key.startsWith("QUERY:") && containsSameClass(key, className)) {
				Object o = (Object) (((NewsmyApplicationCache)cache.get(queryCacheKey)).getVaule());
				if(null == o) {
					continue;
				}
				
				/* 根据key取出来的结果有两种，
				 * 一种是数据库查询结果list，一种是hashmap，hashmap中以paging为key再存放list
				 * 所以这里要分别处理
				 */
				if(o instanceof List) {
					putListCache(key, value, cacheType, (List)o);
				}else if(o instanceof ConcurrentHashMap) {
					putHashMapCache(key, value, cacheType, (ConcurrentHashMap<String, Object>)o);
				}
			}else if(keyStr.equals(key)){
				//hyq新加处理：如果当前缓存是实体对象，则更新该对象
				((NewsmyApplicationCache)cache.get(key)).setValue(value);
				((NewsmyApplicationCache)cache.get(key)).setLastUseTime(new Date());
			}
		}
	}


	@Override
	public void remove(Serializable key) {
		;		
	}


	@Override
	public void put(Serializable key, Object value) {
		;
	}
	
	@Override
	public void put(Serializable key, Object value, long timeLength) {
		// TODO Auto-generated method stub		
	}
}
