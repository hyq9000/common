package com.common.utils;

/**
 * 提供短信发送相关的接口
 * @author leihao,hyq
 * 创建时间:2014-04-05
 */
public interface ShortMessageService {
	/**
	 * 发送短信到目标手机号
	 * @param phoneNumber 目标手机号
	 * @param messageContent 短信内容
	 * @return 发送短信成功之后返回字符串"发送成功",否则则返回字符串"发送失败"
	 */
	public String  sendMessage(String phoneNumber,String messageContent);
	
	/**
	 * 群发短信
	 * @param phoneNumbers 群发号码组
	 * @param messageContext 群发内容
	 * @return 群发短信成功之后返回字符串"发送成功",否则则返回字符串"发送失败"
	 */
	public String sendMessage(String[] phoneNumbers,String messageContext);
	
	/**
	 * 根据给定的错误码，返回出错误说明文本
	 * @param errorCode 错误码,一般是发送消息方法返回的错误码
	 * @return 返回对应错误码的说明文本，没有对应上，则返回"错误码不正确"
	 */
	public String errorCodeToText(int errorCode);
}
