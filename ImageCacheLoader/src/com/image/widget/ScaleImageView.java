package com.image.widget;

import com.image.utils.ImageUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * 图片放缩ImageView, 可设置放缩比例
 * @author hulk
 *
 */
public class ScaleImageView extends ImageView {

	private static final String TAG = "ScaleImageView";
	int sacaleWidth = 0;
	int scaleHeight = 0;
	float scale = 1;
	Bitmap scaledBitmap;
	private boolean debug = true;

	public ScaleImageView(Context context) {
		super(context);
	}

	public ScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		sacaleWidth = this.getWidth();
		scaleHeight = this.getHeight();
		if(debug)Log.d(TAG, "onDraw width= " + sacaleWidth + ", height= " + scaleHeight);
		super.onDraw(canvas);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if(debug)Log.d(TAG, "onFinishInflate width= " + sacaleWidth + ", height= " + scaleHeight);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		sacaleWidth = MeasureSpec.getSize(widthMeasureSpec);
		scaleHeight = MeasureSpec.getSize(heightMeasureSpec);
		if(debug)Log.d(TAG, "onMeasure width= " + sacaleWidth + ", height= " + scaleHeight);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void setImageBitmap(Bitmap bmp) {
		if(bmp == null) throw new IllegalArgumentException("Bitmap cannot null !! ");
		scale = getScaleValue(bmp);
		if(scale > 0 && scale != 1) {
			scaledBitmap = ImageUtils.scaleEtRatioBitmap(bmp, scale);
		} else {
			scaledBitmap = bmp;
		}
		super.setImageBitmap(scaledBitmap);
	}

	/**
	 * resId must one image resource, cannot be selector
	 */
	@Override
	public void setImageResource(int resId) {
		if(resId <= 0) {
			super.setImageBitmap(null);
		} else {
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId);
			scale = getScaleValue(bmp);
			if(scale > 0 && scale != 1) {
				scaledBitmap = ImageUtils.scaleEtRatioBitmap(bmp, scale);
			} else {
				scaledBitmap = bmp;
			}
			super.setImageBitmap(scaledBitmap);
		}
	}

	private float getScaleValue(Bitmap bmp) {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		float wScale = (float) sacaleWidth / bmpWidth;
		float hScale = (float) scaleHeight / bmpHeight;
		float scale = wScale > hScale ? wScale : hScale;
		if(debug)Log.d(TAG, "scale= " + scale + ", width= " + sacaleWidth + ", height= " + scaleHeight
				+ ", bmpWidth= " + bmpWidth + ", bmpHeight= " + bmpHeight);
		return scale;
	}

	public int getMeasureWidth() {
		return sacaleWidth;
	}

	public int getMeasureHeight() {
		return scaleHeight;
	}

	public float getScale() {
		return scale;
	}

	public Bitmap getScaledBitmap() {
		return scaledBitmap;
	}

	public void setScaledBitmap(Bitmap scaledBitmap) {
		this.scaledBitmap = scaledBitmap;
		super.setImageBitmap(scaledBitmap);
	}

	public void setScale(float scale) {
		this.scale = scale;
		Bitmap bmp = getDrawingCache();
		scaledBitmap = ImageUtils.scaleBitmap(bmp, sacaleWidth, scaleHeight);
		super.setImageBitmap(scaledBitmap);
	}
}
