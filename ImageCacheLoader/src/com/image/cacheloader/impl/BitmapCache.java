package com.image.cacheloader.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import com.image.cacheloader.api.BitmapCacheAPI;
import com.image.cacheloader.entity.BitmapEntity;
import com.image.utils.BitmapUtil;



import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class BitmapCache extends CacheImpl<BitmapEntity> implements
		BitmapCacheAPI {

	private static final String TAG = "ImageCache";
	private ReferenceQueue<Bitmap> mQueue;
	
	public BitmapCache() {
		super();
	}
	
	public BitmapCache(int capacity) {
		mQueue = new ReferenceQueue<Bitmap>();
		setCapacity(capacity);
	}

	public void addBitmap(Object key, Bitmap bmp) {
		BitmapEntity entity = new BitmapEntity();
		entity.setKey(key);
		BitmapSoftRef ref = new BitmapSoftRef(key, bmp, mQueue);
		entity.setBitmapRef(ref);
		add(key, entity);
    }

	public Bitmap getBitmapByKey(Object key) {
		Bitmap bmp = null;
		if (key != null) {
			BitmapEntity en = get(key);
			if(en != null) {
				BitmapSoftRef ref = en.getBitmapRef();
				if(ref != null) {
					bmp = ref.get();
				}
			}
		}
		return bmp;
	}
	
	public Bitmap getBitmap(String url) {
		return getBitmapByKey(url);
	}

	public Bitmap getCacheBitmap(Context context, int resId) {
		String key = String.valueOf(resId);
		Bitmap bmp = getBitmapByKey(key);
		if (bmp == null) {
			try {
				bmp = BitmapUtil.getBitmap(context, resId);
				if(bmp != null) {
					addBitmap(key, bmp);
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bmp;
	}

	@Override
	public void recycle(BitmapEntity data) {
		//Bitmap bm= data.getBitmapRef().get();
		//data.recycle(bm);
		if(debugMode) {
			Log.i(TAG, "recycle data : " + data + ", count = " + getCount());
		}
		remove(data);
		data = null;
	}

	@Override
	public void cleanCache() {
		//super.cleanCache();
		BitmapSoftRef ref = null;
        while ((ref = (BitmapSoftRef) mQueue.poll()) != null) {
           removeByKey(ref._key);
        }
	}

	public class BitmapSoftRef extends SoftReference<Bitmap> {
		public Object _key = 0;
        public BitmapSoftRef(Object key, Bitmap bmp, ReferenceQueue<Bitmap> q) {
            super(bmp, q);
            _key = key;
        }
    }

	@Override
	public void addCacheBitmap(Object key, Bitmap bmp) {
		addBitmap(key, bmp);
	}
}
