package com.common.tools.sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 提供发送短信服务实现类
 * 
 * @author leihao 创建时间:2014-04-05
 */
public class ShortMessageServiceImpl implements ShortMessageService {
	/**
	 * 发送短信到单个手机号 
	 * @param phoneNumber    目标手机号
	 * @param messageContent   短信内容
	 * @return 发送短信成功之后返回字符串"发送成功",否则则返回字符串"发送失败"
	 */
	public String sendMessage(String phoneNumber, String messageContent) {
		return sendMessages(phoneNumber, messageContent);
	}

	/**
	 * 群发短信 
	 * @param phoneNumbers            群发号码数组
	 * @param messageContext     群发内容
	 * @return 群发短信成功之后返回字符串"发送成功",否则则返回字符串"发送失败"
	 */
	public String sendGroupMessage(String[] phoneNumbers, String messageContext) {
		String phones = "";
		for (int i = 0; i < phoneNumbers.length; i++) {
			phones += phoneNumbers[i];
			if (!(i == phoneNumbers.length - 1)) {
				phones += ",";
			}
		}
		return sendMessages(phones, messageContext);
	}

	/**
	 * 请求短信平台发送短信,接收返回信息
	 * 
	 * @param url
	 *            短信平台地址
	 * @param postData
	 *            发送短信配置参数
	 * @return 短信平台返回的信息
	 */
	private String GetResponseDataByID(String url, String postData) {
		String data = null;
		try {
			URL dataUrl = new URL(url);
			HttpURLConnection con = (HttpURLConnection) dataUrl
					.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Proxy-Connection", "Keep-Alive");
			con.setDoOutput(true);
			con.setDoInput(true);

			OutputStream os = con.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			dos.write(postData.getBytes());
			dos.flush();
			dos.close();

			InputStream is = con.getInputStream();
			DataInputStream dis = new DataInputStream(is);
			byte d[] = new byte[dis.available()];
			dis.read(d);
			data = new String(d);
			con.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return data;
	}

	// 发送短信参数配置
	private String sendMessages(String phoneNumber, String messageContent) {
		String flag = "发送失败";
		SendMessageConfig smc = SendMessageConfig.getConfigInfo();
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
		String dateStr = sf.format(date);// 发送时间
		if (!phoneNumber.equals("") && !messageContent.equals("")) {
			// 发送短信请求的地址
			String URL = smc.getUrl();
			String postData = smc.getAction() + "&userid=" + smc.getUserid()
					+ "" + "&account=" + smc.getAccount() + "" + "&password="
					+ smc.getPassword() + "" + "&mobile=" + phoneNumber + ""
					+ "&content=" + messageContent + dateStr + "【点到信息】"
					+ "&sendTime=" + smc.getSendTime() + "" + "&extno="
					+ smc.getExtno();
			String data = GetResponseDataByID(URL, postData);
			if (data.indexOf("ok") > 0) {
				flag = "发送成功";
			}
		}
		return flag;
	}
}
