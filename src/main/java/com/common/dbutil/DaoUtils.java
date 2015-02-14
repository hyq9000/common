package com.common.dbutil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务于DAO实现类，提供公共的逻辑实现
 * @author yuing
 * </br>
 * date 2014-0-06
 *
 */
class DaoUtils {
	
	/**
	 * 将实际类型是String型，而值内容是相关的基本类型值，转换成对应的基本类型，如“101",转成整型；
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	protected static Object stringToTypes(String fieldName, Object value,Class clazz)
			throws Exception {
		if(fieldName!=null || value!=null){	
			Field field=clazz.getDeclaredField(fieldName);
			Class type=field.getType();		
			try {
				value=type.cast(value);
			} catch (ClassCastException e) {
				if(type.equals(Integer.class) ||type.equals(int.class)  ){
					value=Integer.parseInt(value.toString() );
				}else if(type.equals(Double.class)||type.equals(double.class)){
					value=Integer.parseInt(value.toString());
				}else if(type.equals(Short.class)||type.equals(short.class)){
					value=Short.parseShort(value.toString());
				}else if(type.equals(Long.class)||type.equals(long.class)){
					value=Long.parseLong(value.toString());
				}else if(type.equals(Float.class)||type.equals(float.class)){
					value=Float.parseFloat(value.toString());
				}else if(type.equals(Byte.class)||type.equals(byte.class)){
					value=Byte.parseByte(value.toString());
				}else if(type.equals(BigDecimal.class)){
					value=new BigDecimal(value.toString());
				}else if(type.equals(Boolean.class)||type.equals(boolean.class)){
					value=Boolean.parseBoolean(value.toString());
				}else if(type.equals(Date.class) ){
					try {
						value=DateFormat.getInstance().parse(value.toString());
					} catch (ParseException e1) {
						throw new Exception("日期格式不正确！");
					}
				}
			}
		}
		return value;
	}
	
	/**
	 * 将带as的sql语句中的别名取出;因为hibernate执行原生SQL时，如果SQL中存在"字段名 as 别名"时会报错误，
	 * 原因是HQL要求在明确指定其别名对象，方可执行;
	 * @param sql 
	 * @return
	 */
	protected static  List<String> getAliasName(String sql){
		String[] sqltmp=sql.split(" {1,}|,");//按空格将SQL拆成单词;
		List<String> alias=new ArrayList<String>();
		int pos=0;
		for(int i=pos;sqltmp[pos].compareToIgnoreCase("FROM")!=0&&i<sqltmp.length;i++){
			if(sqltmp[i].compareToIgnoreCase("as")==0){
				alias.add(sqltmp[i+1].trim());
				pos=i+2;
			}
		}
		return alias;
	}
	
	/**
	 * 根据整型代号返回特定的HQL条件操作符
	 * @param flag
	 * @return
	 */
	protected static String getOperatedFlagString(int flag){
		switch(flag){
			case 0:return " = ";
			case 1:return " > ";
			case 2:return " < ";
			case 10:return " >= ";
			case 20:return " <= ";
			case 3:return " like ";
			case 4:return " != ";
			default:return " = ";
		}
	}
	
	/**
	 * 根据整型代号返回特定的HQL条件操作符
	 * @param flag
	 * @return
	 */
	protected static  String getConditionFlagString(int flag){
		switch(flag){
			case 0:return " AND ";
			case 1:return " OR ";			
			default:return " AND ";
		}
	}
	
	/**
	 * 找到sql最外层查询的“FROM"的下标位置,并统计该下标前的”？“个数，将参数列表中对的参数删除；
	 * 查询出离select 最近的，且没有被()起来的第一个from的下标位置,	 * 
	 * @return
	 */
	 protected static int getFirstFrom(String sql){	
		 /*
		  * 算法说明：
		  * 先计算出SQL的”FROM“单词的个数N，如果N为1，则第一个from就是要找的，否则按序从头遍历到FROM止；有几个from则遍历几次：每次遍历如下操作：		  * 		
		  * 	1,从头开始遍历SQL的每个字符,去识别"("和")"字符，见"(",计数器增1，见“)"则减1,直到当前的"FROM"位置;
		  *     2,当计数器为0时，则说明当前的“FROM"就是要找的FROM,否则计算器清零， 继续下一次遍历；
		  */
		int flag=0;
		String[] sqls=sql.split("FROM");
		if(sqls.length==1){
			return sql.indexOf("FROM");
		}
		int fromIndex=0;//记录查询过程中的from的位置;
		int leftIndex=0;//”("的位置
		for(int count=0;count<sqls.length-1;count++){
			 fromIndex=sql.indexOf("FROM",fromIndex+1);	
			 char[] sql_array=sql.toCharArray();
			 flag=0;
			for(int i=0;i<fromIndex;i++){
				if(sql_array[i]=='('){
					flag++;
					continue;
				}else if (sql_array[i]==')'){
					flag--;
					continue;
				}
			}
			if(flag==0){
				return fromIndex;	
			}
		}
		return 0;
	}
	
	/**
	 * 统计给定SQL最外层查询的”From“之前”？“的个数，并删除参数列表中对应的位置的参数；
	 * @param sql 待解析的SQL
	 * @param firstFromIndex 第一个From的下标;也就是通过getFirstFrom（）方法返回值
	 * @return
	 */
	 protected static  Object[]  getCountFrontOfFirstForm(String sql,int firstFromIndex,Object[] params) {
		 char[] sql_array=sql.toCharArray();
		 int count=0;
		 //统计在第一个FROM前的？的数量
		for(int i=0;i<firstFromIndex;i++){
			if(sql_array[i]=='?'){
				count++;
			}
		}
		if(count>0){
			int length=params.length-count;//参数列表删除后的长度;
			Object[] newParams=new Object[length];
			System.arraycopy(params, count, newParams, 0, length);
			return newParams;
		}else
			return params;
	}
}
