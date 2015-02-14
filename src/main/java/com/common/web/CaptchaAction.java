package com.common.web;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.apache.struts2.ServletActionContext;

import com.common.log.ExceptionLogger;
import com.opensymphony.xwork2.ActionSupport;
import com.sun.image.codec.jpeg.*;

/**
 * 响应验证码图片的action,可以响应一张四位数字或字母的图片<br/>
 * 响应成功后，用户程序可以通过session.getAttribute("captcha")来获取该验证码的值;</br>
 * 时间：2012-8-14 
 * @author yuqing
 */
public class CaptchaAction extends ActionSupport {
	private int obstructLineCount=10,//干扰线数
		height=29,//高
		width=99,//宽
		fontSize=20,//字体大小
		charCount=4;//字符数
	
	private String fontColor="128,128,128,255",//字体颜色		
		backgroudColor="255,255,255,255",//背景颜色
		obstructLineColor="128,128,128,255";//干扰线颜色
	
	/**
	 * 产生一张随机验证码的图片
	 */
	public  String captcha() throws Exception {
		BufferedImage bufImage=new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphic=bufImage.createGraphics();	
		graphic.setColor(this.StringToRGB(backgroudColor));
		//graphic.setBackground(this.StringToRGB(backgroudColor));
		graphic.fillRect(0, 0, width, height);
		//产生干扰线
		graphic.setColor(this.StringToRGB(obstructLineColor));
		for(int i=0;i<obstructLineCount;i++){				
			int x1=(int)(Math.random()*50),y1=(int)(Math.random()*30), 
					x2=(int)(Math.random()*100),y2=(int)(Math.random()*30);			
			graphic.drawLine(x1, y1, x2, y2);
		}		
		//设置字体颜色
		Font font=new Font("隶体", Font.BOLD,fontSize);
		graphic.setFont(font);
		graphic.setColor(this.StringToRGB(fontColor));
		String code="",codeStr="";
		//循环产生4个随机码，随机码可以是大写字母及数字，如果字母是O，I时，以区别数字0,1，故换成小写字母o,i;
		for(int i=0;i<charCount;i++){
			//随机生成字母或数字:如果随机数是1则生成数字，否则生成字母
			if((int)(Math.random()*2)>0){
				int tmp=(int)(Math.random()*10);
				codeStr+=tmp+" ";	
				code+=tmp;
			}else{
				char tmp=(char)((int)(Math.random()*23)+65);
				//将相I,1,O,0这种相似的字符加以区分
				if(tmp=='I')
					tmp='i';
				else if(tmp=='O')
					tmp='o';
				codeStr+=tmp+" ";
				code+=tmp;
			}
		}
		graphic.drawString(codeStr,15,20);
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("image/png");
		request.getSession().setAttribute("captcha",code);		
		ServletOutputStream ops=response.getOutputStream();
		JPEGImageEncoder encoder=JPEGCodec.createJPEGEncoder(ops);       
		encoder.encode(bufImage);        
		ops.flush();
		return null;
	}

	/**
	 * 设置干扰线条数
	 * @param obstructLineCount
	 */
	public void setObstructLineCount(int obstructLineCount) {
		this.obstructLineCount = obstructLineCount;
	}


	/*
	 * 设置验证码字数
	 * @param charCount
	 
	public void setCharCount(int charCount) {
		this.charCount = charCount;
	}
	*/
	/**
	 * 设置字体颜色
	 * @param fontRGB @see {@link java.awt.Color#Color(int)}
	 */
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	

	/*public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}*/

	public void setBackgroudColor(String backgroudColor) {
		this.backgroudColor = backgroudColor;
		
	}

	/**
	 * 设置干扰线颜色
	 * @param obstructLineColor  @see {@link java.awt.Color#Color(int)}
	 */
	public void setObstructLineColor(String obstructLineColor) {
		this.obstructLineColor= obstructLineColor;
	}
	
	/**
	 * 将格式字符串转换成Color对象
	 * @param value
	 * @return
	 */
	private Color StringToRGB(String value){
		try {
			String[] rgbStr=value.split(", *");
			if(rgbStr.length<3){
				throw new Exception("RGB颜色配置不正确!");
			}
			int r=Integer.parseInt(rgbStr[0]);
			int g=Integer.parseInt(rgbStr[1]);
			int b=Integer.parseInt(rgbStr[2]);
			int a=255;
			if(rgbStr.length==4)
				a=Integer.parseInt(rgbStr[3]);
			return new Color(r,g,b,a);
		} catch (Exception e) {
			ExceptionLogger.writeLog(e, this);
			return null;
		}
	}
}
