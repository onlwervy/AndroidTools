package com.image.cacheloader;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

public class ImageLoader extends BitmapLoader {

    static ImageLoader instance;

    public static ImageLoader getInstance(Context context, int cacheCapacity) {
        if(instance == null) {
            instance = new ImageLoader(context, cacheCapacity, CacheFactory.DEF_THREAD_POOL_SIZE);
        }
        return instance;
    }

	protected ImageLoader(Context context) {
		super(context, CacheFactory.DEF_CACHE_CAPACITY, CacheFactory.DEF_THREAD_POOL_SIZE);
	}

	protected ImageLoader(Context context, int cacheSize, int threadPoolSize) {
        super(context, cacheSize, threadPoolSize);
    }
	
	public Bitmap getdefaultBitmap() {
		return defaultBitmap;
	}

	public void setDefaultImg(int resId) {
		setDefaultBitmap(resId);
	}

	public void setDefaultImg(Bitmap defaultImg) {
		this.defaultBitmap = defaultImg;
	}

	public void loadImage(ImageView imageView, String url) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadImage(ImageView imageView, String url, boolean isLoadPrompt) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadImage(ImageView imageView, String url, Bitmap defaultBitmap) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}

	public void loadImage(ImageView imageView, String url, int defaultResId) {
        loadBitmap(context, imageView, url, FIGURE_NO_FLAG, getCacheResBmp(defaultResId), false, null);
    }
	
	public void loadImage(ImageView imageView, String url, int figureFlag, int defResId) {
		loadBitmap(context, imageView, url, figureFlag, getCacheResBmp(defResId), false, null);
	}
	
	public void loadBitmap(final ImageView imageView, final String url) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadBitmapForExternal(final String url, final Handler handler) {
		Bitmap bitmap = getCacheBitmap(context, url);
		if (bitmap != null) {
			sendToTarget(handler, REQUEST_NET_RESULT_CODE, bitmap);
		} else {
			downlodNetOrLoadBitmap(context, url, handler);
		}
	}
}
