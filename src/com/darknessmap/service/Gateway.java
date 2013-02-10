package com.darknessmap.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class Gateway {

	//TODO: Remove from here, we have to use an external system
	String 				_url;
	DefaultHttpClient 	httpclient;
	HttpPost 			httppost;
	List<NameValuePair> nameValuePairs;
	
	private static final Logger logger = LoggerFactory.getLogger();
	
	/**
	 * 
	 */
	public void initialize()
	{
		httpclient      = new DefaultHttpClient();
		httppost 		= new HttpPost(this._url);
		nameValuePairs  = new ArrayList<NameValuePair>(1);//TODO: This should be dynamic.
	}
	
	/**
	 * 
	 * @param payload
	 */
	public void publish(String payload)
	{
		try
		{
			//make sure we dont concatenate previous items.
			if(! nameValuePairs.isEmpty()) nameValuePairs.clear();
			
            // Add your data
            nameValuePairs.add(new BasicNameValuePair("data", payload));
            System.out.println("POST: "+ nameValuePairs.toString());
            
            //make POST, should be PUT?
//            httppost.setMethod("PUT"); //RequestWrapper
            
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            //InputStream inputStream = 
            response.getEntity().getContent().close();
		}
		catch (Exception e)
        {
            logger.error("Update Server: "+e.toString());
        }
	}
	
	
	/**
	 * 
	 * @param url
	 */
	public void setUrl(String url)
	{
		this._url = url;
	}
}
