package com.http.helper.ui;

import com.http.helper.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	protected static final String TAG = "MainActivity";
	private static final int NOTIFICATION_ID = 0x12;
	String url = "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1508/14/c0/11195389_1439563888459_800x600.jpg";
	
	Button mStartBtn;
	ImageView img;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mStartBtn = (Button) findViewById(R.id.start_btn);
		img = (ImageView) findViewById(R.id.img);
		mStartBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}



	public void setImg(final String filePath) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*Matrix matrix = new Matrix();
				matrix.setScale(1.5f, 1.5f);
				img.setImageMatrix(matrix);*/
				Bitmap bm = BitmapFactory.decodeFile(filePath);
				img.setImageBitmap(bm);
			}
		});
	}
}
