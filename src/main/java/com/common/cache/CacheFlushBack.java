package com.common.cache;

/**
 * 类型描述:定义将缓存中数据同步(刷回)到硬盘;
 * </br>创建时期: 2015年2月24日
 * @author hyq
 */
public interface CacheFlushBack {
	/**
	 * 将缓存中有更新的数据项,同步更新到数据库
	 * @return 成功更新的条数:要不就是0条,要不就是有更新的数据数;
	 */
	int flushToDB();
}
