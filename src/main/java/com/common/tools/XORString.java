package com.common.tools;

import java.io.UnsupportedEncodingException;


/**
 * 各集成系统之间传递参数加解密实现； <br/>
 * 创建时间：2012-11-08
 * 
 * @author zhangfan
 */
public class XORString {
	/* 加密的key，各个系统之间key要统一 */
	public final static int KEY = 1;
	
	/**
	 * 对字符串进行加密；
	 * @param initialString 要加密的字符串
	 * @return 加密字符串为空或者""时，返回null
	 * @return 返回加密过后的字符串
	 */
    public static String encryption(String initialString) {
    	int i;
    	
    	if(null == initialString || initialString.equals("")) {
    		return null;
    	}
    	
    	/* string转成char数组 */
    	char[] str = initialString.toCharArray();
		for(i = 0; i < str.length; i++) {
			/* 有些特殊字符不能加密 */
			if('!' == str[i] || ' ' == str[i] || '\'' == str[i] || '&' == str[i] 
			    || '\"' == str[i] || '#' == str[i] || '\\' == str[i] || ']' == str[i]){
				continue;
			}
			
			/* 异或运算 */
			str[i] = (char)(str[i]^KEY);
		}
		
		/* char数组再转化为string */
		return new String(str);
    }
    
	/**
	 * 对字符串进行解密；
	 * @param encryptionString 要解密的字符串
	 * @return 解密字符串为空或者""时，返回null
	 * @return 返回解密过后的字符串
	 */
    public static String deciphering(String encryptionString) {
    	return encryption(encryptionString);
    }
    
    
	/**
	 * 在url中，根据参数名提取参数的值
	 * @param url 原始url
	 * @param param 要提取的参数名
	 * @return url中不存在参数或者param不合法时返回空
	 * @return 正常返回要取参数的值
	 */
    public static String getParams(String url, String param){
    	int index;
    	
    	if(null == url || null == param) {
    		return null;
    	}
    	
    	/* 要提取的参数名不能包含"&" */
    	if(-1 != param.indexOf("&")){
    		return null;
    	}
    	
    	/* 获得url中参数名的起始位置 */
    	index = url.indexOf(param+"=");
    	if(-1 == index) {
    		return null;
    	}
    	
    	url = url.substring(index);
    	index = url.indexOf("&");
    	if(-1 == index) {
    		/* 从起始位置取到url最后 */
    		return url.substring((param+"=").length());
    	}else {
    		/* 从起始位置取到下一个"&" */
    		return url.substring((param+"=").length(), index);
    	}
    }
    
    
    public static void main(String[] args) {
    	//String testString="http://192.168.3.142/TBH/ajax/UserHandler.ashx?checkFlag=1&userInfo=%7B%22accountIntergral%22%3A6%2C%22accountIsForbid%22%3A0%2C%22accountIsOnline%22%3A1%2C%22accountIsVenify%22%3A1%2C%22accountLoginIp%22%3A%22192.168.3.142%22%2C%22accountLoginName%22%3A%22368413156%40qq.com%22%2C%22accountLoginTime%22%3A%222012-10-23T17%3A34%3A25%22%2C%22accountMail%22%3A%22368413156%40qq.com%22%2C%22accountPassword%22%3A%2296e79218965eb72c92a549dd5a330112%22%2C%22accountProducts%22%3A%2246%3AK97%22%2C%22accountRandomStr%22%3Anull%2C%22accountUserName%22%3A%22%E6%97%A9%E4%B8%8A%E5%A5%BD%E5%A4%BA%22%7D";
    	String testString = "\'";
    	System.out.println(testString);
    	//System.out.println(testString.length());
    	String testString1 = encryption(testString);
    	System.out.println(testString1);
    	//System.out.println(testString1.length());
    	String testString2 = deciphering(testString1);
    	System.out.println(testString2);
    	
    	//System.out.println(getParams(testString, "checkFlag"));
    	
    	//System.out.println(testString);
    }
}
