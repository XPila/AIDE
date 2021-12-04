package com.xpila.support.charset;

import android.view.View;
import android.view.MotionEvent;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Typeface;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Paint;

import com.xpila.support.log.Log;
import com.xpila.support.view.ZoomAndShiftView;
import com.xpila.support.view.ZoomAndShiftGestureDetector;


public class CharsetView
extends ZoomAndShiftView
implements ZoomAndShiftGestureDetector.Listener
{
	protected Bitmap mCharset = null;
	protected int mCellWidth = 0;
	protected int mCellHeight = 0;
	protected int mGridWidth = 0;
	protected int mColorBack = 0;
	protected int mColorFore = 0;
	protected int mColorGrid = 0;
	protected int mColorText = 0;
	protected Paint mPaintBack = null;
	protected Paint mPaintFore = null;
	protected Paint mPaintGrid = null;
	protected Paint mPaintText = null;
	public CharsetView(Context context)
	{
		super(context);
		init(context, null);
	}
	public CharsetView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}
	private void init(Context context, AttributeSet attrs)
	{
		float zoom = 1;
		float minZoom = 0.5F;
		float maxZoom = 4F;
		float scrollFactor = 0.1F;
		mGridWidth = 1;
		mColorBack = 0xff000000;
		mColorFore = 0xffe0e0e0;
		mColorGrid = 0xff808080;
		mColorText = 0xff808080;		
		if (attrs != null)
		{
			zoom = attrs.getAttributeFloatValue(null, "zoom", zoom);
			Log.log("CharsetView - zoom = %.3f", zoom);
			minZoom = attrs.getAttributeFloatValue(null, "maxZoom", minZoom);
			Log.log("CharsetView - minZoom = %.3f", minZoom);
			maxZoom = attrs.getAttributeFloatValue(null, "minZoom", maxZoom);
			Log.log("CharsetView - maxZoom = %.3f", maxZoom);
			scrollFactor = attrs.getAttributeFloatValue(null, "scrollFactor", scrollFactor);
			Log.log("CharsetView - scrollFactor = %.3f", scrollFactor);
			mColorBack = attrs.getAttributeIntValue(null, "colorBack", mColorBack);
			Log.log("CharsetView - colorBack = 0x%08x", mColorBack);
			mColorFore = attrs.getAttributeIntValue(null, "colorFore", mColorFore);
			Log.log("CharsetView - colorFore = 0x%08x", mColorFore);
			mColorGrid = attrs.getAttributeIntValue(null, "colorGrid", mColorGrid);
			Log.log("CharsetView - colorGrid = 0x%08x", mColorGrid);
			mColorText = attrs.getAttributeIntValue(null, "colorText", mColorText);
			Log.log("CharsetView - colorText = 0x%08x", mColorText);
		}
		mPaintBack = new Paint();
		mPaintBack.setColor(mColorBack);
		mPaintFore = new Paint();
		mPaintFore.setColor(mColorFore);
		mPaintGrid = new Paint();
		mPaintGrid.setColor(mColorGrid);
		mPaintText = new Paint();
		mPaintText.setColor(mColorText);
		mPaintText.setAntiAlias(true);
		mDetector.mode = ZoomAndShiftGestureDetector.SHIFTXY | ZoomAndShiftGestureDetector.ZOOMXY | ZoomAndShiftGestureDetector.ZOOMUNI | ZoomAndShiftGestureDetector.SHIFTXYCENTER;
		mDetector.updateZoomRangeUni(zoom, minZoom, maxZoom);
		mDetector.scrollFactor = scrollFactor;
	}
	public void setCharset(Bitmap charset)
	{
		mCharset = charset;
		if (mCharset == null) return;
		mCellWidth = mCharset.getWidth();
		mCellHeight = mCharset.getHeight() >> 8;
		onZoom(mDetector.zoomX, mDetector.zoomY, mDetector.shiftX, mDetector.shiftY);
	}
	@Override protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (mCharset == null) return;
		int cellw = (int)(mCellWidth * mDetector.zoomX);
		int cellh = (int)(mCellHeight * mDetector.zoomY);
		int vieww = cellw * 17 + mGridWidth * 18;
		int viewh = cellh * 17 + mGridWidth * 18;
		int x0 = (int)-mDetector.shiftX;
		int y0 = (int)-mDetector.shiftY;
		//draw horizontal grid lines
		for (int r = 0; r < 18; r++)
		{
			int y = r * (cellh + mGridWidth);
			canvas.drawLine(x0 + 0, y0 + y, x0 + vieww, y0 + y, mPaintGrid);
		}
		//draw vertical grid lines
		for (int c = 0; c < 18; c++)
		{
			int x = c * (cellw + mGridWidth);
			canvas.drawLine(x0 + x, y0 + 0, x0 + x, y0 + viewh, mPaintGrid);
		}
		//adjust text size
		mPaintText.setTextSize(cellh * 0.8F);
		mPaintText.setTextScaleX((float)cellw / cellh);
		//draw horizontal text
		for (int c = 0; c < 16; c++)
		{
			int x = (c + 1) * (cellw + mGridWidth) + (int)(cellw * 0.2F);
			int y = (int)(cellh * 0.8F);
			canvas.drawText(String.format("%X", c), x0 + x, y0 + y, mPaintText);
		}
		//draw vertical text
		for (int r = 0; r < 16; r++)
		{
			int x = (int)(cellw * 0.2F);
			int y = (r + 1) * (cellh + mGridWidth) + (int)(cellh * 0.8F);
			canvas.drawText(String.format("%X", r), x0 + x, y0 + y, mPaintText);
		}
		//draw characters
		for (int r = 0; r < 16; r++)
			for (int c = 0; c < 16; c++)
			{
				int x = (c + 1) * (cellw + mGridWidth) + mGridWidth;
				int y = (r + 1) * (cellh + mGridWidth) + mGridWidth;
				CharsetGenerator.drawChar(canvas, mCharset, c + 16 * r, x0 + x, y0 + y, mDetector.zoomX, mPaintBack, mPaintFore);
			}
	}
	public void onZoom(float zoomX, float zoomY, float shiftX, float shiftY)
	{
		int cellw = (int)(mCellWidth * mDetector.zoomX);
		int cellh = (int)(mCellHeight * mDetector.zoomY);
		int vieww = cellw * 17 + mGridWidth * 18;
		int viewh = cellh * 17 + mGridWidth * 18;
		mDetector.updateShiftRange(mWidth, mHeight, vieww, viewh);
		invalidate();
	}
}
