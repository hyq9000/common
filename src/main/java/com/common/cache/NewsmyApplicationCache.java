package com.common.cache;

import java.util.Date;


/**
 * 用以封装待缓存实体的数据类；
 */
public class NewsmyApplicationCache implements java.io.Serializable {	
	// Fields
	private Date lastUseTime;//最近使用时间戳，用记录被数据在缓存中的活跃时，以便缓存机制自动清理无用数据；
	private Object value;//被缓存实体对象；
	
	// Constructors
	
	/** default constructor */
	public NewsmyApplicationCache() {
	}
	
	/** minimal constructor */
	public NewsmyApplicationCache(Date lastUseTime) {
		this.lastUseTime = lastUseTime;
	}
	
	/** full constructor */
	public NewsmyApplicationCache(Date lastUseTime, Object value) {
		this.lastUseTime = lastUseTime;
		this.value = value;
	}
	
	public Date getLastUseTime() {
		return this.lastUseTime;
	}
	
	public void setLastUseTime(Date lastUseTime) {
		this.lastUseTime = lastUseTime;
	}
	
	public Object getVaule() {
		return this.value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
}
