package com.common.tools.syncAccountInfo;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 平板电脑账号表同步到云服务平台账号表实现<br/>
 * 创建时间：2012-6-18<br/>
 * @author zhangfan
 */
public class SyncAccountInfo {
	/* 云服务平台账号表用varchar表示性别，平板电脑账号表用int表示性别，以下定义用于性别字段的转化 */
	/* 云服务平台性别宏定义 */
	public static final String SEX_MAN_STRING = "男";
	public static final String SEX_WOMAN_STRING = "女";
	/* 平板电脑性别宏定义 */
	public static final int SEX_MAN_INT = 1;
	public static final int SEX_WOMAN_INT = 0;

	/* 事务状态宏定义 */
	public static final int TRAMSACTION_NOT_START = 2;
	public static final int TRAMSACTION_COMMIT = 1;
	public static final int TRAMSACTION_ROLLBACK = 0;


	/**
	 * 将平板电脑mssql数据库某一具体数据插入到云服务平台mysql数据库中 <br/>
	 * 创建时间：2012-6-19 <br/>
	 * @param mssqlCon 平板电脑mssql数据库连接<br/>
	 * @param mysqlCon 云服务平台mysql数据库连接<br/>
	 * @param mssqlResult 指向平板电脑mssql数据库某一具体数据的结果集<br/>
	 * @exception 如果数据库操作失败，则抛出异常
	 */

