package com.image.cacheloader;

import com.image.cacheloader.api.ImageLoadCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * usd for manage all icon or picture of drawable
 * 
 * @author Hulk
 * 
 */
public class IconLoader extends BitmapLoader {

	public static final String TAG = "IconLoader";
	public final static int REQUEST_NET_RESULT_CODE = 0x11;
	static IconLoader instance;

	public static IconLoader getInstance(Context context, int cacheCapacity) {
	    if(instance == null) {
	        instance = new IconLoader(context, cacheCapacity, CacheFactory.DEF_THREAD_POOL_SIZE);
	    }
        return instance;
    }

	protected IconLoader(Context context) {
		super(context, CacheFactory.DEF_CACHE_CAPACITY, CacheFactory.DEF_THREAD_POOL_SIZE);
	}

	protected IconLoader(Context context, int cacheCapacity, int poolSize) {
        super(context, cacheCapacity, poolSize);
    }

	public Bitmap getDefaultIcon() {
		return defaultBitmap;
	}

	public void setDefaultIcon(Bitmap defaultIcon) {
		this.defaultBitmap = defaultIcon;
	}

	public void setDefaultIcon(int resId) {
		setDefaultBitmap(resId);
	}

	public void loadIcon(ImageView imageView, String url) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadIcon(final String url, ImageLoadCallback callback) {
		loadBitmap(url, callback);
	}
	
	public void loadIcon(ImageView imageView, String url, int figureFlag) {
		loadBitmap(context, imageView, url, figureFlag, defaultBitmap, false, null);
	}

	/**
	 * load Icon
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
	 * @param defResId
	 */
	public void loadIcon(ImageView imageView, String url, int figureFlag, int defResId) {
		loadBitmap(context, imageView, url, figureFlag, getBitmap(context, defResId), false, null);
	}
}
