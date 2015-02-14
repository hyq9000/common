package com.common.tools.syncAccountInfo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SyncAfterSales {
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
	public static void insetIntoCloud(Connection cloudCon, ResultSet afterSalesResult) throws SQLException {
		String mail;
		String passWord;
		String username;
		//String regip;
		String randomStr;
		int regDate;

		String sqlString;
		/* 初始值为未开启事务 */
		int transactionFlag = TRAMSACTION_NOT_START;

		PreparedStatement mysqlStatement = null;
		try {
			/* 从mssql数据库的AccInfo表中取出对应数据，插入到mysql数据库的CCORE_ACCOUNT_INFO表中 */
			username = afterSalesResult.getString("username");
			passWord = afterSalesResult.getString("password");
			mail = afterSalesResult.getString("email");
			//regip = afterSalesResult.getString("regip");
			
			regDate = afterSalesResult.getInt("regdate");
			String stringDate = String.valueOf(regDate);
			stringDate = stringDate + "000";
			Date date = new Date(Long.parseLong(stringDate));
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
			stringDate = sdf.format(date);  
			
			randomStr = afterSalesResult.getString("salt");
			
			/* 由于一次同步需要向mysql数据库插入两条数据，此处开启事务，防止插入数据不对称 */
			cloudCon.setAutoCommit(false);
			/* 事务开启成功标记 */
			transactionFlag = TRAMSACTION_COMMIT;

			/* 向CCORE_ACCOUNT_INFO插入同步数据 */
			sqlString = "insert into CCORE_ACCOUNT_INFO (ACCOUNT_LOGIN_NAME, ACCOUNT_MAIL, ACCOUNT_PASSWORD, "
					+ "ACCOUNT_RANDOM_STR, ACCOUNT_IS_FORBID, ACCOUNT_IS_VENIFY, ACCOUNT_IS_ONLINE) "
					+ "values (?, ?, ?, ?, 0, 0, 0)";
			mysqlStatement = cloudCon.prepareStatement(sqlString);
			mysqlStatement.setString(1, username);
			mysqlStatement.setString(2, mail);
			mysqlStatement.setString(3, passWord);
			mysqlStatement.setString(4, randomStr);

			mysqlStatement.executeUpdate();
			mysqlStatement.close();
			mysqlStatement = null;

			/* 向CCORE_USER_BASIC_INFO插入同步数据 */
			sqlString = "insert into CCORE_USER_BASIC_INFO (USER_ID, USER_REGTIME)"
					+ "values (?, ?)";
			mysqlStatement = cloudCon.prepareStatement(sqlString);
			mysqlStatement.setString(1, username);
			mysqlStatement.setString(2, stringDate);

			mysqlStatement.executeUpdate();
			mysqlStatement.close();
			mysqlStatement = null;

		} catch (SQLException ex) {
			transactionFlag = TRAMSACTION_ROLLBACK;
			System.err.println("SQLException: " + ex.getMessage());
		} finally {
			if (TRAMSACTION_COMMIT == transactionFlag) {
				/* 没有出错，则提交事务 */
				cloudCon.commit();
			} else if (TRAMSACTION_ROLLBACK == transactionFlag) {
				/* 如果出错，则回滚事务 */
				cloudCon.rollback();
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
	public static void syncAfterSales(String cloudUrl, String cloudUsername,
			String cloudPassword, String afterSalesUrl, String afterSalesUsername,
			String afterSalesPassword) throws SQLException, ClassNotFoundException {
		String username;
		String sqlString;
		int count;

		PreparedStatement cloudStatement = null;
		PreparedStatement afterSalesStatement = null;
		Connection cloudCon = null;
		Connection afterSalesCon = null;
		ResultSet afterSalesResult = null;
		ResultSet cloudResult = null;

		try {
			/* 加载mysql jdbc驱动 */
			Class.forName("com.mysql.jdbc.Driver");
			cloudCon = DriverManager.getConnection(cloudUrl, cloudUsername,
					cloudPassword);
			afterSalesCon = DriverManager.getConnection(afterSalesUrl, afterSalesUsername,
					afterSalesPassword);

			afterSalesStatement = afterSalesCon.prepareStatement("select * from pb1ucenter_members");
			afterSalesResult = afterSalesStatement.executeQuery();
			while (afterSalesResult.next()) {
				username = afterSalesResult.getString("username");
				/* 售后数据库username不合法时，表示此数据有问题，不能同步 */
				if (null == username || username.equals("")) {
					continue;
				}

				/* 根据mail查询mysql数据库中，是否已经有了对应的账号信息 */
				sqlString = "select count(*) as count from CCORE_ACCOUNT_INFO where ACCOUNT_LOGIN_NAME=?";
				cloudStatement = cloudCon.prepareStatement(sqlString);
				cloudStatement.setString(1, username);

				cloudResult = cloudStatement.executeQuery();
				cloudResult.next();
				count = cloudResult.getInt("count");

				cloudResult.close();
				cloudResult = null;
				cloudStatement.close();
				cloudStatement = null;

				/* 如果有对应的账号信息，则查找下一条数据，如果没有，则同步此信息到mysql数据库中 */
				if (0 != count) {
					continue;
				} else {
					insetIntoCloud(cloudCon, afterSalesResult);
				}
			}

		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException");
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		} finally {
			/* 最后将结果集，数据库连接等资源释放 */
			if (null != afterSalesResult) {
				afterSalesResult.close();
			}
			if (null != afterSalesStatement) {
				afterSalesStatement.close();
			}
			if (null != afterSalesCon) {
				afterSalesCon.close();
			}

			if (null != cloudResult) {
				cloudResult.close();
			}
			if (null != cloudStatement) {
				cloudStatement.close();
			}
			if (null != cloudCon) {
				cloudCon.close();
			}
		}
	}

	public static void main(String args[]) {
		try {
			/* 以下参数是根据我的测试环境配置的 */
			syncAfterSales("jdbc:mysql://192.168.3.141:3306/test", "root", "root",
					"jdbc:mysql://192.168.3.143:3306/test", "zf",
					"zf");

		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException");
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		}
	}
}
