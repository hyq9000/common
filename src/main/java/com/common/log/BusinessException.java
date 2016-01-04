package com.common.log;

/**
 * 类型描述:一个业务异常类,所有非系统的异常、需要捕获并业务处理的异常,都应用此类;
 * </br>创建时期: 2015年12月22日
 * @author hyq
 */
public class BusinessException extends RuntimeException {

	private int error;
	/**
	 * @param message 业务可理解的异常消息串
	 */
	public BusinessException(String message,int error){
		super(message);
		this.error=error;
	}
	
	public int getError(){
		return this.error;
	}
	
	/**
	 * 该方法用于子类重写，用于自定义异常的处理逻辑
	 * @param target 你要用的业务对象,可能是一个service，也可能是任何你要用的对象
	 */
	public void handle(Object target){}
	
	
}
