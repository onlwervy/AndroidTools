package com.image.cacheloader;

import com.image.cacheloader.api.BitmapCacheAPI;
import com.image.cacheloader.api.CacheAPI;
import com.image.cacheloader.entity.CacheBase;
import com.image.cacheloader.impl.BitmapCache;
import com.image.cacheloader.impl.CacheImpl;

import android.content.Context;


/**
 * bitmap factory,must init by application context
 * @author hao
 *
 */
public class CacheFactory {

	public static final int TYPE_GENERIC = -1;
	public static final int TYPE_BITMAP = 0;
	
	public static final int DEF_CACHE_CAPACITY = 8;
	public static final int DEF_THREAD_POOL_SIZE = 4;

	static IconLoader iconLoader;
	static ImageLoader imageLoader;
	static Context context;
	
	private static boolean debug = false;
	private static boolean autoClearOldCache = true;

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		CacheFactory.debug = debug;
	}

	public static boolean isAutoClearOldCache() {
		return autoClearOldCache;
	}

	public static void setAutoClearOldCache(boolean autoClearOldCache) {
		CacheFactory.autoClearOldCache = autoClearOldCache;
	}

	/**
	 * * create Bitmap GenericC api.
	 * 
	 * @param capacity
	 *            new a cache which capacity is 10
	 * @param capacity
	 * @return
	 */
	public static CacheAPI<CacheBase> createGenericCacheAPI(int capacity) {
		CacheImpl<CacheBase> impl = new CacheImpl<CacheBase>();
		if (capacity > 0) {
			impl.setCapacity(capacity);
		}
		return impl;
	}

	/**
	 * create Bitmap Cache pai.
	 * 
	 * @param capacity
	 *            new a cache which capacity is 10
	 * @return
	 */
	public static BitmapCacheAPI createBitmapCacheAPI(int capacity) {
		if (capacity > 0) {
			return new BitmapCache(capacity);
		} else {
			return new BitmapCache();
		}
	}
	
	public static IconLoader createIconManager(Context context) {
		return new IconLoader(context);
	}

	public static ImageLoader createImageManager(Context context) {
		return new ImageLoader(context);
	}

	public static void init(Context context) {
		CacheFactory.context = context;
		iconLoader = new IconLoader(CacheFactory.context);
		imageLoader = new ImageLoader(CacheFactory.context);
	}

	public static void recycle() {
		if(iconLoader != null) {
			iconLoader.clearCache();
			iconLoader = null;
		}
		if(imageLoader != null) {
			imageLoader.clearCache();
			imageLoader = null;
		}
	}

	public Context getContext() {
		return context;
	}

	public static IconLoader getIconManager() {
		if(iconLoader == null) {
			iconLoader = new IconLoader(CacheFactory.context);
		}
		return iconLoader;
	}

	public static ImageLoader getImageManager() {
		if(imageLoader == null) {
			imageLoader = new ImageLoader(CacheFactory.context);
		}
		return imageLoader;
	}
}
