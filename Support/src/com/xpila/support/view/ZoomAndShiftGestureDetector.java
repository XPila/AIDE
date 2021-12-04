package com.xpila.support.view;


import android.view.View;
import android.view.MotionEvent;
import com.xpila.support.view.DragAndScaleGestureDetector;


public class ZoomAndShiftGestureDetector
implements View.OnTouchListener, DragAndScaleGestureDetector.Listener, ScaleGestureDetector.Listener
{
	public final static int ZOOMX = 1;
	public final static int ZOOMY = 2;
	public final static int ZOOMXY = ZOOMX | ZOOMY;
	public final static int ZOOMUNI = 4;
	public final static int ZOOMEXC = 8;
	public final static int SHIFTX = 16;
	public final static int SHIFTY = 32;
	public final static int SHIFTXY = SHIFTX | SHIFTY;
	public static final int SHIFTXCENTER = 64;
	public static final int SHIFTYCENTER = 128;
	public static final int SHIFTXYCENTER = SHIFTXCENTER | SHIFTYCENTER;
	public interface Listener
	{
		public void onZoom(float zoomX, float zoomY, float shiftX, float shiftY);
		public void onShift(float zoomX, float zoomY, float shiftX, float shiftY);
	}
	private Listener mListener = null;
	private DragAndScaleGestureDetector mDetector = null;
	public boolean enabled = false;
	public int mode = 0;
	public float zoomX = 0;
	public float zoomY = 0;
	public float minZoomX = 0;
	public float minZoomY = 0;
	public float maxZoomX = 0;
	public float maxZoomY = 0;
	public float beginZoomX = 0;
	public float beginZoomY = 0;
	public float shiftX = 0;
	public float shiftY = 0;
	public float minShiftX = 0;
	public float minShiftY = 0;
	public float maxShiftX = 0;
	public float maxShiftY = 0;
	public float beginShiftX = 0;
	public float beginShiftY = 0;
	public boolean uniformZoom = false;
	public float scrollFactor = 0;
	
	public ZoomAndShiftGestureDetector(Listener listener, View v, float deltaMin)
	{
		mDetector = new DragAndScaleGestureDetector(this, null, deltaMin);
		mListener = listener;
		if (v != null) v.setOnTouchListener(this);
		enabled = true;
	}
	public boolean onTouch(View v, MotionEvent event)
	{
		if (!enabled) return false;
		if (mListener == null) return false;
		return mDetector.onTouch(v, event);
	}
	public boolean onGenericMotion(View v, MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_SCROLL)
		{
			float x = event.getAxisValue(MotionEvent.AXIS_X);
			float y = event.getAxisValue(MotionEvent.AXIS_Y);
			float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
			float hscroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
			float scroll = vscroll;
			float zX = zoomX;
			float zY = zoomY;
			float sX = shiftX;
			float sY = shiftY;
			float dX = (1 + (scroll * scrollFactor));
			float dY = (1 + (scroll * scrollFactor));
			beginZoomX = zX;
			beginZoomY = zY;
			beginShiftX = sX;
			beginShiftY = sY;
			scale(v, x, y, 1, 1, x, y, dX, dY);
			return true;
		}
		return false;
	}	
	public void onDragBegin(View v, float x0, float y0, float x, float y)
	{
		beginShiftX = shiftX;
		beginShiftY = shiftY;
		drag(v, x0, y0, x, y);
	}
	public void onDragEnd(View v, float x0, float y0, float x, float y)
	{
		drag(v, x0, y0, x, y);
	}
	public void onDrag(View v, float x0, float y0, float x, float y)
	{
		drag(v, x0, y0, x, y);
	}
	public void onScaleBegin(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy)
	{
		beginZoomX = zoomX;
		beginZoomY = zoomY;
		beginShiftX = shiftX;
		beginShiftY = shiftY;
		scale(v, x0, y0, dx0, dy0, x, y, dx, dy);
	}
	public void onScaleEnd(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy)
	{
		scale(v, x0, y0, dx0, dy0, x, y, dx, dy);
	}
	public void onScale(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy)
	{
		scale(v, x0, y0, dx0, dy0, x, y, dx, dy);
	}
	public void drag(View v, float x0, float y0, float x, float y)
	{
		if ((mode & SHIFTX) != 0)
			shiftX = beginShiftX - (x - x0);
		if ((mode & SHIFTY) != 0)
			shiftY = beginShiftY - (y - y0);
		if (shiftX < minShiftX) shiftX = minShiftX;
		if (shiftY < minShiftY) shiftY = minShiftY;
		if (shiftX > maxShiftX) shiftX = maxShiftX;
		if (shiftY > maxShiftY) shiftY = maxShiftY;
		if (mListener != null) mListener.onShift(zoomX, zoomY, shiftX, shiftY);
	}
	public void scale(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy)
	{
		if ((mode & ZOOMUNI) != 0);
		{
			float d0 = (float)Math.sqrt(dx0 * dx0 + dy0 * dy0);
			float d = (float)Math.sqrt(dx * dx + dy * dy);
			dx0 = dy0 = d0;
			dx = dy = d;
		}
		if ((mode & ZOOMX) != 0)
			zoomX = beginZoomX * dx / dx0;
		if ((mode & ZOOMY) != 0)
			zoomY = beginZoomY * dy / dy0;
		if (zoomX < minZoomX) zoomX = minZoomX;
		if (zoomX > maxZoomX) zoomX = maxZoomX;
		if (zoomY < minZoomY) zoomY = minZoomY;
		if (zoomY > maxZoomY) zoomY = maxZoomY;
		if ((mode & SHIFTX) != 0)
			shiftX = (x0 + beginShiftX) * zoomX / beginZoomX - x;
		if ((mode & SHIFTY) != 0)
			shiftY = (y0 + beginShiftY) * zoomY / beginZoomY - y;
		if (mListener != null) mListener.onZoom(zoomX, zoomY, shiftX, shiftY);
	}
	public void updateZoomRangeUni(float zoom, float minZoom, float maxZoom)
	{
		zoomX = zoom;
		zoomY = zoom;
		minZoomX = minZoom;
		minZoomY = minZoom;
		maxZoomX = maxZoom;
		maxZoomY = maxZoom;
		if (zoomX < minZoomX) zoomX = minZoomX;
		if (zoomX > maxZoomX) zoomX = maxZoomX;
		if (zoomY < minZoomY) zoomY = minZoomY;
		if (zoomY > maxZoomY) zoomY = maxZoomY;
	}
	public void updateShiftRange(float w, float h, float wz, float hz)
	{
		if ((w * h) == 0) return;
		if ((wz * hz) == 0) return;
		minShiftX = 0;
		minShiftY = 0;
		maxShiftX = wz - w;
		maxShiftY = hz - h;
		if (maxShiftX < 0)
		{
			if ((mode & SHIFTXCENTER) != 0)
			{
				maxShiftX = maxShiftX / 2;
				minShiftX = maxShiftX;
			}
			else
				maxShiftX = 0;
			//mDetector.shiftX = mDetector.minShiftX;
		}
		if (maxShiftY < 0)
		{
			if ((mode & SHIFTYCENTER) != 0)
			{
				maxShiftY = maxShiftY / 2;
				minShiftY = maxShiftY;
			}
			else
				maxShiftY = 0;
			//mDetector.shiftY = mDetector.minShiftY;
		}
		if (shiftX > maxShiftX) shiftX = maxShiftX;
		if (shiftY > maxShiftY) shiftY = maxShiftY;
		if (shiftX < minShiftX) shiftX = minShiftX;
		if (shiftY < minShiftY) shiftY = minShiftY;
	}
}
