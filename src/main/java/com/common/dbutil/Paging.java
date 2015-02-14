package com.common.dbutil;

import java.io.Serializable;

/**
 * 封装分页所需数据的类 <br/>
 * 创建时间：2012-7-3
 * @author yuqing
 */
public class Paging implements Serializable{
	
	private static final long serialVersionUID = -7190987503523083333L;
	/* 每页行数默认为10 */
	private int pageSize = 10;
	/* 页号默认为1 */
	private int pageNo = 1;
	/* 总共数据条数默认为50 */
	private int totalCount = 50;
	/* 是否需要底层生成总成数据条数 
	 * 2014-05-17 HYQ
	 * 解决这样的问题：那些复杂的SQL,DBUTIL无法为之计算总行数时，用户可以定义该值为false,
	 * 以告知DBUTIL无需为之计算总行数
	 */
	private boolean generateTotalCount=true;
	
	

	/**
	 * @deprecated 建议采用Paging(int pageSize, int pageNo)来构造；
	 * @param pageSize 每页行数
	 * @param pageNo 当前页号，从1开始；
	 * @param totalCount 大约有多少行数据；这个值可以先给出，而后由DAO计算赋值；
	 */
	public Paging(int pageSize, int pageNo, int totalCount) {
		super();
		this.pageSize = pageSize;
		this.pageNo = pageNo;
		this.totalCount = totalCount;
	}
	
	/**	
	 * @param pageSize 每页行数
	 * @param pageNo 当前页号，从1开始；
	 */
	public Paging(int pageSize, int pageNo) {
		super();
		this.pageSize = pageSize;
		this.pageNo = pageNo;
	}
	
	/**	
	 * @param pageSize 每页行数
	 * @param pageNo 当前页号，从1开始；
	 * @param generateTotalCount 是否需要自动查询出总行数;
	 */
	public Paging(int pageSize, int pageNo,boolean generateTotalCount) {
		super();
		this.pageSize = pageSize;
		this.pageNo = pageNo;
		this.generateTotalCount =generateTotalCount;
	}
	
	/**
	 * @deprecated 此方法不再建议使用,用 getPageSize()
	 * @return the size
	 */
	public int getpageSize() {
		return pageSize;
	}
	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isGenerateTotalCount() {
		return generateTotalCount;
	}

	public void setGenerateTotalCount(boolean generateTotalCount) {
		this.generateTotalCount = generateTotalCount;
	}
}
