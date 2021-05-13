package com.http.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

/**
 * 网络请求异常类：用于捕获异常和提示错误信�?
 * 
 * @author hulk
 * @version 1.2
 * @created 2014-3-7
 */
public class ReqException extends Exception {
	private static final long serialVersionUID = 1L;
	// 是否保存错误日志
	private final static boolean Debug = false;

	/** 定义异常类型 */
	public final static byte TYPE_UNSPECIAL = 0x00;
	public final static byte TYPE_NETWORK = 0x01;
	public final static byte TYPE_SOCKET = 0x02;
	public final static byte TYPE_HTTP_CODE = 0x03;
	public final static byte TYPE_HTTP_ERROR = 0x04;
	public final static byte TYPE_XML = 0x05;
	public final static byte TYPE_IO = 0x06;
	public final static byte TYPE_RUN = 0x07;
	public final static byte TYPE_CONNECT = 0x08;

	private byte type;
	private int code;

	private ReqException(byte type, int code, Exception excp) {
		super(excp);
		this.type = type;
		this.code = code;
		if (Debug) {
			saveErrorLogToSD(excp);
		}
	}

	@Override
	public String toString() {
		return "QueueException [type=" + type + ", code=" + code + "]";
	}

	public int getCode() {
		return this.code;
	}

	public int getType() {
		return this.type;
	}

	public static boolean isDebug() {
		return Debug;
	}

	/**
	 * 提示友好的错误信�?
	 * 
	 * @param ctx
	 */
	public void makeToast(Context ctx) {
		switch (this.getType()) {
		case TYPE_HTTP_CODE:
			String err = "http status code_error: " + this.getCode();
			Toast.makeText(ctx, err, Toast.LENGTH_SHORT).show();
			break;
		case TYPE_HTTP_ERROR:
			Toast.makeText(ctx, "http throwable error", Toast.LENGTH_SHORT)
					.show();
			break;
		case TYPE_SOCKET:
			Toast.makeText(ctx, "socket throwable error", Toast.LENGTH_SHORT)
					.show();
			break;
		case TYPE_NETWORK:
			Toast.makeText(ctx, "network not connected", Toast.LENGTH_SHORT)
					.show();
			break;
		case TYPE_XML:
			Toast.makeText(ctx, "xml parser failed", Toast.LENGTH_SHORT).show();
			break;
		case TYPE_IO:
			Toast.makeText(ctx, "io throwable error", Toast.LENGTH_SHORT)
					.show();
			break;
		case TYPE_RUN:
			Toast.makeText(ctx, "app run code error", Toast.LENGTH_SHORT)
					.show();
			break;
		}
	}

	public int getExType() {
		return parseType(this);
	}
	
	public static int parseType(Exception e) {
		if(e == null) return TYPE_UNSPECIAL;
		int type = TYPE_UNSPECIAL;
		if (e instanceof UnknownHostException) {
			return TYPE_NETWORK;
		} else if (e instanceof IOException) {
			return TYPE_IO;
		} else if (e instanceof SocketException || e instanceof SocketTimeoutException) {
			return TYPE_SOCKET;
		} else if (e instanceof ConnectionPoolTimeoutException 
				|| e instanceof ConnectTimeoutException || e instanceof ConnectException) {
			return TYPE_CONNECT;
		}
		return type;
	}
	
	public static boolean isTimeout(Exception e) {
		if (e instanceof SocketException || e instanceof SocketTimeoutException
				|| e instanceof ConnectionPoolTimeoutException 
				|| e instanceof ConnectTimeoutException || e instanceof ConnectException) {
			return true;
		}
		return false;
	}
	
	public static boolean needSetNetProxy(Exception e) {
		if (e instanceof UnknownHostException || e instanceof IOException
				|| e instanceof Exception) {
			return true;
		}
		return false;
	}

	/**
	 * 保存异常日志: sdcard/slim/Log/
	 * 
	 * @param excp
	 */
	public static void saveErrorLogToSD(Throwable excp) {
		if(excp == null) return;
		String errorlog = "errorlog.txt";
		String savePath = "";
		String logFilePath = "";
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			// 判断是否挂载了SD�?
			String storageState = Environment.getExternalStorageState();
			if (storageState.equals(Environment.MEDIA_MOUNTED)) {
				savePath = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + "/slim/Log/";
				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				logFilePath = savePath + errorlog;
			}
			// 没有挂载SD卡，无法写文�?
			if (logFilePath == "") {
				return;
			}
			File logFile = new File(logFilePath);
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			fw = new FileWriter(logFile, true);
			pw = new PrintWriter(fw);
			pw.println("--------------------" + (new Date().toLocaleString())
					+ "---------------------");
			excp.printStackTrace(pw);
			pw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static ReqException http(int code) {
		return new ReqException(TYPE_HTTP_CODE, code, null);
	}

	public static ReqException http(Exception e) {
		return new ReqException(TYPE_HTTP_ERROR, 0, e);
	}

	public static ReqException socket(Exception e) {
		return new ReqException(TYPE_SOCKET, 0, e);
	}

	public static ReqException io(Exception e) {
		if (e instanceof UnknownHostException || e instanceof ConnectException) {
			return new ReqException(TYPE_NETWORK, 0, e);
		} else if (e instanceof IOException) {
			return new ReqException(TYPE_IO, 0, e);
		}
		return run(e);
	}

	public static ReqException xml(Exception e) {
		return new ReqException(TYPE_XML, 0, e);
	}

	public static ReqException network(Exception e) {
		if (e instanceof UnknownHostException || e instanceof ConnectException) {
			return new ReqException(TYPE_NETWORK, 0, e);
		} else if (e instanceof ReqException) {
			return http(e);
		} else if (e instanceof SocketException) {
			return socket(e);
		}
		return http(e);
	}

	public static ReqException run(Exception e) {
		return new ReqException(TYPE_RUN, 0, e);
	}

}
