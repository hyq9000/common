package com.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.model.BatchSmsAttributes;
import com.aliyun.mns.model.MessageAttributes;
import com.aliyun.mns.model.RawTopicMessage;
import com.aliyun.mns.model.TopicMessage;
import com.common.log.ExceptionLogger;

/**
 * 阿里云的短信发送功能类 </br>
 * Date: 2017-06-13
 * 
 * @author hyq
 */
public class AliSms {

	private  CloudAccount account;
	private  MNSClient client;
	private  CloudTopic topic;
	
	/*
	 * 由spring自动注入属性值
	 */
	@Value("#{configProperties['accessKeyId']}")
	private  String accessKeyId;
	@Value("#{configProperties['accessKeySecret']}")
	private  String accessKeySecret;
	@Value("#{configProperties['msnEndpoint']}")
	private  String msnEndpoint;
	@Value("#{configProperties['signName']}")
	private  String signName;
	@Value("#{configProperties['topicTxt']}")
	private  String topicTxt; 
	
	/**
	 * 实例化方法;在非spring环境下，可以采用此方法实例化及初始化相关配置参数；
	 * @param accessId 阿里云APP统一访问ID
	 * @param accessKey 阿里云APP统一访问key
	 * @param msnEndpoint 阿里云要求的东西；
	 * @param signName 短信签名
	 * @param topicTxt 短信主题
	 * @return
	 */
	public  static AliSms getInstance(String accessId,String accessKey,String msnEndpoint,String signName,String topicTxt){		
		return new AliSms(accessId,accessKey,msnEndpoint,signName,topicTxt);
	}
	/**给框架用的构造方法*/
	public AliSms(){}
	
	private  AliSms(String accessId,String accessKey,String msnEndpoint,String signName,String topicTxt){
		account = new CloudAccount(accessId,accessKey,msnEndpoint);
		client = account.getMNSClient();
		topic = client.getTopicRef(topicTxt);
	}
	
	/**
	 * 发送短信
	 * @param templateCode 短信模板码
	 * @param receiverPhoneNumbers 接收者手机；多个手机号，用","隔开; 
	 * @param templateParameters 模板的参数名及值;KEY为参数名，VALUE为对应的值
	 * @throws Exception
	 */
	public void sendMessage(String templateCode,String receiverPhoneNumbers,
			Map<String,String> templateParameters) throws Exception{	
		if( templateCode==null ||templateCode.trim().equals("") || receiverPhoneNumbers==null
				||receiverPhoneNumbers.trim().equals(""))
			throw new Exception("短信发送时参数配置不正确！");
		/*
		 * Step 2. 设置SMS消息体（必须）
		 * 注：目前暂时不支持消息内容为空，需要指定消息内容，不为空即可。
		 */
		RawTopicMessage msg = new RawTopicMessage();
		msg.setMessageBody("sms-message");
		/*
		 * Step 3. 生成SMS消息属性
		 */
		MessageAttributes messageAttributes = new MessageAttributes();
		BatchSmsAttributes batchSmsAttributes = new BatchSmsAttributes();
		// 3.1 设置发送短信的签名（SMSSignName）
		batchSmsAttributes.setFreeSignName(signName);
		// 3.2 设置发送短信使用的模板（SMSTempateCode）
		batchSmsAttributes.setTemplateCode(templateCode);
		// 3.3 设置发送短信所使用的模板中参数对应的值（在短信模板中定义的，没有可以不用设置）
		BatchSmsAttributes.SmsReceiverParams smsReceiverParams = new BatchSmsAttributes.SmsReceiverParams();
		for(Map.Entry<String,String> p : templateParameters.entrySet())
			smsReceiverParams.setParam(p.getKey(),p.getValue());
	
		// 3.4 增加接收短信的号码
		String tels="";
		String[] nums=receiverPhoneNumbers.split(",");
		for(String tel : nums){
			batchSmsAttributes.addSmsReceiver(tel, smsReceiverParams);
			tels+=tel+",";
		}

		messageAttributes.setBatchSmsAttributes(batchSmsAttributes);
	
		/*
		 * Step 4. 发布SMS消息
		 */
		TopicMessage ret = topic.publishMessage(msg, messageAttributes);
		ExceptionLogger.writeLog("成功发送短信："+tels);
	
		client.close();
	}
	
	public static void main(String[] args) throws Exception{
		AliSms sms=AliSms.getInstance("", "", "", "", "");
		sms.sendMessage("tcode", "13378017839",new HashMap<String, String>() {
		    {
		        put("Name", "June"); 
		        put("NUMBER", "4889983"); 
		    }
		});
	}
}
