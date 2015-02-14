package com.common.utils;

/**
 * 类型描述:定义一个业务异常的类,用区分系统异常及业务异常 
 * </br>修改日期: 2014年12月17日
 * @author hyq
 */
public class BusinessException extends RuntimeException {
	private int exceptionCode=0;
	
	/**
	 * 业务逻辑异常码
	 * @return
	 */
	public int getExceptionCode() {
		return exceptionCode;
	}

	/**
	 *
	 * @param message  业务逻辑异常文本
	 * @param exceptionCode 业务逻辑异常码
	 */
	public BusinessException(String message,int exceptionCode) {
		super(message);
		this.exceptionCode=exceptionCode;
	}
	
	
	/**
	 * @param message  业务逻辑异常文本
	 */
	public BusinessException(String message) {
		super(message);
		this.exceptionCode=-10;
	}
}
