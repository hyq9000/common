package com.common.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.common.log.ExceptionLogger;

public class HttpUtils {
	
	public static String uploadFile(String action,String name,String fileFullName){
		try {
			HttpClient tc=HttpClientBuilder.create().build();
			HttpPost post=new HttpPost(action);
			MultipartEntityBuilder me=MultipartEntityBuilder.create();
			me.addBinaryBody(name, new File(fileFullName));
			post.setEntity(me.build());
			HttpResponse response=tc.execute(post);
			String result=EntityUtils.toString(response.getEntity());
			post.abort();
			return result;
		} catch (ClientProtocolException e) {
			ExceptionLogger.writeLog(e, HttpUtils.class);
		} catch (IOException e) {
			ExceptionLogger.writeLog(e, HttpUtils.class);
		}
		return null;
	}

	public static HttpResponse get(String action,Map parameters){
		return get(action, parameters, null);
	}
	
	public static HttpResponse get(String action){
		return get(action, null, null);
	}
	
	public static HttpResponse get(String action,Map parameters,Map headers){
		HttpClient tc=HttpClientBuilder.create().build();
		if(parameters!=null && parameters.size()>0){
			List<NameValuePair> params=new ArrayList<NameValuePair>();
			action+="?";
			for(Object entry : parameters.entrySet()){
				Map.Entry<String, String> tmp=(Map.Entry<String, String>)entry;
					action+=tmp.getKey()+"="+tmp.getValue()+"&";
			}
		}
		HttpGet get=new HttpGet(action);
		if(headers!=null && headers.size()>0){
			for(Object entry : headers.entrySet()){
				Map.Entry<String, String> tmp=(Map.Entry<String, String>)entry;
				get.setHeader(tmp.getKey(),tmp.getValue());
			}
		}
		
		try {			
			return tc.execute(get);
		}catch(ClientProtocolException e1){
			ExceptionLogger.writeLog(e1, HttpUtils.class);
		} catch(IOException e2){
			ExceptionLogger.writeLog(e2, HttpUtils.class);
		}
		return null;
	}
	
	
	public static HttpResponse post(String action,Map parameters,Map headers){
		try {	
			HttpClient tc=HttpClientBuilder.create().build();
			HttpPost post=new HttpPost(action);
			if(headers!=null && headers.size()>0){
				for(Object entry : headers.entrySet()){
					Map.Entry<String, String> tmp=(Map.Entry<String, String>)entry;
					post.setHeader(tmp.getKey(),tmp.getValue());
				}
			}
			if(parameters!=null && parameters.size()>0){
				List<NameValuePair> params=new ArrayList<NameValuePair>();
				for(Object entry : parameters.entrySet()){
					Map.Entry<String, String> tmp=(Map.Entry<String, String>)entry;
					params.add(new BasicNameValuePair(tmp.getKey(), tmp.getValue()));
				}
				post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			}
				
			return tc.execute(post);
		} catch (UnsupportedEncodingException e) {
			
		} catch(ClientProtocolException e1){
			ExceptionLogger.writeLog(e1, HttpUtils.class);
		} catch(IOException e2){
			ExceptionLogger.writeLog(e2, HttpUtils.class);
		}	
		return null;
	}
	
	public static HttpResponse post(String action,Map parameters){
		return post(action,parameters,null);
	} 
}
