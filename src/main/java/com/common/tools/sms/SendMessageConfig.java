package 

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * 短信功能参数配置
 * @author leihao
 *
 */
public class SendMessageConfig {
	private String url;//发送短信功能地址
	private String userid;//企业ID
	private String account;//用户名
	private String password;//密码
	private String sendTime;//默认是立即发送短信,否则按照设定的时间发送短信
	private String extno;//默认为空
	private  final String action = "action=send";//默认
	public static SendMessageConfig getConfigInfo(){
		SendMessageConfig smc = new SendMessageConfig();
	/*	String path = smc.getClass().getResource("").getPath();
		path = path.substring(1,path.length());
		Properties p =new Properties();
		try {
			path = path+"messageInterfaceInfo.properties";
			File file = new File(path);
			FileInputStream pInStream = null;
			pInStream = new FileInputStream(path);
			p.load(pInStream);
			if(p != null){*/
				smc.setUrl("http://sms.junmedia.cn:8888/sms.aspx");
				smc.setUserid("1056");
				smc.setAccount("diandaowang");
				smc.setSendTime("");
				smc.setPassword("diandaowang");
				smc.setExtno("");
	/*		}
		} catch (Exception e) {
			e.printStackTrace();	
		}*/
		return smc;
	}
	public String getExtno() {
		return extno;
	}
	public void setExtno(String extno) {
		this.extno = extno;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}
	public String getAction() {
		return action;
	}
	
}
