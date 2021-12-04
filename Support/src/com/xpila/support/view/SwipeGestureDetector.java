package com.xpila.support.view;


import android.view.View;
import android.view.MotionEvent;
import java.util.*;


public class SwipeGestureDetector
implements View.OnTouchListener
{
	public static final int DIR_LEFT = 1;
	public static final int DIR_RIGHT = 2;
	public static final int DIR_UP = 4;
	public static final int DIR_DOWN = 8;
	
	public interface Listener
	{
		public void onSwipe(View v, int dir, float vx, float vy);
	}
	private Listener mListener = null;
	private boolean mDown = false;
	private float mX = 0;
	private float mY = 0;
	private long mT = 0;
	public boolean enabled = false;
	public float velXMin = 0;
	public float velYMin = 0;
	public SwipeGestureDetector(Listener listener, View v, float velXMin, float velYMin)
	{
		mListener = listener;
		if (v != null) v.setOnTouchListener(this);
		this.velXMin = velXMin;
		this.velYMin = velYMin;
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
			long t = event.getEventTime();
			switch (action)
			{
			case MotionEvent.ACTION_DOWN:
				mX = x;
				mY = y;
				mT = t;
				mDown = true;
				return false;
			case MotionEvent.ACTION_MOVE:
				if (mDown)
				{
					float dx = x - mX;
					float dY = y - mY;
					long dt = t - mT;
					float velX = dx / dt;
					float velY = dY / dt;
					int dir = 0;
					if (Math.abs(velX) > velXMin)
						dir |= (velX > 0)?DIR_RIGHT:DIR_LEFT;
					if (Math.abs(velY) > velYMin)
						dir |= (velY > 0)?DIR_DOWN:DIR_UP;
					if (dir != 0)
					{
						mListener.onSwipe(v, dir, velX, velY);
						mDown = false;
					}
				}
				return false;
			case MotionEvent.ACTION_UP:
				mDown = false;
				return false;
			}
		}
		else
			mDown = false;
		return false;
	}
}

