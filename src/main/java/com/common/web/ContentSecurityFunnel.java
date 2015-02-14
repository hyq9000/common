package com.common.web;

/**
 * 将页面提交内容进行安全处理的漏斗，将提交内容中含用非法html
 * ,css,javascript，sql脚本转换成合法内容
 * <br/>时间：2012-11-19
 * @author yuqing
 */
public class ContentSecurityFunnel {
	/**
	 * 将内容中的<,",',>换成对应的实体名称；
	 * @param content 页面提交过来的文本内容；
	 * @return 返回转换后的字符串
	 */
	public  static String getSecurityString(String content) throws NullPointerException{
			return  content.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
					.replaceAll("\"", "&quot;").replaceAll("'", "&#39;");
	}
	
	public static void main(String[] args){
		String tmp="str中国人发<script>alert('')</script>";
		System.out.println(getSecurityString(tmp));
	}
}
