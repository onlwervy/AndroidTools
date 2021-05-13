/**
 * @author: wang.lili<wang.lili@neusoft.com>
 * @date: 2012-12-10
 * @description: http client
 */

package com.http.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class NetClient extends HttpApi {

	private static final String TAG = "NetClient";

	public HttpClient httpclient;

	public NetClient(Context context, Map<String,String> httpHeader) {
		this.context = context;
		this.httpHeader = httpHeader;
		httpclient = getHttpClient();
	}

	public NetClient(Context context, Map<String,String> httpHeader, int timeout) {
		this.context = context;
		this.httpHeader = httpHeader;
		this.timeout = timeout;
		httpclient = getHttpClient();
	}

	public NetClient(Context context, Map<String,String> httpHeader, int timeout, boolean debug) {
		this.context = context;
		this.httpHeader = httpHeader;
		this.timeout = timeout;
		this.debug = debug;
		httpclient = getHttpClient();
	}

	/**
	 * get Http Client
	 * @return
	 */
	public HttpClient getHttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		return new DefaultHttpClient(httpParams);
	}

	/**
	 * post Map data
	 * 
	 * @param url
	 * @param postdata
	 * @author hulk
	 * @return
	 */
	public String doPost(String url, Map<String, String> postdata) {
		return doPost(url, postdata, timeout);
	}

	@Override
	public String doGet(String url, Map<String, String> paramMap, int timeout) {
		if(TextUtils.isEmpty(url)){
			Log.e(TAG, "doGet error: url  is null");
			throw new IllegalArgumentException("error: url is null");
		}
		String result = "";
		try {
			HttpGet get = new HttpGet(url);
			initHttpHeader(get);
			if(debug) Log.d(TAG,"GET start url= " + url
					+ ",\n time: " + new Date().toLocaleString());
			setParamFroHttpClient(httpclient.getParams(), paramMap);
			HttpResponse response = httpclient.execute(get);
			HttpEntity resEntity = response.getEntity();
			// zhanghao mody fy
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity);
				}
			} else {
				handleError(statusCode, response);
			}
			logError(TAG, statusCode, result);
			if(debug) Log.d(TAG,"GET result json: " + result
					+ ",\n time: " + new Date().toLocaleString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String doPost(String url, Map<String, String> param, int timeout) {
		if(TextUtils.isEmpty(url) || param == null){
			Log.e(TAG, "doPost error: url or param is null");
			throw new IllegalArgumentException("error: url or param is null");
		}
		String result = "";
		try {		
			HttpClient client = getHttpClient();
			HttpPost post = getHttpPost(url);
			initHttpHeader(post);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if(param != null) {
				Set<String> mapSet = param.keySet();
				Iterator<String> itor = mapSet.iterator();
				while (itor.hasNext()) {
					String key = itor.next();
					BasicNameValuePair param1 = new BasicNameValuePair(key,
							param.get(key));
					params.add(param1);
				}
			}
			if(debug) Log.d(TAG,"POST start url= " + url + ",\nparam: " + param
					+ ",\ntime: " + new Date().toLocaleString());
			HttpEntity paramEntity = new UrlEncodedFormEntity(params, "utf-8");
			post.setEntity(paramEntity);
			HttpResponse response = client.execute(post);
			//zhanghao modify
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			} else {
				handleError(statusCode, response);
			}
			logError(TAG, statusCode, result);
			if(debug) Log.d(TAG,"POST result json: " + result
					+ ",\ntime: " + new Date().toLocaleString());
		} catch (IOException e) {
			Log.e(TAG, "receive error info from server: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * handle error messge
	 * @param statusCode
	 * @param response
	 */
	void handleError(int statusCode, HttpResponse response) {
		if (statusCode != HttpStatus.SC_OK) {
			String msg = "error HttpStatus= " + statusCode;
			if (statusCode == HttpStatus.SC_BAD_GATEWAY) {
				msg = "SC_BAD_GATEWAY";
			}
			Log.e(TAG, "handle Error " + msg);
		}
	}

	/**
	 * get
	 * 
	 * @param url
	 * @return
	 */
	public String doGet(String url) {
		return doGet(url, null, timeout);
	}
}
