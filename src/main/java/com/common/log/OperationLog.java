package com.common.log;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CcoreOperationLog entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "cloud_operation_log")
public class OperationLog implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4262992130684694329L;
	// Fields
	private Integer logId;
	private String logIP;
	private String logUser;
	private String logContent;
	private Timestamp logTime;

	/**
	 * 重定义equals函数
	 */
	public boolean equals(Object obj) {
		/* obj是OperationLog对象时比较ID */
		if(obj instanceof OperationLog)
			return this.logId.equals(((OperationLog)obj).logId);
		/* obj不是OperationLog对象时，用原来的equals比较 */
		else
			return super.equals(obj);
	}
	
	// Constructors

	/** default constructor */
	public OperationLog() {
	}

	/** minimal constructor */
	public OperationLog(Integer logId) {
		this.logId = logId;
	}

	/** full constructor */
	public OperationLog(Integer logId, String logIP,
			String logUser, String logContent, Timestamp logTime) {
		this.logId = logId;
		this.logIP = logIP;
		this.logUser = logUser;
		this.logContent = logContent;
		this.logTime = logTime;
	}

	// Property accessors
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "LOG_ID", unique = true, nullable = false)
	public Integer getLogId() {
		return this.logId;
	}

	public void setLogId(Integer logId) {
		this.logId = logId;
	}

	@Column(name = "LOG_IP", length = 45)
	public String getLogIP() {
		return this.logIP;
	}

	public void setLogIP(String logIP) {
		this.logIP = logIP;
	}

	@Column(name = "LOG_TIME", length = 19)
	public Timestamp getLogTime() {
		return this.logTime;
	}
	
	public void setLogTime(Timestamp logTime) {
		this.logTime = logTime;
	}

	@Column(name = "LOG_USER", length = 20)
	public String getLogUser() {
		return this.logUser;
	}
	
	public void setLogUser(String logUser) {
		this.logUser = logUser;
	}
	
	@Column(name = "LOG_CONTENT", length = 254)
	public String getLogContent() {
		return this.logContent;
	}
	
	public void setLogContent(String logContent) {
		this.logContent = logContent;
	}
	
}
