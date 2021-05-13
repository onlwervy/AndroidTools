package com.image.cacheloader.entity;

import com.image.cacheloader.impl.BitmapCache.BitmapSoftRef;

import android.graphics.Bitmap;

public class BitmapEntity extends CacheBase {

	BitmapSoftRef bitmapRef = null;

	@Override
	public String toString() {
		return super.toString() + ", bitmapRef= " + bitmapRef;
	}

	public BitmapSoftRef getBitmapRef() {
		return bitmapRef;
	}

	public void setBitmapRef(BitmapSoftRef bitmap) {
		this.bitmapRef = bitmap;
	}
	
	public void recycle(Bitmap bitmap) {
		if(bitmap != null) {
			bitmap.recycle();
		}
		setKey(null);
	}
}
