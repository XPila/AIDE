package com.xpila.support.view;


import android.view.View;
import android.view.MotionEvent;
import com.xpila.support.view.DragGestureDetector;
import com.xpila.support.view.ScaleGestureDetector;


public class DragAndScaleGestureDetector
implements View.OnTouchListener, DragGestureDetector.Listener, ScaleGestureDetector.Listener
{
	public interface Listener
	extends DragGestureDetector.Listener, ScaleGestureDetector.Listener
	{}
	private Listener mListener = null;
	private DragGestureDetector mDragDetector = null;
	private ScaleGestureDetector mScaleDetector = null;
	public boolean enabled = false;
	public DragAndScaleGestureDetector(Listener listener, View v, float deltaMin)
	{
		mDragDetector = new DragGestureDetector(this, null, deltaMin);
		mScaleDetector = new ScaleGestureDetector(this, null, deltaMin);
		mListener = listener;
		if (v != null) v.setOnTouchListener(this);
		enabled = true;
	}
	public boolean onTouch(View v, MotionEvent event)
	{
		if (!enabled) return false;
		if (mListener == null) return false;
		boolean drag = mDragDetector.onTouch(v, event);
		boolean scale = mScaleDetector.onTouch(v, event);
		return drag || scale;
	}
	public void onDragBegin(View v, float x0, float y0, float x, float y)
	{ if (mListener != null) mListener.onDragBegin(v, x0, y0, x, y); }
	public void onDragEnd(View v, float x0, float y0, float x, float y)
	{ if (mListener != null) mListener.onDragEnd(v, x0, y0, x, y); }
	public void onDrag(View v, float x0, float y0, float x, float y)
	{ if (mListener != null) mListener.onDrag(v, x0, y0, x, y); }
	public void onScaleBegin(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy)
	{ if (mListener != null) mListener.onScaleBegin(v, x0, y0, dx0, dy0, x, y, dx, dy); }
	public void onScaleEnd(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy)
	{ if (mListener != null) mListener.onScaleEnd(v, x0, y0, dx0, dy0, x, y, dx, dy); }
	public void onScale(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy)
	{ if (mListener != null) mListener.onScale(v, x0, y0, dx0, dy0, x, y, dx, dy); }
}

