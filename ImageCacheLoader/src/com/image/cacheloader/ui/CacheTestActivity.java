package com.image.cacheloader.ui;

import java.util.Date;


import com.image.cacheloader.CacheFactory;
import com.image.cacheloader.IconLoader;
import com.image.cacheloader.ImageLoader;
import com.image.cacheloader.api.ImageLoadCallback;
import com.image.utils.CacheUtils;
import com.image.utils.NetException;
import com.image.utils.NetUtils;
import com.slim.cache.R;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CacheTestActivity extends Activity {

	private String TAG = "CacheTestActivity";
	ImageView img_view;
	TextView urlTv;
	Button loadBtn;
	Handler handler = new Handler();

	// avatar:
	// http://slimup.oss.aliyuncs.com/avatar/20131006/14ed82ae0b51deb1a8b151b8929ef767.jpg

	String testUrl = "http://slimup.oss.aliyuncs.com/avatar/20131006/14ed82ae0b51deb1a8b151b8929ef767.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cache_test);
		img_view = (ImageView) findViewById(R.id.img_view);
		urlTv = (TextView) findViewById(R.id.url_tv);
		loadBtn = (Button) findViewById(R.id.load_btn);
		urlTv.setText(testUrl);
		loadBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//downloadImg();
				loadImg();
			}
		});
	}
	
	private void loadImg() {
		ImageLoadCallback callback = new ImageLoadCallback() {
			
			@Override
			public void onAsyncLoad(String url, Bitmap bitmap) {
				img_view.setImageBitmap(bitmap);
				String path = CacheUtils.savePathImageToSD(bitmap, url, 100);
				Log.i(TAG, "path: " + path);
			}
		};
		//ImageLoader mg = CacheFactory.createImageManager(getApplicationContext());
		IconLoader mg = CacheFactory.getIconManager();
		mg.loadBitmap(testUrl, callback);
		//ImageLoader.get(getApplicationContext()).loadImage(getApplicationContext(), img_view, testUrl);
	}

	void downloadImg() {;
		loadBtn.setEnabled(false);
		final Date date1 = new Date();
		urlTv.setText(testUrl + "\n\n start time: " + date1.toLocaleString());
		new Thread(new Runnable() {
			public void run() {
				try {
					//final Bitmap img = NetUtils.getNetBitmap(testUrl);
					final Bitmap img = NetUtils.getHttpBitmap(testUrl);
					handler.post(new Runnable() {
						public void run() {
							if (img != null) {
								img_view.setImageBitmap(img);
								loadBtn.setEnabled(true);
								Date date2 = new Date();
								long duration = date2.getTime() - date1.getTime();
								urlTv.setText(urlTv.getText().toString() 
										+ ",\n end time: " + date2.toLocaleString() 
										+ "\nduration (ms) = " + duration);
							} else {
								Log.e(TAG, "img bitmap is null !!");
							}
						}
					});
				} catch (NetException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}
}