	public static void insetIntomysql(Connection mssqlCon, Connection mysqlCon,
			ResultSet mssqlResult) throws SQLException {
		String mail;
		String name;
		String passWord;
		String loginTime;
		String regTime;
		String loginIP;
		String guid;
		String sex;
		String Tel;
		String country;
		String province;
		String city;
		String addr;
		String cardNum;
		String question;
		String answer;
		String birthday;
		String ID;
		int forbid;
		int venify;
		int sexInt;
		int intergral;
		int upNum;
		int downNum;

		String sqlString;
		/* 初始值为未开启事务 */
		int transactionFlag = TRAMSACTION_NOT_START;

		PreparedStatement mysqlStatement = null;
		PreparedStatement mssqlStatement = null;
		ResultSet mssqlResultTmp = null;
		try {
			/* 从mssql数据库的AccInfo表中取出对应数据，插入到mysql数据库的CCORE_ACCOUNT_INFO表中 */
			ID = mssqlResult.getString("strUserId");
			mail = mssqlResult.getString("strMail");
			/* 安卓新云密码没有经过md5加密，同步时需要加密存入到云服务 */
			passWord = DigestUtils.md5Hex(mssqlResult.getString("strPass"));
			loginTime = mssqlResult.getString("dlastLogTime");
			intergral = mssqlResult.getInt("strInteg");
			loginIP = mssqlResult.getString("srlastIP");
			guid = mssqlResult.getString("strGuid");
			regTime = mssqlResult.getString("strRegTime");
			forbid = mssqlResult.getInt("isForbid");
			venify = mssqlResult.getInt("isVerifyMail");
			upNum = mssqlResult.getInt("nupNum");
			downNum = mssqlResult.getInt("ndownNum");
			/* 从mssql数据库的UserInfo表中取出对应数据，插入到mysql数据库的CCORE_USER_BASIC_INFO表中 */
			sqlString = "select * from UserInfo where strBsGuid=?";
			mssqlStatement = mssqlCon.prepareStatement(sqlString);
			mssqlStatement.setString(1, guid);
			mssqlResultTmp = mssqlStatement.executeQuery();
			/* 当查询不到数据时，mssql数据库表数据不对称，此条同步的数据有问题，不能同步 */
			if (false == mssqlResultTmp.next()) {
				return;
			}

			name = mssqlResultTmp.getString("strName");
			Tel = mssqlResultTmp.getString("strTel");
			country = mssqlResultTmp.getString("strCoun");
			province = mssqlResultTmp.getString("strProvin");
			city = mssqlResultTmp.getString("strCity");
			addr = mssqlResultTmp.getString("strMailAddr");
			cardNum = mssqlResultTmp.getString("strCardNum");
			question = mssqlResultTmp.getString("strSecuQ");
			answer = mssqlResultTmp.getString("strSecuAnsw");
			birthday = mssqlResultTmp.getString("dbirthday");
			if(null == birthday || birthday.equals("")) {
				Date date=new Date(); 
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
				birthday = df.format(date); 
			}
			
			sexInt = mssqlResultTmp.getInt("sex");
			/* mssql和mysql对于性别字段的定义不一样，需要转化一下 */
			if (SEX_MAN_INT == sexInt) {
				sex = SEX_MAN_STRING;
			} else {
				sex = SEX_WOMAN_STRING;
			}

			mssqlResultTmp.close();
			mssqlResultTmp = null;
			mssqlStatement.close();
			mssqlStatement = null;

			/* 由于一次同步需要向mysql数据库插入两条数据，此处开启事务，防止插入数据不对称 */
			mysqlCon.setAutoCommit(false);
			/* 事务开启成功标记 */
			transactionFlag = TRAMSACTION_COMMIT;

			/* 向CCORE_ACCOUNT_INFO插入同步数据 */
			sqlString = "insert into CCORE_ACCOUNT_INFO (ACCOUNT_LOGIN_NAME, ACCOUNT_MAIL, ACCOUNT_USER_NAME, ACCOUNT_PASSWORD, "
					+ "ACCOUNT_LOGIN_TIME, ACCOUNT_LOGIN_IP, ACCOUNT_INTERGRAL, ACCOUNT_IS_FORBID, ACCOUNT_IS_VENIFY, ACCOUNT_IS_ONLINE, ACCOUNT_PRODUCTS, ACCOUNT_UPLOAD, ACCOUNT_DOWNLOAD) "
					+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, '', ?, ?)";
			mysqlStatement = mysqlCon.prepareStatement(sqlString);
			mysqlStatement.setString(1, ID);
			mysqlStatement.setString(2, mail);
			mysqlStatement.setString(3, name);
			mysqlStatement.setString(4, passWord);
			mysqlStatement.setString(5, loginTime);
			mysqlStatement.setString(6, loginIP);
			mysqlStatement.setInt(7, intergral);
			mysqlStatement.setInt(8, forbid);
			mysqlStatement.setInt(9, venify);
			mysqlStatement.setInt(10, upNum);
			mysqlStatement.setInt(11, downNum);

			mysqlStatement.executeUpdate();
			mysqlStatement.close();
			mssqlStatement = null;

			/* 向CCORE_USER_BASIC_INFO插入同步数据 */
			sqlString = "insert into CCORE_USER_BASIC_INFO (USER_ID, USER_SEX, USER_REGTIME, USER_NAME, USER_TEL, USER_BIRTHDAY, "
					+ "USER_COUNTRY, USER_PROVINCE, USER_CITY, USER_ADDR, USER_CARD_NUM, USER_SECURE_QUESTION, USER_SECURE_ANSWER) "
					+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			mysqlStatement = mysqlCon.prepareStatement(sqlString);
			mysqlStatement.setString(1, ID);
			mysqlStatement.setString(2, sex);
			mysqlStatement.setString(3, regTime);
			mysqlStatement.setString(4, name);
			mysqlStatement.setString(5, Tel);
			mysqlStatement.setString(6, birthday);
			mysqlStatement.setString(7, country);
			mysqlStatement.setString(8, province);
			mysqlStatement.setString(9, city);
			mysqlStatement.setString(10, addr);
			mysqlStatement.setString(11, cardNum);
			mysqlStatement.setString(12, question);
			mysqlStatement.setString(13, answer);

			mysqlStatement.executeUpdate();
			mysqlStatement.close();
			mysqlStatement = null;

		} catch (SQLException ex) {
			transactionFlag = TRAMSACTION_ROLLBACK;
			System.err.println("SQLException: " + ex.getMessage());
		} finally {
			if (TRAMSACTION_COMMIT == transactionFlag) {
				/* 没有出错，则提交事务 */
				mysqlCon.commit();
			} else if (TRAMSACTION_ROLLBACK == transactionFlag) {
				/* 如果出错，则回滚事务 */
				mysqlCon.rollback();
			}
            
			/* 释放结果集，数据库连接等资源 */
			if (null != mssqlResultTmp) {
				mssqlResultTmp.close();
			}

			if (null != mssqlStatement) {
				mssqlStatement.close();
			}

			if (null != mysqlStatement) {
				mysqlStatement.close();
			}
		}

	}

	/**
	 * 实现了平板电脑mssql数据库差异数据同步到云服务平台mysql数据库 <br/>
	 * 创建时间：2012-6-18 <br/>
	 * @param mysqlUrl 云服务平台mysql数据库url<br/>
	 * @param mysqlUsername 云服务平台mysql数据库用户名<br/>
	 * @param mysqlPassword 云服务平台mysql数据库密码<br/>
	 * @param mssqlUrl 平板电脑mssql数据库url<br/>
	 * @param mssqlUsername 平板电脑mssql数据库用户名<br/>
	 * @param mssqlPassword 平板电脑mssql数据库密码<br/>
	 * @exception 如果数据库操作失败，则抛出异常
	 */
	public static void syncAccountInfo(String mysqlUrl, String mysqlUsername,
			String mysqlPassword, String mssqlUrl, String mssqlUsername,
			String mssqlPassword) throws SQLException, ClassNotFoundException {
		String userID;
		String sqlString;
		int count;

		PreparedStatement mysqlStatement = null;
		PreparedStatement mssqlStatement = null;
		Connection mysqlCon = null;
		Connection mssqlCon = null;
		ResultSet mssqlResult = null;
		ResultSet mysqlResult = null;

		try {
			/* 加载mysql jdbc驱动 */
			Class.forName("com.mysql.jdbc.Driver");
			mysqlCon = DriverManager.getConnection(mysqlUrl, mysqlUsername,
					mysqlPassword);
			/* 加载mssql jdbc驱动 */
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			mssqlCon = DriverManager.getConnection(mssqlUrl, mssqlUsername,
					mssqlPassword);

			mssqlStatement = mssqlCon.prepareStatement("select * from AccInfo");
			mssqlResult = mssqlStatement.executeQuery();
			while (mssqlResult.next()) {
				userID = mssqlResult.getString("strUserId");
				/* mssql数据库strMail为空时，表示此数据有问题，不能同步 */
				if (null == userID || userID.equals("")) {
					continue;
				}

				/* 根据ACCOUNT_LOGIN_NAME查询mysql数据库中，是否已经有了对应的账号信息 */
				sqlString = "select count(*) as count from CCORE_ACCOUNT_INFO where ACCOUNT_LOGIN_NAME=?";
				mysqlStatement = mysqlCon.prepareStatement(sqlString);
				mysqlStatement.setString(1, userID);

				mysqlResult = mysqlStatement.executeQuery();
				mysqlResult.next();
				count = mysqlResult.getInt("count");

				mysqlResult.close();
				mysqlResult = null;
				mysqlStatement.close();
				mysqlStatement = null;

				/* 如果有对应的账号信息，则查找下一条数据，如果没有，则同步此信息到mysql数据库中 */
				if (0 != count) {
					continue;
				} else {
					insetIntomysql(mssqlCon, mysqlCon, mssqlResult);
				}
			}

		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException");
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		} finally {
			/* 最后将结果集，数据库连接等资源释放 */
			if (null != mssqlResult) {
				mssqlResult.close();
			}
			if (null != mssqlStatement) {
				mssqlStatement.close();
			}
			if (null != mssqlCon) {
				mssqlCon.close();
			}

			if (null != mysqlResult) {
				mysqlResult.close();
			}
			if (null != mysqlStatement) {
				mysqlStatement.close();
			}
			if (null != mysqlCon) {
				mysqlCon.close();
			}
		}
	}

	public static void main(String args[]) {
		try {
			/* 以下参数是根据我的测试环境配置的 */
			syncAccountInfo("jdbc:mysql://192.168.3.140:3306/cloud_db", "root", "root",
					"jdbc:sqlserver://192.168.3.53:1433;DatabaseName=Product_230_1026", "sa", "sa123456");

		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException");
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		}
	}
}
