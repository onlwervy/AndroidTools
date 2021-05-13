package com.http.helper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
/**
 * Copyright HULK All Rights Reserved
 * @author HULK & HULK Created at 2013-11-04
 * @version 1.0.0
 * */
public class NetTask extends HttpApi {
	
	private static final String TAG = "NetTask";
	
	// APN for network operation
    private int mApn = APN_WIFI;	//	wifi
    protected static final int APN_WIFI = -1;
    protected static final int APN_NOTSET = 0;
    protected static final int APN_CMWAP = 1;	//	mobile 2G wap ip = 10.0.0.172
    protected static final int APN_CMNET = 2;	//	mobile 2G net
    protected static final int APN_UNIWAP = 3;	//	unit com 2G wap 
    protected static final int APN_UNINET = 4;	//	unit com 2G net 
    protected static final int APN_CTWAP = 5;	//	telecom 2G wap ip = 10.0.0.200
    protected static final int APN_CTNET = 6;	//	telecom 2G net
    protected static final int APN_3GWAP = 7; //  telecom 3G
	
	private String netTypeName = null;	//	wifi
	
	private int resultCode = HTTP_RESULT_CODE;

	public NetTask(Context context) {
		this.context = context;
	}
	
	public NetTask(Context context, Map<String,String> httpHeader) {
		this.context = context;
		this.httpHeader = httpHeader;
		init();
	}
	
	public NetTask(Context context, Map<String,String> httpHeader, boolean debug){
		this.debug = debug;
		this.context = context;
		this.httpHeader = httpHeader;
		init();
	}
	
	public NetTask(Context context, Map<String,String> httpHeader, int timeout, boolean debug){
		this.debug = debug;
		this.context = context;
		this.timeout = timeout;
		this.httpHeader = httpHeader;
		init();
	}
	
	public NetTask(Context context, Map<String,String> httpHeader, Observer observer, boolean debug){
		this(context, httpHeader, observer, TIME_OUT_DEF, debug);
	}
	
	public NetTask(Context context, Map<String,String> httpHeader, Observer observer, int timeout, boolean debug){
		this.context = context;
		this.observer = observer;
		this.timeout = timeout;
		this.debug = debug;
		this.httpHeader = httpHeader;
		init();
	}

	public NetTask(Context context, Map<String,String> httpHeader, Handler handler, boolean debug){
		this(context, httpHeader, handler, TIME_OUT_DEF, debug);
	}

	public NetTask(Context context, Map<String,String> httpHeader, Handler handler, int timeout, boolean debug){
		this.context = context;
		this.handler = handler;
		this.timeout = timeout;
		this.debug = debug;
		this.httpHeader = httpHeader;
		init();
	}
	
	public NetTask(Context context, Map<String,String> httpHeader, Observer observer, String url, Map<String,String> param){
		this.context = context;
		this.observer = observer;
		this.url = url;
		this.param = param;
		this.httpHeader = httpHeader;
		init();
	}

	public void init() {
		this.netTypeName = getMobileNetWorkType(context);
	}

	/**
	 * @return the resultCode
	 */
	public int getResultCode() {
		return resultCode;
	}

	/**
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getNetTypeName() {
		return netTypeName;
	}

	public void setNetTypeName(String netTypeName) {
		this.netTypeName = netTypeName;
	}

	/* --------------------------------------------------------------------------
	 * 	  App API inner useful
	 * --------------------------------------------------------------------------*/
	/**
	 * @param url : url used to connect
	 * @param handler : handler callback when connection finished
	 * @param param : the data used to connect 
	 * @param timeout : time out
	 * */
	public String doPost(String url, Map<String,String> param) {
		return doPost(url, param, TIME_OUT_DEF);
	}
	
	public String doPost(String url, Map<String,String> params, int timeout) {
		String json = "";
		ReqResponce res = doPost(url, params, true);
		if(res != null) {
			json = res.getJson();
		}
		return json;
	}

