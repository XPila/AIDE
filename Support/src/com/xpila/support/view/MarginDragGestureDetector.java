package com.xpila.support.view;


import android.view.View;
import android.view.MotionEvent;


public class MarginDragGestureDetector
implements View.OnTouchListener
{
	public interface Listener
	{
		public void onDragMargin(View v, int margin, boolean outside);
	}
	public final static int LEFT = 1;
	public final static int RIGHT = 2;
	public final static int TOP = 4;
	public final static int BOTTOM = 8;
	private Listener mListener = null;
	private int mMask = 0;
	private float mMarginIn = 0;
	private float mMarginOut = 0;
	private float mDownX = 0;
	private float mDownY = 0;
	private boolean mNotified = false;
	public MarginDragGestureDetector(Listener listener, View v, int mask, float marginIn, float marginOut)
	{
		mListener = listener;
		mMask = mask;
		mMarginIn = marginIn;
		mMarginOut = marginOut;
		if (v != null) v.setOnTouchListener(this);
	}
	public boolean onTouch(View v, MotionEvent event)
	{
		if ((event.getPointerCount() != 1) || (mListener == null)) return false;
		int w = v.getWidth();
		int h = v.getHeight();
		switch (event.getActionMasked())
		{
			case event.ACTION_DOWN:
				mDownX = event.getX();
				mDownY = event.getY();
				mNotified = false;
				break;
			case event.ACTION_MOVE:
				if (mNotified) break;
				if (((mMask & LEFT) != 0) && (mDownX < mMarginIn) && (event.getX() >= mMarginIn)) notifyListener(v, LEFT, false);
				if (((mMask & LEFT) != 0) && (mDownX >= mMarginOut) && (event.getX() < mMarginOut)) notifyListener(v, LEFT, true);
				if (((mMask & RIGHT) != 0) && (mDownX >= (w - mMarginIn)) && (event.getX() < (w - mMarginIn))) notifyListener(v, RIGHT, false);
				if (((mMask & RIGHT) != 0) && (mDownX < (w - mMarginOut)) && (event.getX() >= (w - mMarginOut))) notifyListener(v, RIGHT, true);
				if (((mMask & TOP) != 0) && (mDownY < mMarginIn) && (event.getY() >= mMarginIn)) notifyListener(v, TOP, false);
				if (((mMask & TOP) != 0) && (mDownY >= mMarginOut) && (event.getY() < mMarginOut)) notifyListener(v, TOP, true);
				if (((mMask & BOTTOM) != 0) && (mDownY >= (h - mMarginIn)) && (event.getY() < (h - mMarginIn))) notifyListener(v, BOTTOM, false);
				if (((mMask & BOTTOM) != 0) && (mDownY < (h - mMarginOut)) && (event.getY() >= (h - mMarginOut))) notifyListener(v, BOTTOM, true);
				break;
		}
		return true;
	}
	protected void notifyListener(View v, int margin, boolean outside)
	{
		mListener.onDragMargin(v, margin, outside);
		mNotified = true;
	}
}

