package com.common.sendmail;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.common.xml.XmlReader;

/**
 * 用户密码找回功能实现，发送邮件给用户 <br/>
 * 创建时间：2012-6-14 <br/>
 * 发送的邮件内容中，不能包含具体IP的网站，如：http://192.168.3.140:8091/ccore/ccore/login.html，
 * 否则发送的邮件会被当成垃圾邮件处理
 * @author zhangfan
 */
public class SendMail {
	/* 全局邮件配置索引下标，目前只有10个邮件账号供发送 */
	public static int INDEX = 0;
	/* 全局邮件发送配置，主要存放发送账号信息 */
	public static List<Map<String,Object>> mailList;
	static {
		XmlReader reader = new  XmlReader(XmlReader.class.getResource("/cloud_config.xml"));
		mailList = reader.readStructure("//tns:email-config");
	}
	
	
	/**
	 * 查找合适的邮件账号，发送邮件给用户 <br/>
	 * 创建时间：2012-6-14 <br/>
	 * @param sendPort 邮件发送端口，一般默认为25<br/>
	 * @param sendServer 邮件服务器域名<br/>
	 * @param sendFrom 发件人姓名<br/>
	 * @param sendAddr 发件人邮箱地址<br/>
	 * @param sendPassword 发件人邮箱密码<br/>
	 * @param subject 邮件主题<br/>
	 * @param body 邮件内容<br/>
	 * @param receiveAddr 收件人邮箱地址，如果有多个收件人，要用";"分开<br/>
	 * @exception 如果操作失败，则抛出异常
	 * @deprecated 此方法从1.1.9.3开始弃用,用public static void sendMail(String subject, String body, String receiveAddr)
	 */
	public static void sendMail(int sendPort, String sendServer,
			String sendFrom, String sendAddr, String sendPassword,
			String subject, String body, String receiveAddr) throws Exception {

        /* 有多少个发送账号，就循环多少次发送 */
		for(int i = 0; i < mailList.size(); i++) {
			try {
				sendOneMail(subject, body, receiveAddr);
			}
			/* 发送的异常处理 */
			catch(Exception e) {
				System.out.println(e);
				/* 如果是目的地址的问题，则将异常抛出，不进行后面账号的发送 */
				if(e.getMessage()!=null&&e.getMessage().equals("Invalid Addresses")){
					throw e;
				}
				/* 如果不是目的地址异常，则跳过当前账号，用后面的账号发送 */
				else {
					/* 跳过当前发送账号，账号索引指向下一个账号 */
					Logger.getLogger(SendMail.class).error("发生错误的邮箱是: " + mailList.get(INDEX).get("user-name").toString() + 
							"exception:",e);
					INDEX = (++INDEX) % mailList.size();
					continue;
				}
			}
			
			/* 有一个发送成功后，直接返回 */
			return;
		}	
	}

	
	/**
	 * 查找合适的邮件账号，发送邮件给用户 <br/>
	 * 创建时间：2012-12-14 <br/>
	 * @param subject 邮件主题<br/>
	 * @param body 邮件内容<br/>
	 * @param receiveAddr 收件人邮箱地址，如果有多个收件人，要用";"分开<br/>
	 * @exception 如果操作失败，则抛出异常
	 */
	public static void sendMail(String subject, String body, String receiveAddr) throws Exception {		
        /* 有多少个发送账号，就循环多少次发送 */
		for(int i = 0; i < mailList.size(); i++) {
			try {
				sendOneMail(subject, body, receiveAddr);
			}
			/* 发送的异常处理 */
			catch(Exception e) {
				System.out.println(e);
				/* 如果是目的地址的问题，则将异常抛出，不进行后面账号的发送 */
				if(e.getMessage()!=null&&e.getMessage().equals("Invalid Addresses")){
					throw e;
				}
				/* 如果不是目的地址异常，则跳过当前账号，用后面的账号发送 */
				else {
					/* 跳过当前发送账号，账号索引指向下一个账号 */
					Logger.getLogger(SendMail.class).error("发生错误的邮箱是: " + mailList.get(INDEX).get("user-name").toString() + 
							"exception:",e);
					INDEX = (++INDEX) % mailList.size();
					continue;
				}
			}
			
			/* 有一个发送成功后，直接返回 */
			return;
		}
	}
	
	
	/**
	 * 根据邮件配置INDEX，发送邮件给用户 <br/>
	 * 创建时间：2012-12-14 <br/>
	 * @param subject 邮件主题<br/>
	 * @param body 邮件内容<br/>
	 * @param receiveAddr 收件人邮箱地址，如果有多个收件人，要用";"分开<br/>
	 * @exception 如果操作失败，则抛出异常
	 */
	private static void sendOneMail(String subject, String body, String receiveAddr) throws Exception {		
		Map<String,Object> map = mailList.get(INDEX);
		String sendPort = map.get("server-port").toString();
		String sendServer = map.get("server-dns").toString();
		String sendAddr = map.get("user-name").toString();
		String sendPassword = map.get("user-password").toString();
		String sendFrom = map.get("signature").toString();

		Properties props = new Properties();
		props.put("mail.smtp.host", sendServer);
		props.put("mail.smtp.port", sendPort);
		props.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(props, null);
		/* 设置发送时间，目的地址，主题，正文等信息 */
		MimeMessage msg = new MimeMessage(session);
		msg.setSentDate(new Date());
		InternetAddress fromAddress = new InternetAddress(sendAddr,
				sendFrom, "UTF-8");
		msg.setFrom(fromAddress);
		
		/* 解析收件人地址，加到邮件收件人列表中 */
		String[] addr = receiveAddr.split(";");
		for(int i = 0; i < addr.length; i++) {
			msg.addRecipients(Message.RecipientType.TO, addr[i]);
		}
		
		msg.setSubject(subject, "UTF-8");
		msg.setText(body, "UTF-8");
		msg.saveChanges();
		/* 构建session会话 */
		Transport transport = null;
		transport = session.getTransport("smtp");
		transport.connect(sendServer, sendAddr, sendPassword);
		transport.sendMessage(msg, msg.getAllRecipients());
		transport.close();
	}
	
	
	public static void main(String args[]) throws Exception {

		/* 发送邮件端口，smtp协议默认为25 */
		int send_port = 25;
		/* 以下配置需要按照实际情况修改，此处为测试配置 */
		/* 邮件服务器域名 */
		String send_server = "smtp.qq.com";
		/* 发送人姓名 */
		String send_from = "湖南纽曼科技";
		/* 发送人邮箱地址 */
		String send_addr = "2372311627@qq.com";
		/* 发送人邮箱密码 */
		String send_password = "sssstttt";
		/* 发送邮件的主题 */
		String subject = "恭喜您：加入到纽曼圈子。";
		/* 发送邮件的正文 */
		String body = "1111111";
		/* 收件人邮箱地址 */
		String receive_addr = "368413156@qq.com";

		/* 发送邮件 */
		sendMail(send_port, send_server, send_from, send_addr,
				send_password, subject, body, receive_addr);
		
		/*
		XmlReader reader = new  XmlReader(XmlReader.class.getResource("cloud_config.xml"));
		List<Map<String,Object>> list = reader.readStructure("//tns:email-config");
		
		for(int i = 0; i < list.size(); i++) {
			Map<String,Object> map = list.get(i);
			String sendPort = map.get("server-port").toString();
			String sendServer = map.get("server-dns").toString();
			String sendAddr = map.get("user-name").toString();
			String sendPassword = map.get("user-password").toString();
			String sendFrom = map.get("signature").toString();
			
			System.out.println(sendPort);
			System.out.println(sendServer);
			System.out.println(sendAddr);
			System.out.println(sendPassword);
			System.out.println(sendFrom);
			
		}
		*/

	}

}