package com.image.cacheloader.api;

import android.graphics.Bitmap;

public interface ImageLoadCallback {
	public void onAsyncLoad(String url, Bitmap bitmap);
}
