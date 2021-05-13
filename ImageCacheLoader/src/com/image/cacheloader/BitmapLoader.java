package com.image.cacheloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.image.cacheloader.api.BitmapCacheAPI;
import com.image.cacheloader.api.ImageLoadCallback;
import com.image.utils.BitmapUtil;
import com.image.utils.CacheUtils;
import com.image.utils.ImageUtils;
import com.image.utils.NetUtils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;


public class BitmapLoader extends BitmapUtil {
	
	public final static int REQUEST_NET_RESULT_CODE = -111;
	public final static int LOAD_IMAGE_RESULT_CODE = -222;
	private static final String TAG = "BitmapLoader";
	public Context context;
	private BitmapCacheAPI cache;
	private ExecutorService pool;
	public Bitmap defaultBitmap = null;
	boolean debug = CacheFactory.isDebug();
	
	Handler mHandler = new Handler();
	
	public BitmapLoader(Context context, int cacheCapacity, int poolSize) {
		this.context = context;
		cache = CacheFactory.createBitmapCacheAPI(cacheCapacity);
		pool = Executors.newFixedThreadPool(poolSize);
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	public BitmapCacheAPI getCache() {
		return cache;
	}

	public void setCache(BitmapCacheAPI cache) {
		this.cache = cache;
	}
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setCacheCapacity(int capacity) {
		this.cache.setCapacity(capacity);
	}
	
	public void clearCache() {
		this.cache.clearCache();
	}

	public Bitmap getDefaultBitmap() {
		return defaultBitmap;
	}

	public void setDefaultBitmap(int resId) {
		this.defaultBitmap = getCacheResBmp(resId);
	}

	public void setDefaultBitmap(Bitmap defaultBmp) {
		this.defaultBitmap = defaultBmp;
	}

	public boolean isMobileDataMode(Context context) {
		return NetUtils.isMobileDataMode(context);
	}

	public Bitmap getCacheResBmp(int resId) {
		if(cache == null) return null;
		return cache.getCacheBitmap(context, resId);
	}
	
	public Bitmap getCacheBmp(String url) {
		return cache.getBitmap(url);
	}

	public Bitmap getCacheBitmap(Context context, String url) {
		if(TextUtils.isEmpty(url)) return null;
		Bitmap bitmap = cache.getBitmap(url);
		if (bitmap == null) {
			String fileName = NetUtils.getFileName(url);
			bitmap = CacheUtils.getCacheImage(context, fileName);
		}
		if (debug && bitmap == null) {
			Log.i(TAG, "local bitmap is null url: " + url);
		}
		return bitmap;
	}

	/**
	 *  post cache image if exist, or click to download net image by user in mobile data mode
	 * @param context
	 * @param url
	 * @param callback  post cache bitmap if exist, or bitmap is null  in mobile data mode
	 */
	public void loadBmpIntelligent( String url, ImageLoadCallback callback) {
		boolean mobileData = isMobileDataMode(context);
		loadBitmap(context, null, url, FIGURE_NO_FLAG, defaultBitmap, mobileData, callback);
	}

	/**
	 * load cache image if exist, or click to download net image by user in mobile data mode
	 * @param context
	 * @param imageView
	 * @param url
	 */
	public void loadBmpIntelligent(ImageView imageView, String url) {
		boolean mobileData = isMobileDataMode(context);
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, mobileData, null);
	}

	/**
	 *   post cache image if exist, or click to download net image by user in mobile data mode
	 * @param context
	 * @param url
	 * @param defBitmap
	 * @param callback  post cache bitmap if exist, or bitmap is null  in mobile data mode
	 */
	public void loadBmpIntelligent(String url, Bitmap defBitmap, ImageLoadCallback callback) {
		boolean mobileData = isMobileDataMode(context);
		loadBitmap(context, null, url, FIGURE_NO_FLAG, defBitmap, mobileData, callback);
	}

	public void loadBitmap(String url, ImageLoadCallback callback) {
		loadBitmap(context, null, url, FIGURE_NO_FLAG, defaultBitmap, false, callback);
	}
	
