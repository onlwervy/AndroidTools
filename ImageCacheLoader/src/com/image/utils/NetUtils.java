package com.image.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

public class NetUtils {

	private final static int TIMEOUT_CONNECTION = 15 * 1000;
	private final static int TIMEOUT_SOCKET = 15 * 1000;
	private final static int RETRY_TIME = 2;
	public static final String UTF_8 = "UTF-8";
	private static final String TAG = "NetUtils";

	public static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo;
	}

	public static boolean isNetConnected(Context context) {
		if (context != null) {
			NetworkInfo ni = getNetworkInfo(context);
			return ni != null && ni.isAvailable() && ni.isConnected();
		}
		return false;
	}
	
	/**
	 * get net type name: cmwap/cmnet/wifi/uniwap/uninet/ctnet/ctwap
	 * @param context
	 * @return
	 */
	public static String getNetWorkTypeName(Context context) {
        String networkType = null;
        try {
            NetworkInfo networkInfo = getNetworkInfo(context);// NULL
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
        return networkType;
    }

	public static Bitmap getNetBitmap(Context cxt, String url) throws NetException {
		return downloadNetBitmap(cxt, url);
	}

	public static InputStream downloadNetStream(Context cxt, String url) {
		if(!isNetConnected(cxt)) {
			return null;
		}
		InputStream input = null;
		int retryTimes = 0;
		do {
			try {
				URL mURL = new URL(url);
				HttpURLConnection con = (HttpURLConnection) mURL
						.openConnection();
				con.setDoInput(true);
				con.connect();
				input = con.getInputStream();
				int maxSize = con.getContentLength();
				Log.i(TAG, "maxSize== " + maxSize);
				return input;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (input == null) {
				retryTimes++;
				if (retryTimes < RETRY_TIME) {
					try {
						Thread.sleep(1000);
						Log.i(TAG, "downloadNetStream retryTimes= " + retryTimes);
					} catch (InterruptedException e1) {
					}
					continue;
				}
			}
		} while (retryTimes < RETRY_TIME);
		return input;
	}
	
	public static Bitmap downloadNetBitmap(Context cxt, String url) {
		Bitmap bitmap = null;
		InputStream inStream = null;
		try {
			inStream = downloadNetStream(cxt, url);
			if (inStream != null) {
				bitmap = BitmapFactory.decodeStream(inStream);
			}
		} catch (OutOfMemoryError e3) {
			e3.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	public static void saveStreamToFiles(Context context, InputStream is,
			String fileName) {
		if (is == null)
			return;
		byte[] buffer = new byte[1024];
		int len = -1;
		try {
			FileOutputStream bos = context.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			while ((len = is.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
				bos.flush();
			}
			if (bos != null) {
				bos.close();
			}
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap getHttpBitmap(String url) throws NetException {
		HttpClient httpClient = null;
		GetMethod httpGet = null;
		InputStream inStream = null;
		Bitmap bmp = null;
		int retryTimes = 0;
		try {
			do {
				try {
					httpClient = getHttpClient();
					httpGet = getHttpGet(url, null, null);
					int statusCode = httpClient.executeMethod(httpGet);
					if (statusCode != HttpStatus.SC_OK) {
						throw NetException.http(statusCode);
					}
					inStream = httpGet.getResponseBodyAsStream();
					bmp = BitmapFactory.decodeStream(inStream);
					inStream.close();
					break;
				} catch (OutOfMemoryError e3) {
					e3.printStackTrace();
				} catch (HttpException e) {
					e.printStackTrace();
					throw NetException.http(e);
				} catch (IOException e) {
					e.printStackTrace();
					throw NetException.network(e);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (bmp == null) {
					retryTimes++;
					if (retryTimes < RETRY_TIME) {
						try {
							Thread.sleep(1000);
							Log.w(TAG, "downloadBitmap retryTimes= " + retryTimes);
						} catch (InterruptedException e3) {
						}
						continue;
					}
				}
			} while (retryTimes < RETRY_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpGet != null) {
				httpGet.releaseConnection();
			}
			httpClient = null;
		}
		return bmp;
	}

	public static HttpClient getHttpClient() {
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(TIMEOUT_CONNECTION);
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout(TIMEOUT_SOCKET);
		httpClient.getParams().setContentCharset(UTF_8);
		return httpClient;
	}

	private static GetMethod getHttpGet(String url, String cookie,
			String userAgent) {
		GetMethod httpGet = new GetMethod(url);
		httpGet.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpGet.setRequestHeader("Connection", "Keep-Alive");
		httpGet.setRequestHeader("Cookie", cookie);
		httpGet.setRequestHeader("User-Agent", userAgent);
		return httpGet;
	}

	/**
	 * get file name according to url
	 * 
	 * @param url
	 * @return
	 */
	public static String getFileName(String url) {
		if (TextUtils.isEmpty(url))
			return "";
		return url.substring(url.lastIndexOf(File.separator) + 1);
	}

	/**
	 * Java文件操作 获取不带扩展名的文件名
	 * 
	 * @param fileNameOrPath
	 * @return
	 */
	public static String getFileNameNoEx(String fileNameOrPath) {
		if ((fileNameOrPath != null) && (fileNameOrPath.length() > 0)) {
			int dot = fileNameOrPath.lastIndexOf('.');
			if ((dot > -1) && (dot < (fileNameOrPath.length()))) {
				return fileNameOrPath.substring(0, dot);
			}
		}
		return fileNameOrPath;
	}
	
	/**
	 * get Network Type of ConnectivityManager
	 * 
	 * @return  TYPE_MOBILE = 0,  TYPE_WIFI = 1, TYPE_MOBILE_MMS = 2,  WAP 3 NET
	 * 
	 * 
	 * 	       public static final int TYPE_MOBILE
	 * 
	 *         Added in API level 1
	 * 
	 *         The Mobile data connection. When active, all data traffic will
	 *         use this network type's interface by default (it has a default
	 *         route)
	 * 
	 *         Constant Value: 0 (0x00000000)
	 *         
	 *         * 
	 *         public static final int TYPE_WIFI
	 * 
	 *         Added in API level 1
	 * 
	 * 
	 * 
	 *         The WIFI data connection. When active, all data traffic will use
	 *         this network type's interface by default (it has a default
	 *         route).
	 * 
	 *         Constant Value: 1 (0x00000001)
	 *         
	 *         public static final int TYPE_MOBILE_MMS
	 * 
	 *         Added in API level 8
	 * 
	 *         An MMS-specific Mobile data connection. This network type may use
	 *         the same network interface as TYPE_MOBILE or it may use a
	 *         different one. This is used by applications needing to talk to
	 *         the carrier's Multimedia Messaging Service servers.
	 * 
	 *         Constant Value: 2 (0x00000002)
	 * 
	 *         public static final int TYPE_BLUETOOTH
	 * 
	 *         Added in API level 13
	 * 
	 *         The Bluetooth data connection. When active, all data traffic will
	 *         use this network type's interface by default (it has a default
	 *         route).
	 * 
	 *         Constant Value: 7 (0x00000007)
	 * 
	 * 
	 *         public static final int TYPE_DUMMY
	 * 
	 *         Added in API level 14
	 * 
	 * 
	 * 
	 *         Dummy data connection. This should not be used on shipping
	 *         devices.
	 * 
	 *         Constant Value: 8 (0x00000008)
	 * 
	 * 
	 *         public static final int TYPE_ETHERNET
	 * 
	 *         Added in API level 13
	 * 
	 * 
	 * 
	 *         The Ethernet data connection. When active, all data traffic will
	 *         use this network type's interface by default (it has a default
	 *         route).
	 * 
	 *         Constant Value: 9 (0x00000009)
	 * 
	 * 
	 *         public static final int TYPE_MOBILE_DUN
	 * 
	 *         Added in API level 8
	 * 
	 * 
	 * 
	 *         A DUN-specific Mobile data connection. This network type may use
	 *         the same network interface as TYPE_MOBILE or it may use a
	 *         different one. This is sometimes by the system when setting up an
	 *         upstream connection for tethering so that the carrier is aware of
	 *         DUN traffic.
	 * 
	 *         Constant Value: 4 (0x00000004)
	 * 
	 * 
	 *         public static final int TYPE_MOBILE_HIPRI
	 * 
	 *         Added in API level 8
	 * 
	 * 
	 * 
	 *         A High Priority Mobile data connection. This network type uses
	 *         the same network interface as TYPE_MOBILE but the routing setup
	 *         is different. Only requesting processes will have access to the
	 *         Mobile DNS servers and only IP's explicitly requested via
	 *         requestRouteToHost(int, int) will route over this interface if no
	 *         default route exists.
	 * 
	 *         Constant Value: 5 (0x00000005)
	 * 
	 * 
	 * 
	 *         public static final int TYPE_MOBILE_SUPL
	 * 
	 *         Added in API level 8
	 * 
	 * 
	 * 
	 *         A SUPL-specific Mobile data connection. This network type may use
	 *         the same network interface as TYPE_MOBILE or it may use a
	 *         different one. This is used by applications needing to talk to
	 *         the carrier's Secure User Plane Location servers for help
	 *         locating the device.
	 * 
	 *         Constant Value: 3 (0x00000003)
	 * 
	 * 
	 *         public static final int TYPE_WIMAX
	 * 
	 *         Added in API level 8
	 * 
	 * 
	 * 
	 *         The WiMAX data connection. When active, all data traffic will use
	 *         this network type's interface by default (it has a default
	 *         route).
	 * 
	 *         Constant Value: 6 (0x00000006)
	 */
	public static int getType(Context context) {
		int netType = -1;
		NetworkInfo networkInfo = getNetworkInfo(context);
		if (networkInfo == null) {
			return netType;
		}
		netType = networkInfo.getType();
		return netType;
	}
	
	public static boolean isMobileDataMode(Context context) {
		return getType(context) == ConnectivityManager.TYPE_MOBILE;
	}
}
