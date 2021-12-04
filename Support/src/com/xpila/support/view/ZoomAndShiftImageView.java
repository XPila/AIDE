package com.xpila.support.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import com.xpila.support.log.Log;
import com.xpila.support.view.ZoomAndShiftView;
import com.xpila.support.view.ZoomAndShiftGestureDetector;


public class ZoomAndShiftImageView
extends ZoomAndShiftView
implements ZoomAndShiftGestureDetector.Listener
{
	protected Drawable mImage = null;
	protected int mImageWidth = 0;
	protected int mImageHeight = 0;
	protected Matrix mMatrix = null;
	public ZoomAndShiftImageView(Context context)
	{
		super(context);
		init(context, null);
	}
	public ZoomAndShiftImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}
	protected void init(Context context, AttributeSet attrs)
	{
		float zoom = 1;
		float minZoom = 0.5F;
		float maxZoom = 4F;
		float scrollFactor = 0.1F;
		if (attrs != null)
		{
			zoom = attrs.getAttributeFloatValue(null, "zoom", zoom);
			Log.log("ZoomAndShiftImageView - zoom = %.3f", zoom);
			minZoom = attrs.getAttributeFloatValue(null, "maxZoom", minZoom);
			Log.log("ZoomAndShiftImageView - minZoom = %.3f", minZoom);
			maxZoom = attrs.getAttributeFloatValue(null, "minZoom", maxZoom);
			Log.log("ZoomAndShiftImageView - maxZoom = %.3f", maxZoom);
			scrollFactor = attrs.getAttributeFloatValue(null, "scrollFactor", scrollFactor);
			Log.log("ZoomAndShiftImageView - scrollFactor = %.3f", scrollFactor);
		}
		mDetector.mode = ZoomAndShiftGestureDetector.SHIFTXY | ZoomAndShiftGestureDetector.ZOOMXY | ZoomAndShiftGestureDetector.ZOOMUNI;
		mDetector.updateZoomRangeUni(zoom, minZoom, maxZoom);
		mDetector.scrollFactor = scrollFactor;
		mMatrix = new Matrix();
	}
	public void setImageDrawable(Drawable drawable)
	{
		mImage = drawable;
		if (mImage != null)
		{
			mImageWidth = mImage.getIntrinsicWidth();
			mImageHeight = mImage.getIntrinsicHeight();
			mImage.setBounds(0, 0, mImageWidth, mImageHeight);
		}
		else
		{
			mImageWidth = 0;
			mImageHeight = 0;
		}
		onZoom(mDetector.zoomX, mDetector.zoomY, mDetector.shiftX, mDetector.shiftY);
	}
	@Override protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (mImage == null) return;
		if ((mWidth * mHeight) == 0) return;
		if ((mImageWidth * mImageHeight) == 0) return;
		canvas.setMatrix(mMatrix);
		mImage.draw(canvas);
	}
	
	public void onZoom(float zoomX, float zoomY, float shiftX, float shiftY)
	{
		mDetector.updateShiftRange(mWidth, mHeight, mImageWidth * zoomX, mImageHeight * zoomY);
		updateMatrix();
		invalidate();
	}
	public void onShift(float zoomX, float zoomY, float shiftX, float shiftY)
	{
		updateMatrix();
		invalidate();
	}
	protected void updateMatrix()
	{
		mMatrix.reset();
		mMatrix.preScale(mDetector.zoomX, mDetector.zoomY);
		mMatrix.postTranslate(-mDetector.shiftX, -mDetector.shiftY);
	}
}