	public void loadBitmap(String url, ImageLoadCallback callback, boolean isCacheMode) {
		loadBitmap(context, null, url, FIGURE_NO_FLAG, defaultBitmap, isCacheMode, callback);
	}
	
	public void loadBitmap(String url, Bitmap defBmp, boolean isCacheMode, ImageLoadCallback callback) {
		loadBitmap(context, null, url, FIGURE_NO_FLAG, defBmp, isCacheMode, callback);
	}
	
	public void loadBitmap(String url, int defBmpRasid, boolean isCacheMode, ImageLoadCallback callback) {
		Bitmap defBmp = getBitmap(context, defBmpRasid);
		loadBitmap(context, null, url, FIGURE_NO_FLAG, defBmp, false, callback);
	}

	public void loadBitmap(ImageView imageView, String url, Bitmap defBitmap, boolean isCacheMode) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defBitmap, isCacheMode, null);
	}
	
	/**
	 * load bitmap
	 * @param context
	 * @param imageView
	 * @param url
	 * @param figureFlag the image figure refer to:
     * <p>public static final int FIGURE_NO_FLAG = -1;
     * <p>public static final int FIGURE_ROUNDED_FLAG = 1;
     * <p>public static final int FIGURE_ROUND_RECT_FLAG = 2;
     * <p>public static final int FIGURE_OVAL_FLAG = 3;
     * <p>public static final int FIGURE_TRIANGLE_FLAG = 4;
     * <p>public static final int FIGURE_PENTAGON_FLAG = 5;
     * <p>public static final int FIGURE_DRAW_PATH_FLAG = 6;
	 * @param isLoadPrompt
	 * @param isNoNetPrompt
	 * @param defBitmap
	 * @param isCacheMode
	 * @param callback
	 */
	public synchronized void loadBitmap(Context context, final ImageView imageView, String url,
			int figureFlag, Bitmap defBitmap, boolean isCacheMode, ImageLoadCallback callback) {
		loadBitmap(context, imageView, url, figureFlag, -1, -1, defBitmap, isCacheMode, callback);
	}

	/**
	 * 
	 * @param context
	 * @param imageView
	 * @param url
	 * @param figureFlag the image figure refer to:
     * <p>public static final int FIGURE_NO_FLAG = -1;
     * <p>public static final int FIGURE_ROUNDED_FLAG = 1;
     * <p>public static final int FIGURE_ROUND_RECT_FLAG = 2;
     * <p>public static final int FIGURE_OVAL_FLAG = 3;
     * <p>public static final int FIGURE_TRIANGLE_FLAG = 4;
     * <p>public static final int FIGURE_PENTAGON_FLAG = 5;
     * <p>public static final int FIGURE_DRAW_PATH_FLAG = 6;
	 * @param width
	 * @param height
	 * @param isLoadPrompt
	 * @param isNoNetPrompt
	 * @param defBitmap
	 * @param isCacheMode
	 * @param callback
	 */
	public void loadBitmap(Context context, ImageView imageView, String url,
			int figureFlag, int width, int height,  Bitmap defBitmap, 
			boolean isCacheMode, ImageLoadCallback callback) {
		Bitmap cacheBmp = getCacheBitmap(context, url);
		if(isCacheMode) {
			//return result whether bitmap is null or not
			handleResultBitmap(context, imageView, cacheBmp, url, figureFlag, -1, -1, false, callback);
		} else if(cacheBmp != null) {
			handleResultBitmap(context, imageView, cacheBmp, url, figureFlag, -1, -1, false, callback);
		} else {
			handleResultBitmap(context, imageView, defBitmap, url, figureFlag, -1, -1, false, callback);
			//start download net bitmap
			startDownLoadNetBmp(context, imageView, url, figureFlag, width, height, callback);
		}
	}
	
	public void startDownLoadNetBmp(Context context, ImageView imageView, String url, 
			int figureFlag, ImageLoadCallback callback) {
		startDownLoadNetBmp(context, imageView, url, figureFlag, -1, -1, callback);
	}
	
	public void startDownLoadNetBmp(final Context context, final ImageView imageView, final String url, 
			final int figureFlag, final int width, final int height, final ImageLoadCallback callback) {
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				final Bitmap bitmap = (Bitmap) msg.obj;
				mHandler.post(new Runnable() {
					public void run() {
						handleResultBitmap(context, imageView, bitmap, url, figureFlag, width, height, true, callback);
					}
				});
			}
		};
		if(debug) Log.i(TAG, "start download bitmap url: " + url);
		downlodNetOrLoadBitmap(context, url, handler);
	}
	
	/**
	 * download image bitmap from net, and add to cache ,files
	 * @param context
	 * @param url
	 * @param handler
	 * @param isLoadPrompt
	 * @param isNoNetPrompt
	 */
	public void downlodNetOrLoadBitmap(final Context context, final String url, final Handler handler) {
		if(TextUtils.isEmpty(url) || !NetUtils.isNetConnected(context)) {
			Log.e(TAG, "url is null, or no net work !! ");
			return;
		}
		pool.execute(new Runnable() {
			public void run() {
				Bitmap bitmap = null;
				if(debug) Log.i(TAG, "executing url or path: " + url);
				if(url.startsWith("http") || url.startsWith("https")) {
					bitmap = downloadBitmap(context, url);
				} else {
					String path = url;
					bitmap = loadLocalBitmap(path);
				}
				sendToTarget(handler, REQUEST_NET_RESULT_CODE, bitmap);
			}
		});
	}
	
	public Bitmap cacheBitmap(Bitmap bitmap, String url, int figureFlag, int width, int height) {
		Bitmap bmp = null;
		if(width > 0 && height > 0) {
			bmp = ImageUtils.scaleBitmap(bitmap, figureFlag, width, height);
		} else {
			bmp = bitmap;
		}
		if(figureFlag != FIGURE_NO_FLAG) {
			bmp = drawFigureBitmap(bmp, figureFlag);
		}
		if(bmp != null) {
			saveCacheBitmap(url, bmp);
			cache.addBitmap(url, bmp);
		} else {
			Log.w(TAG, "cache bitmap failed, url: " + url);
		}
		return bmp;
	}
	
	public void saveCacheBitmap(String url, Bitmap bitmap){
		if (bitmap == null) return;
		String fileName = NetUtils.getFileName(url);
		String path = CacheUtils.saveCacheImage(context, bitmap, fileName, 100);
		if(debug) Log.d(TAG, "save files name: " + fileName + ", Path: " + path);
	}

	public void handleResultBitmap(Context context, final ImageView imageView, Bitmap bitmap, String url, int figureFlag, 
			final int width, final int height, boolean cacheEnable,  ImageLoadCallback callback) {
		if(cacheEnable) {
			bitmap = cacheBitmap(bitmap, url, figureFlag, width, height);
		}
		final Bitmap bmp = bitmap;
		if(imageView != null) {
			mHandler.post(new Runnable() {
				public void run() {
					if(bmp != null) {
						imageView.setImageBitmap(bmp);
					} else if(defaultBitmap != null) {
						imageView.setImageBitmap(defaultBitmap);
					}
				}
			});
		}
		if(callback != null) {
			callback.onAsyncLoad(url, bmp);
		}
	}
	
	public void sendToTarget(Handler handler, int reqCode, Object obj) {
		Message msg = handler.obtainMessage(reqCode);
		msg.obj = obj;
		msg.sendToTarget();
	}
	
	public ProgressDialog showProgressDlg(final Context context,
			final String title, final String message,
			final boolean indeterminate, final boolean cancelable,
			final OnCancelListener lCancel) {
		if (context instanceof Activity && ((Activity) context).isFinishing()) {
			return null;
		}
		try {
			return ProgressDialog.show(context, title, message, indeterminate,
					cancelable, lCancel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
