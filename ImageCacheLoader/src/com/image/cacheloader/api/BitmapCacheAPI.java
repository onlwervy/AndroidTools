package com.image.cacheloader.api;

import com.image.cacheloader.entity.BitmapEntity;

import android.content.Context;
import android.graphics.Bitmap;

public interface BitmapCacheAPI extends CacheAPI<BitmapEntity> {

	void addBitmap(Object key, Bitmap bmp);

	Bitmap getBitmapByKey(Object key);

	Bitmap getBitmap(String url);

	void addCacheBitmap(Object key, Bitmap bmp);

	Bitmap getCacheBitmap(Context context, int resId);
}
