package com.xpila.support.view;


import android.view.View;
import android.view.MotionEvent;


public class ScaleGestureDetector
implements View.OnTouchListener
{
	public interface Listener
	{
		public void onScaleBegin(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy);
		public void onScaleEnd(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy);
		public void onScale(View v, float x0, float y0, float dx0, float dy0, float x, float y, float dx, float dy);
	}
	private Listener mListener = null;
	private boolean mDown = false;
	private boolean mInProgress = false;
	private float mX = 0;
	private float mY = 0;
	private float mDX = 0;
	private float mDY = 0;
	public boolean enabled = false;
	public float deltaMin = 0;
	public ScaleGestureDetector(Listener listener, View v, float deltaMin)
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
		if ((count == 1) && (action == MotionEvent.ACTION_DOWN))
			return true;
		else if (count == 2)
		{
			float xA = event.getX(0);
			float yA = event.getY(0);
			float xB = event.getX(1);
			float yB = event.getY(1);
			float x = (xA + xB) / 2;
			float y = (yA + yB) / 2;
			float dx = xB - xA;
			float dy = yB - yA;
			switch (action)
			{
			case MotionEvent.ACTION_POINTER_DOWN:
				mX = x;
				mY = y;
				mDX = dx;
				mDY = dy;
				mDown = true;
				mInProgress = false;
				return true;
			case MotionEvent.ACTION_MOVE:
				if (mInProgress)
					mListener.onScale(v, mX, mY, mDX, mDY, x, y, dx, dy);
				else if (mDown)
				{
					float ddx = dx - mDX;
					float ddy = dy - mDY;
					if (Math.sqrt(ddx * ddx + ddy * ddy) >= deltaMin)
					{
						mInProgress = true;
						mListener.onScaleBegin(v, mX, mY, mDX, mDY, x, y, dx, dy);
					}
				}
				return true;
			case MotionEvent.ACTION_POINTER_UP:
				if (mInProgress)
				{
					mListener.onScaleEnd(v, mX, mY, mDX, mDY, x, y, dx, dy);
					mInProgress = false;
					mDown = false;
				}
				return true;
			}
		}
		else
		{
			if (mInProgress) mListener.onScaleEnd(v, mX, mY, mDX, mDY, mX, mY, mDX, mDY);
			mInProgress = false;
			mDown = false;
		}
		return false;
	}
}

