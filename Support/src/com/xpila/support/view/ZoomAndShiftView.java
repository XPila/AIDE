package com.xpila.support.view;


import android.content.Context;
import android.view.View;
import android.view.MotionEvent;
import android.util.AttributeSet;


import com.xpila.support.log.Log;
import com.xpila.support.view.ZoomAndShiftGestureDetector;


public class ZoomAndShiftView
extends View
implements ZoomAndShiftGestureDetector.Listener
{
	protected int mWidth = 0;
	protected int mHeight = 0;
	protected ZoomAndShiftGestureDetector mDetector = null;
	public ZoomAndShiftView(Context context)
	{
		super(context);
		init(context, null);
	}
	public ZoomAndShiftView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}
	private void init(Context context, AttributeSet attrs)
	{
		mWidth = 0;
		mHeight = 0;
		mDetector = new ZoomAndShiftGestureDetector(this, null, 1);
	}
	public float getZoomX() { return mDetector.zoomX; }
	public void setZoomX(float zoomX)
	{
		if (zoomX > mDetector.maxZoomX) zoomX = mDetector.maxZoomX;
		if (zoomX < mDetector.minZoomX) zoomX = mDetector.minZoomX;
		if (mDetector.zoomX != zoomX)
		{
			mDetector.zoomX = zoomX;
			onZoom(mDetector.zoomX, mDetector.zoomY, mDetector.shiftX, mDetector.shiftY);
		}
	}
	public float getZoomY() { return mDetector.zoomY; }
	public void setZoomY(float zoomY)
	{
		if (zoomY > mDetector.maxZoomY) zoomY = mDetector.maxZoomY;
		if (zoomY < mDetector.minZoomY) zoomY = mDetector.minZoomY;
		if (mDetector.zoomY != zoomY)
		{
			mDetector.zoomY = zoomY;
			onZoom(mDetector.zoomX, mDetector.zoomY, mDetector.shiftX, mDetector.shiftY);
		}
	}
	public float getZoomUni() { return (mDetector.zoomX + mDetector.zoomY) / 2; }
	public void setZoomUni(float zoom)
	{
		if (zoom > mDetector.maxZoomX) zoom = mDetector.maxZoomX;
		if (zoom < mDetector.minZoomX) zoom = mDetector.minZoomX;
		if (zoom > mDetector.maxZoomY) zoom = mDetector.maxZoomY;
		if (zoom < mDetector.minZoomY) zoom = mDetector.minZoomY;
		if ((mDetector.zoomX != zoom) || (mDetector.zoomY != zoom))
		{
			mDetector.zoomX = zoom;
			mDetector.zoomY = zoom;
			onZoom(mDetector.zoomX, mDetector.zoomY, mDetector.shiftX, mDetector.shiftY);
		}
	}
	public float getMinZoomX() { return mDetector.minZoomX; }
	public void setMinZoomX(float minZoomX) { mDetector.minZoomX = minZoomX; }
	public float getMaxZoomX() { return mDetector.maxZoomX; }
	public void setMaxZoomX(float maxZoomX) { mDetector.maxZoomX = maxZoomX; }
	public float getMinZoomY() { return mDetector.minZoomY; }
	public void setMinZoomY(float minZoomY) { mDetector.minZoomY = minZoomY; }
	public float getMaxZoomY() { return mDetector.maxZoomY; }
	public void setMaxZoomY(float maxZoomY) { mDetector.maxZoomY = maxZoomY; }
	public float getMinZoomUni() { return (mDetector.minZoomX + mDetector.minZoomY) / 2; }
	public void setMinZoomUni(float minZoom) { mDetector.minZoomX = minZoom; mDetector.minZoomY = minZoom; }
	public float getMaxZoomUni() { return (mDetector.maxZoomX + mDetector.maxZoomY) / 2; }
	public void setMaxZoomUni(float maxZoom) { mDetector.maxZoomX = maxZoom; mDetector.maxZoomY = maxZoom; }
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
		onZoom(mDetector.zoomX, mDetector.zoomY, mDetector.shiftX, mDetector.shiftY);
	}
	@Override public boolean onTouchEvent(MotionEvent event)
	{
		if (mDetector.onTouch(this, event)) return true;
		return false;
	}
	@Override public boolean onGenericMotionEvent(MotionEvent event)
	{
		mDetector.onGenericMotion(this, event);
		return false;
	}
	public void onZoom(float zoomX, float zoomY, float shiftX, float shiftY)
	{
		invalidate();
	}
	public void onShift(float zoomX, float zoomY, float shiftX, float shiftY)
	{
		invalidate();
	}
}

