package com.xpila.support.view;


import android.view.View;
import android.view.MotionEvent;


public class DragGestureDetector
implements View.OnTouchListener
{
	public interface Listener
	{
		public void onDragBegin(View v, float x0, float y0, float x, float y);
		public void onDragEnd(View v, float x0, float y0, float x, float y);
		public void onDrag(View v, float x0, float y0, float x, float y);
	}
	private Listener mListener = null;
	private boolean mDown = false;
	private boolean mInProgress = false;
	private float mX = 0;
	private float mY = 0;
	public boolean enabled = false;
	public float deltaMin = 0;
	public DragGestureDetector(Listener listener, View v, float deltaMin)
	{
		mListener = listener;
		if (v != null) v.setOnTouchListener(this);
		this.deltaMin = deltaMin;
		enabled = true;
	}
	public boolean onTouch(View v, MotionEvent event)
	{
		if (!enabled) return false;
		if (mListener == null) return false;
		int action = event.getActionMasked();
		int count = event.getPointerCount();
		if (count == 1)
		{
			float x = event.getX();
			float y = event.getY();
			switch (action)
			{
			case MotionEvent.ACTION_DOWN:
				mX = x;
				mY = y;
				mDown = true;
				mInProgress = false;
				return true;
			case MotionEvent.ACTION_MOVE:
				if (mInProgress)
					mListener.onDrag(v, mX, mY, x, y);
				else if (mDown)
				{
					float dx = x - mX;
					float dy = y - mY;
					if (Math.sqrt(dx * dx + dy * dy) >= deltaMin)
					{
						mInProgress = true;
						mListener.onDragBegin(v, mX, mY, x, y);
					}
				}
				return true;
			case MotionEvent.ACTION_UP:
				if (mInProgress)
				{
					mListener.onDragEnd(v, mX, mY, x, y);
					mInProgress = false;
					mDown = false;
				}
				return true;
			}
		}
		else
		{
			if (mInProgress) mListener.onDragEnd(v, mX, mY, mX, mY);
			mInProgress = false;
			mDown = false;
		}
		return false;
	}
}

