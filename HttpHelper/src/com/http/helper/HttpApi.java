package com.http.helper;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public abstract class HttpApi implements Runnable {

	public static final int RETRY_COUNT = 2;
	public static final int TIME_OUT_DEF = 15 * 1000;
	public static final int TIME_OUT_MIN = 5 * 1000;

	public static final String HTTP_DATA = "data";
	public static final String HTTP_STATE = "state";
	public static final String HTTP_TAGE = "tage";
	public static final String HTTP_RESULT_JSON = "json";

	public static final int STATUS_SUCCEED = 200;
	public static final int STATUS_FAILED = 199;
	public static final int HTTP_RESULT_CODE = 201;
	private static final String TAG = "HttpApi";
	public Context context;
	public boolean debug = false;
	public int timeout = TIME_OUT_DEF;
	public int retryCount = RETRY_COUNT;
	public boolean retryEnable = false;
	public boolean verbose = false;
	Map<String,String> httpHeader;
	boolean canNullHttpHeader = true;
	Handler handler;
	Observer observer;
	String url;
	Map<String,String> param;
	boolean httpGetMethod = false;
	protected boolean mUseDefault = true;

	@Override
	public void run() {
		if(TextUtils.isEmpty(url)) {
			String urlmsg = "post param is null, Please check it";
			throw new IllegalArgumentException(urlmsg);
		}
		String resultJson = null;
		if(httpGetMethod) {
			resultJson = doGet(url, param, timeout);
		} else {
			if((param == null || param.isEmpty())) {
				String msg = "post param is null, Please check them ";
				throw new IllegalArgumentException(msg);
			}
			resultJson = doPost(url, param, timeout);
		}
		if(observer != null) {
			observer.post(HTTP_RESULT_CODE, resultJson);
		}
		if(handler != null) {
			Message msg = handler.obtainMessage(HTTP_RESULT_CODE);
			int status = STATUS_SUCCEED;
			if(TextUtils.isEmpty(resultJson)) {
				status = STATUS_FAILED;
			}
			msg.arg1 = status;
			Bundle bundle = msg.getData();
			bundle.putString(HTTP_RESULT_JSON, resultJson);
			msg.sendToTarget();
		}
	}

	public boolean isHttpGetMethod() {
		return httpGetMethod;
	}

	public void setHttpGetMethod(boolean httpGetMethod) {
		this.httpGetMethod = httpGetMethod;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the param
	 */
	public Map<String, String> getParam() {
		return param;
	}

	/**
	 * @param param the param to set
	 */
	public void setParam(Map<String, String> param) {
		this.param = param;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public boolean isRetryEnable() {
		return retryEnable;
	}

	public void setRetryEnable(boolean retryEnable) {
		this.retryEnable = retryEnable;
	}

	/**
	 * @return the observer
	 */
	public Observer getObserver() {
		return observer;
	}

	/**
	 * @param observer the observer to set
	 */
	public void setObserver(Observer observer) {
		this.observer = observer;
	}

	/* -----------------------------------------------------------
	 *      protected method for package inner use
	 * -----------------------------------------------------------*/
	public Handler getHandler(){
		return handler;
	}
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}

	/**
	 * Execute http get, and return result(json String)
	 * start request by http get method and return json result
	 * @param url
	 * @param param : it will been Stitched as integral get request url
	 * (eg, http://42.121.120.19/login?xx=aa&yy=bb).
	 *  If url is integral, the param is null or empty.
	 *  @param timeout
	 * @return
	 */
	public abstract String doGet(String url, Map<String,String> param,int timeout);

	/**
	 * Execute http post, and return result(json String)
	 * @param url : url used to connect
	 * @param handler : handler callback when connection finished
	 * @param param : the data to connect 
	 * @param timeout : time out
	 * */
	public abstract String doPost(String url, Map<String, String> param,
			int timeout);

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug
	 *            the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isCanNullHttpHeader() {
		return canNullHttpHeader;
	}

	public void setCanNullHttpHeader(boolean canNullHttpHeader) {
		this.canNullHttpHeader = canNullHttpHeader;
	}

	/**
	 * get post
	 * 
	 * @param url
	 * @return
	 */
	protected HttpPost getHttpPost(String url) {
		HttpPost post = new HttpPost(url);
		initHttpHeader(post);
		return post;
	}
	
	/**
	 * add http request header base on your need
	 * */
	public void setHttpHeader(Map<String,String> header){
    	httpHeader = header;
    }
	
	public Map<String,String> getHttpHeader(){
		return httpHeader;
	}

	/**
	 * init http header to do QeuenAuth.
	 *  if Http request Header is null, it maybe lead to request failed because of QeuenAuth
	 * @param reqest  HttpHeader is null, it maybe lead to request failed because of QeuenAuth
	 */
	protected void initHttpHeader(final HttpRequestBase reqest) {
		if(!canNullHttpHeader && isEmptyHeader()) {
			Log.e(TAG, "httpHeader is null url: " + reqest.getURI());
			String eMsg = "HttpHeader can not be null(null maybe lead to failed), " +
    				"Please set header by lib.queue.transaction.QeuenAuth"; 
    		throw new IllegalArgumentException(eMsg);
		}
		if(httpHeader != null && !httpHeader.isEmpty()){
    		for(String key : httpHeader.keySet())
    			reqest.addHeader(key, httpHeader.get(key));
    	} else {
    		Log.i(TAG, "httpHeader null, url: " + reqest.getURI());
    	}
	}
	
	private boolean isEmptyHeader() {
		return httpHeader == null || httpHeader.isEmpty();
	}
	
	protected ReqResponce executeClient(final DefaultHttpClient httpClient, final HttpRequestBase httpRequest) throws ClientProtocolException, IOException, URISyntaxException {
		String json = "";
		int statusCode = -1;
		ReqResponce queueResponce = new ReqResponce();
		HttpResponse httpresponse = null;
		boolean interrupt = false; // intercepted by gateway or not
		if(debug) Log.d(TAG,"Request execute URL: " + httpRequest.getURI());
		httpresponse = httpClient.execute(httpRequest);
		statusCode = httpresponse.getStatusLine().getStatusCode();
		org.apache.http.Header contenttypeHeader = httpresponse.getEntity().getContentType();
        String contentType = contenttypeHeader.getValue();
        // determine it is wml
        if (contentType != null && 
        	contentType.indexOf("vnd.wap.wml") > 0) {
        	Log.e(TAG,"interrupt contentType= " + contentType);
            interrupt = true;
        }
		if(!interrupt){
			switch (statusCode) {
			case HttpStatus.SC_OK:
				HttpEntity ent = httpresponse.getEntity();
				json = EntityUtils.toString(ent,HTTP.UTF_8);
				if(verbose) Log.v(TAG,"JSON: " + json);
				if (json != null && json.length() > 0) {
					/*
					 * return context is html file and then retry request current request
					 * */
					if (json.startsWith("<html")
							|| json.startsWith("<Html")
							|| json.startsWith("<HTML")) { 
						interrupt = true;
						httpresponse = null;
						json = "";
						Log.e(TAG, "INVALID JSON, will retry it.... ");
					}
				}
				break;
			 case 301: // HttpConnection.HTTP_MOVED_PERM
		     case 302: // HttpConnection.HTTP_MOVED_TEMP
		     case 307: // HttpConnection.HTTP_TEMP_REDIRECT
		    	 interrupt = true;
				 String fowardUrl = null;
				 if (httpresponse.containsHeader("location")) {
					org.apache.http.Header redirectHeader = 
							httpresponse.getFirstHeader("location");
					fowardUrl = redirectHeader.getValue();
					Log.e(TAG,"return fowardUrl= " + fowardUrl);
					if(!TextUtils.isEmpty(fowardUrl))
						httpRequest.setURI(new java.net.URI(fowardUrl));
				 }
		       break;
			default:
				break;
			}
		}
		logError(TAG, statusCode, json);
		queueResponce.setStatusCode(statusCode);
		queueResponce.setInterrupted(interrupt);
		queueResponce.setJson(json);
		return queueResponce;
	}

	public String postImage(String url, String path) {
		return postImage(url, path, null, false, false);
	}
	/**
	 * post a image to server
	 * @param url
	 * @param path
	 * @param params
	 * @param isMergeParam  to merge param to url (for get method)
	 * @return
	 */
	public String postImage(String url, String path,Map<String,
			String> params, boolean isMergeParam, boolean isAndParams) {
		String strResult = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			setParamFroHttpClient(httpclient.getParams(), params);
			httpclient.getParams().setParameter(
					CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			if (isMergeParam) {
				url = getUrlWithParams(url, params, isAndParams);
			}
			HttpPost httppost = getHttpPost(url);
			File file = new File(path);
			log(TAG, "postImage : url=  " + url + ", path= " + path);
			FileEntity reqEntity = new FileEntity(file, "binary/octet-stream");
			httppost.setEntity(reqEntity);
			reqEntity.setContentType("binary/octet-stream");
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			// handle result
			if (resEntity != null) {
				strResult = EntityUtils.toString(resEntity);
			}
			if (resEntity != null) {
				resEntity.consumeContent();
			}
			httpclient.getConnectionManager().shutdown();
			int statusCode = response.getStatusLine().getStatusCode();
			logError(TAG, statusCode, strResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}

	public static void setParamFroHttpClient(HttpParams params, Map<String,String> paramMap) {
		if(paramMap != null && !paramMap.isEmpty()){
			for(Entry<String, String> en : paramMap.entrySet()){
				params.setParameter(en.getKey(), en.getValue());
			}
		}
	}

	/**
	 * merge url and params for GET method.
	 * @param srcUrl
	 * @param paramMap the param map
	 * @returnc  merged url and params for GET method.
	 */
	public static String getUrlWithParams(String srcUrl, Map<String,String> paramMap, boolean isAndParams) {
		String destUrl = null;
		if(paramMap != null && !paramMap.isEmpty()){
			StringBuilder buffer = new StringBuilder();
			buffer.append(srcUrl);
			if(isAndParams) {
				buffer.append("&");//and other params
			} else {
				buffer.append("?");//new params
			}
			
			String value = null;
			for(String key : paramMap.keySet()){
				value = paramMap.get(key);
				buffer.append(key).append("=").append(value).append("&");
			}
			buffer.deleteCharAt(buffer.length()-1);
			destUrl = buffer.toString().trim();
		} else {
			destUrl = srcUrl;
		}
		return destUrl;
	}

	public void logError(String tag, int statusCode, String json) {
		if(statusCode != HttpStatus.SC_OK || TextUtils.isEmpty(json)) {
			Log.e(tag, "error statusCode = " + statusCode);
			Log.w(tag, "result json: " + json);
		}
	}

	public void log(String tag, String msg) {
		if(debug) {
			Log.d(tag, msg);
		}
	}
}
