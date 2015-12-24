package com.xpila.support.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import com.xpila.support.view.ZoomAndShiftGestureDetector;


public class ZoomAndShiftImageView
extends ImageView
implements ZoomAndShiftGestureDetector.Listener
{
	protected int mWidth = 0;
	protected int mHeight = 0;
	protected Drawable mImage = null;
	protected int mImageWidth = 0;
	protected int mImageHeight = 0;
	protected ZoomAndShiftGestureDetector mDetector = null;
	protected Matrix mMatrix = null;
	public ZoomAndShiftImageView(Context context)
	{
		super(context);
		init();
	}
	public ZoomAndShiftImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	protected void init()
	{
		mDetector = new ZoomAndShiftGestureDetector(this, null, 1);
		mDetector.mode = ZoomAndShiftGestureDetector.SHIFTXY | ZoomAndShiftGestureDetector.ZOOMXY | ZoomAndShiftGestureDetector.ZOOMUNI;
		mDetector.zoomX = 0.1F;
		mDetector.zoomY = 0.1F;
		mDetector.minZoomX = 0.1F;
		mDetector.minZoomY = 0.1F;
		mDetector.maxZoomX = 10F;
		mDetector.maxZoomY = 10F;
		mMatrix = new Matrix();
		setScaleType(ImageView.ScaleType.MATRIX);
	}
	public void setZoom(float zoom)
	{
		mDetector.zoomX = zoom;
		mDetector.zoomY = zoom;
		if ((mWidth * mHeight) > 0)
		{
			updateShiftRange();
			updateMatrix();
		}
	}
	@Override public void setImageDrawable(Drawable drawable)
	{
		super.setImageDrawable(drawable);
		mImage = drawable;
		if (mImage != null)
		{
			mImageWidth = mImage.getIntrinsicWidth();
			mImageHeight = mImage.getIntrinsicHeight();
		}
		else
		{
			mImageWidth = 0;
			mImageHeight = 0;
		}
		updateZoomRange();
		updateShiftRange();
		updateMatrix();
	}
	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int modeW = MeasureSpec.getMode(widthMeasureSpec);
		int sizeW = MeasureSpec.getSize(widthMeasureSpec);
		int modeH = MeasureSpec.getMode(heightMeasureSpec);
		int sizeH = MeasureSpec.getSize(heightMeasureSpec);
		int w = getSuggestedMinimumWidth();
		int h = getSuggestedMinimumHeight();
		if ((modeW == View.MeasureSpec.EXACTLY) || ((modeW == View.MeasureSpec.AT_MOST) && (w == 0)))
			w = sizeW;
		if ((modeH == View.MeasureSpec.EXACTLY) || ((modeH == View.MeasureSpec.AT_MOST) && (h == 0)))
			h = sizeH;
		setMeasuredDimension(w, h);
	}
	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		if ((oldw * oldh) == 0)
			updateZoomRange();
		if ((mWidth * mHeight) > 0)
		{
			updateShiftRange();
			updateMatrix();
		}
	}
	@Override public boolean onTouchEvent(MotionEvent event)
	{
		if (mDetector.onTouch(this, event)) return true;
		return super.onTouchEvent(event);
	}
	public void onZoom(float zoomX, float zoomY, float shiftX, float shiftY)
	{
		updateShiftRange();
		updateMatrix();
	}
	public void onShift(float zoomX, float zoomY, float shiftX, float shiftY)
	{
		updateMatrix();
	}
	protected void updateZoomRange()
	{
		if ((mWidth * mHeight) == 0) return;
		if ((mImageWidth * mImageHeight) == 0) return;
		float zoomToFitX = (float)mWidth / mImageWidth;
		float zoomToFitY = (float)mHeight / mImageHeight;
		float minZoom = (zoomToFitX < zoomToFitY)?zoomToFitX:zoomToFitY;
		if (minZoom > 1) minZoom = 1;
		float maxZoom = 2;
		mDetector.minZoomX = minZoom;
		mDetector.minZoomY = minZoom;
		mDetector.maxZoomX = maxZoom;
		mDetector.maxZoomY = maxZoom;
		mDetector.zoomX = minZoom;
		mDetector.zoomY = minZoom;
	}
	protected void updateShiftRange()
	{
		if ((mWidth * mHeight) == 0) return;
		if ((mImageWidth * mImageHeight) == 0) return;
		mDetector.maxShiftX = (mImageWidth * mDetector.zoomX) - mWidth;
		mDetector.maxShiftY = (mImageHeight * mDetector.zoomY) - mHeight;
		if (mDetector.maxShiftX < 0)
		{
			mDetector.maxShiftX = mDetector.maxShiftX / 2;
			mDetector.minShiftX = mDetector.maxShiftX;
			mDetector.shiftX = mDetector.minShiftX;
		}
		else
			mDetector.minShiftX = 0;
		if (mDetector.maxShiftY < 0)
		{
			mDetector.maxShiftY = mDetector.maxShiftY / 2;
			mDetector.minShiftY = mDetector.maxShiftY;
			mDetector.shiftY = mDetector.minShiftY;
		}
		else
			mDetector.minShiftY = 0;
		if (mDetector.shiftX > mDetector.maxShiftX)
			mDetector.shiftX = mDetector.maxShiftX;
		if (mDetector.shiftY > mDetector.maxShiftY)
			mDetector.shiftY = mDetector.maxShiftY;
		if (mDetector.shiftX < mDetector.minShiftX)
			mDetector.shiftX = mDetector.minShiftX;
		if (mDetector.shiftY < mDetector.minShiftY)
			mDetector.shiftY = mDetector.minShiftY;
	}
	protected void updateMatrix()
	{
		mMatrix.reset();
		mMatrix.preScale(mDetector.zoomX, mDetector.zoomY);
		mMatrix.postTranslate(-mDetector.shiftX, -mDetector.shiftY);
		setImageMatrix(mMatrix);
	}
}

