package com.image.cacheloader.ui;

import com.image.cacheloader.CacheFactory;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class CacheApp extends Application {
	CacheApp app;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Context cxt = getApplicationContext();
		Log.i("hulk", "onCreate: " + cxt);
		CacheFactory.init(cxt);
	}

	
}