	/**
	 * get a QueueResponce contains throwable
	 * @param url
	 * @param params
	 * @param debug
	 * @return
	 */
	public ReqResponce doPost(String url, Map<String, String> params, boolean debug) {
		ReqResponce res = null;
		Throwable throwable = null;
		try {
			res = doPost(url, params, timeout, false);
		} catch (Throwable e) {
			Log.e(TAG, "doPost Throwable: " + e);
			throwable = e;
			e.printStackTrace();
		}
		if(res == null) {
			res = new ReqResponce();
		}
		res.setThrowable(throwable);
		return res;
	}
	
	public ReqResponce doPostOrThrowable(String url, Map<String, String> params, boolean debug) throws Throwable {
		return doPost(url, params, timeout, false);
	}

	/**
	 * @param url : url used to connect
	 * @param handler : handler callback when connection finished
	 * @param param : the data to connect 
	 * @param timeout : time out
	 * @param catchException
	 * @return
	 * @throws Exception 
	 */
	public ReqResponce doPost(String url, Map<String,String> param, int timeout, boolean catchException) throws Throwable {
		if(timeout <= TIME_OUT_MIN) {
			timeout = TIME_OUT_DEF;
		}
		if(TextUtils.isEmpty(url) || param == null){
			Log.e(TAG, "doPost error: url or param is null");
			throw new IllegalArgumentException("error: url or param is null");
		}
		List<BasicNameValuePair> data = null;
		if(param != null && !param.isEmpty()){
			data = new ArrayList<BasicNameValuePair>(param.size());
			String value = null;
			for(String key : param.keySet()){
				value = param.get(key);
				data.add(new BasicNameValuePair(key,value));
			}
		}
		if(debug) Log.d(TAG,"POST start url= " + url + ",\nparam: " + param
				+ ",\n time: " + new Date().toLocaleString());
		HttpPost post = new HttpPost(url);
		setNetTypeIfNeed(post);
        if(data != null) post.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));
		HttpParams params = createHttpParams(timeout);
		post.setParams(params);
		ReqResponce responce = startRequestBase(post, params, catchException);
		if(debug) Log.d(TAG,"POST result responce: " + responce
				+ ",\n time: " + new Date().toLocaleString());
		return responce;
	}

	/**
	 * set net type if need(cmwap,3g)
	 * @param reqest
	 */
	private void setNetTypeIfNeed(HttpRequestBase reqest) {
		try {
			if ("cmwap".equals(netTypeName)) {
	            String href = url;
	            String host = null;
	            String suffix = null;

	            if (href.startsWith("http://")) {
	                href = href.substring(7);
	            }
	            int index = href.lastIndexOf('/');
	            if (index != -1) {
	                host = href.substring(0, index);
	                suffix = href.substring(index);
	            } else {
	                host = href;
	                suffix = "";
	            }
	            URI uri = new URI("http://10.0.0.172:80" + suffix);
	            reqest.setURI(uri);
	            reqest.setHeader("X-Online-Host", host);
	            Log.d(TAG,"cmwap: href=" + href + ",host=" + host + ",suffix=" + suffix);
	        }
		} catch (URISyntaxException e) {
			 Log.e(TAG,"URISyntaxException: " + e);
			 e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG,"setNetTypeIfNeed Exception: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * start request by http get method and return json result
	 * @param url
	 * @return
	 */
	public String doGet(String url) {
		return doGet(url, null, TIME_OUT_DEF);
	}
	
	public String doGet(String url, Map<String,String> param, int timeout) {
		String json = "";
		try {
			ReqResponce responce = doGet(url, param, timeout, true);
			if(responce != null) {
				json = responce.getJson();
			}
		} catch (Throwable e) {
			Log.e(TAG, "doGet url: " + url + ",param:" + param + "\nException: " + e );
			e.printStackTrace();
		}
		return json;
	}

	public ReqResponce doGet(String url, Map<String, String> param) {
		ReqResponce res = null; 
		Throwable throwable = null;
		try {
			res = doGet(url, param, timeout, true);
		} catch (Throwable ex) {
			Log.e(TAG, "doPost Throwable: " + ex);
			throwable = ex;
			ex.printStackTrace();
		}
		if(res == null) {
			res = new ReqResponce();
		}
		res.setThrowable(throwable);
		return res;
	}
	
	public ReqResponce doGetOrThrowable(String url, Map<String, String> param) throws Throwable {
		return doGet(url, param, timeout, true);
	}
	
	public ReqResponce doGetOrThrowable(String url, Map<String,String> param, boolean debug) throws Throwable {
		this.debug = debug;
		return doGet(url, param, timeout, true);
	}

	/**
	 * start request by http get method and return json result
	 * @param url
	 * @param param : it will been Stitched as integral get request url
	 * (eg, http://42.121.120.19/login?xx=aa&yy=bb).
	 *  If url is integral, the param is null or empty.
	 *  @param timeout
	 * @return
	 * @throws Exception 
	 */
	public ReqResponce doGet(String url, Map<String,String> paramMap,int timeout, boolean catchException) throws Throwable {
		if(TextUtils.isEmpty(url)){
			Log.e(TAG, "doGet error: url  is null, param: " + paramMap);
			throw new IllegalArgumentException("error: url is null");
		}
		String destUrl = getUrlWithParams(url, paramMap, false);
		if(debug) Log.d(TAG,"GET start destUrl: " + destUrl 
				+ ", param: " + param + ", \ntime: " + new Date().toLocaleString());
		HttpGet httpGet = new HttpGet(destUrl);
		setNetTypeIfNeed(httpGet);
		HttpParams params = createHttpParams(timeout);
		setParamFroHttpClient(params, paramMap);
		httpGet.setParams(params);
		ReqResponce responce = startRequestBase(httpGet, params, catchException);
		if(debug) Log.d(TAG,"GET result responce: " + responce 
				+ ",\n time: " + new Date().toLocaleString());
		return responce;
	}
	
	/**
     * set network Proxy
     * @param context
     * @param httpRequestOrigin
     */
	private void setNetProxy(HttpUriRequest httpRequestOrigin) {
        final String host = Proxy.getDefaultHost();
        final int port = Proxy.getDefaultPort();
        if (!TextUtils.isEmpty(host) && port != -1) {
            final HttpHost proxy = new HttpHost(host, port);
            httpRequestOrigin.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }
    
	private void removetNetworkProxy(HttpUriRequest request) {
		request.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
	}

	/**
	 * set APN of mobile net works
	 * @param apnName
	 */
	private void setApn (String apnName) {
    	if (apnName == null || apnName.length() == 0) {
    		mApn = APN_NOTSET;
    		return;
    	}
    	int newApn;
    	apnName = apnName.toLowerCase();
    	if ("cmwap".equals(apnName)) {
    		newApn = APN_CMWAP;
    	} else if ("uniwap".equals(apnName)) {
    		newApn = APN_UNIWAP;
    	} else if ("ctwap".equals(apnName)) {
    		newApn = APN_CTWAP;
    	} else if ("3gwap".equals(apnName)) {
    		newApn = APN_3GWAP;
        } else if("wifi".equals(apnName)){
        	newApn = APN_WIFI;
        } else {
        	newApn = APN_NOTSET;
    	}
    	if (mApn != newApn) {
    		mApn = newApn;
        	mUseDefault = true;
    	}
    }

    // ==================================================================
    //     private method for http task self call
    // ==================================================================
    /**
     * start to http connection
     * @hide
     * @param reqest
     * @param params
     * @param catchException
     * @return QueueResponce object
     * @throws Exception
     */
	private ReqResponce startRequestBase(final HttpRequestBase reqest,final HttpParams params, boolean catchException) throws Throwable {
    	initHttpHeader(reqest);
		HttpRequestBase httpRequest = reqest;
		String netName = netTypeName;
		 // set APN of mobile net works
		setApn(netName);
		
		switch (mApn) {
		case APN_CTWAP:
		case APN_UNIWAP:
		case APN_3GWAP:
		case APN_CMWAP:
			setNetProxy(httpRequest);
			break;
		case APN_WIFI:
			break;
		default:
			removetNetworkProxy(httpRequest);
			break;
		}
		boolean interrupt; // intercepted by gateway or not
		boolean needSetProxy; //  switch agents or not
        int tryCount = 0; // retry timer
        
        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        ReqResponce response = null;
		do{
			tryCount ++;
			interrupt = false;
			needSetProxy = false;
			boolean timeoutEx = false;
			Throwable throwable = null;
			if(catchException) {
				try {
					response = executeClient(httpClient, httpRequest);
				} catch (Exception e) {
					throwable = e;
					Log.e(TAG,"POST Exception= " + e + ",tryCount= " + tryCount);
					needSetProxy = ReqException.needSetNetProxy(e);
					timeoutEx = ReqException.isTimeout(e);
					if(response != null) {
						interrupt = response.isInterrupted() | timeoutEx;
					}
				} catch (Error e) {
					throwable = e;
					Log.e(TAG, "POST Error: " + e);
					e.printStackTrace();
				}
				if(response == null) {
					response = new ReqResponce();
				}
				response.setThrowable(throwable);
				response.setTimeouted(timeoutEx);
				response.setInterrupted(interrupt);
				ReqException.saveErrorLogToSD(throwable);
			} else {
				response = executeClient(httpClient, httpRequest);
				if(response != null) {
					interrupt = response.isInterrupted();
				}
			}
			if(debug) Log.d(TAG,"interrupt: " + interrupt + ", needSetProxy: " + needSetProxy + ", retryEnable: " + retryEnable);
			if(retryEnable && interrupt){
				if(tryCount >= retryCount) {
					Log.e(TAG,"REQUEST FAILED: tryCount= " + tryCount);
					break;	
				}else {
					Log.e(TAG,"interrupt tryCount= " + tryCount + ", retrying......");
					Thread.sleep(1000);
				}
			} else {
				if(debug) Log.d(TAG,"REQUEST OVER: " + ", tryCount: " + tryCount);
				break;
			}
			//httpRequest.abort();
		}while(true);
		// very important for release Context or Activity cite to recycle memory
		if(httpHeader != null) httpHeader.clear();
		httpHeader = null;
		return response;
	}
    
    /**
     * @hide
     * Create the default HTTP protocol parameters.
     */
	private HttpParams createHttpParams(int timeout) {
        final HttpParams params = new BasicHttpParams();
        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        HttpClientParams.setRedirecting(params, false);
        HttpConnectionParams.setSocketBufferSize(params, 8*1024);
        
        ConnManagerParams.setTimeout(params, timeout/2);
        ConnManagerParams.setMaxTotalConnections(params, 128); // 
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(32);
        ConnManagerParams.setMaxConnectionsPerRoute(params,connPerRoute);
        return params;
    }
    
    /**
     * @hide
     * */
	private String getMobileNetWorkType(Context context) {
        String networkType = null;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();// NULL

            if (networkInfo != null && networkInfo.isAvailable()) {
                String typeName = networkInfo.getTypeName(); // MOBILE/WIFI
                if (!"MOBILE".equalsIgnoreCase(typeName)) {
                    networkType = typeName;
                } else {
                    networkType = networkInfo.getExtraInfo(); // cmwap/cmnet/wifi/uniwap/uninet
                    if (networkType == null) {
                        networkType = typeName + "#[]";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(debug) Log.d(TAG, "net type name= " + networkType);
        return networkType;
    }
	
	public static boolean isHtmlPrefixJson(String json) {
		if(TextUtils.isEmpty(json)) return true;
		boolean isHtmlPrefix = json.startsWith("<html")
				|| json.startsWith("<Html")
				|| json.startsWith("<HTML");
		return isHtmlPrefix;
	}
}
