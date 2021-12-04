package com.xpila.support.view;

import android.view.View;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.SurfaceView;
import android.view.View.MeasureSpec;

import java.util.Queue;
import java.util.LinkedList;

import com.xpila.support.log.Log;


//there are three modes of usage:
//1. rect ((mInvalidateRect != null) && (mInvalidateQueue == null))
// invalidate function sends notify signal to drawing thread through mInvalidateRect.
//2. queue ((mInvalidateRect == null) && (mInvalidateQueue != null))
// invalidate function sends notify signal to drawing thread through mInvalidateQueue.
//3. loop ((mInvalidateRect == null) && (mInvalidateQueue == null))
// invalidate function does nothing.
public class ThreadedSurfaceView
extends SurfaceView
{
	protected Object mLock = null; //synchronization object	
	protected int mSurfaceFormat = 0; //surface format
	protected int mSurfaceWidth = 0; //surface width in pixels
	protected int mSurfaceHeight = 0; //surface height in pixels
	protected SurfaceHolder mSurfaceHolder = null; //surface holder for this view
	protected HolderCallback mHolderCallback = null; //surface holder callback
	protected DrawThread mDrawThread = null; //drawing thread
	protected Rect mInvalidateRect = null; //invalidate rectangle
	protected Queue<Rect> mInvalidateQueue = null; //invalidate queue containing rectangles
	public ThreadedSurfaceView(Context context)
	{
		super(context);
		initThreadedSurfaceView();
	}
	public ThreadedSurfaceView(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		initThreadedSurfaceView();
	}
	protected void initThreadedSurfaceView()
	{
		mLock = new Object();		
		mSurfaceHolder = getHolder();
		mHolderCallback = new HolderCallback();
		mSurfaceHolder.addCallback(mHolderCallback);
		//mInvalidateQueue = new LinkedList<Rect>();
		//mInvalidateRect = new Rect();
	}
	public void drawSurface(Canvas canvas, Rect rect)
	{//called in drawing thread, synchronized by mLock
		//drawing code
	}
	public void recalcRect(Rect rect)
	{//called in drawing thread, synchronized by mLock
		//recalculate rectangle before call drawSurface (because of lockCanvas...)
		
	}
	public void invalidateSurface(Rect rect)
	{
		//Log.log("invalidateSurface %d %d %d %d", rect.left, rect.top, rect.width(), rect.height());
		if (mInvalidateRect != null)
			synchronized (mInvalidateRect)
			{
				if (rect != null)
					mInvalidateRect.set(rect);
				else
					mInvalidateRect.setEmpty();
				mInvalidateRect.notify();
			}		
		else if (mInvalidateQueue != null)
			synchronized (mInvalidateQueue)
			{
				mInvalidateQueue.add(rect);
				mInvalidateQueue.notify();
			}		
	}	
	protected boolean startDrawThread()
	{
		if ((mDrawThread != null) && mDrawThread.mRunning) return true;
		mDrawThread = new DrawThread();
		mDrawThread.start();
		synchronized (mDrawThread)
		{
			try
			{ mDrawThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return mDrawThread.mRunning;
	}
	protected boolean stopDrawThread()
	{
		if (mDrawThread == null) return true;
		mDrawThread.mRunning = false;
		mDrawThread.interrupt();
		try
		{ mDrawThread.join(); }
		catch (InterruptedException e)
		{ e.printStackTrace(); }
		mDrawThread = null;
		return true;
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
		if ((modeW == View.MeasureSpec.AT_MOST) || (modeW == View.MeasureSpec.EXACTLY))
			w = sizeW;
		if ((modeH == View.MeasureSpec.AT_MOST) || (modeH == View.MeasureSpec.EXACTLY))
			h = sizeH;
		setMeasuredDimension(w, h);
	}	
	protected class HolderCallback
	implements SurfaceHolder.Callback
	{
		@Override public void surfaceCreated(SurfaceHolder holder)
		{
			startDrawThread();
		}
		@Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			mSurfaceFormat = format;
			mSurfaceWidth = width;
			mSurfaceHeight = height;
		}
		@Override public void surfaceDestroyed(SurfaceHolder holder)
		{
			stopDrawThread();
		}
	}
	protected class DrawThread
	extends Thread
	{
		protected boolean mRunning = false;
		@Override public void run()
		{
			//Log.log("Thread - run");
			mRunning = true;
			synchronized (this)
			{ notify(); }
			try
			{
				Rect rect = new Rect();
				while (mRunning)
				{
					//Log.log("Thread - loop");
					if (mInvalidateRect != null)
						synchronized (mInvalidateRect)
						{
							mInvalidateRect.wait();
							rect.set(mInvalidateRect);
						}
					else if (mInvalidateQueue != null)
						synchronized (mInvalidateQueue)
						{
							if (mInvalidateQueue.isEmpty())
							{
								mInvalidateQueue.wait();
								if (!mInvalidateQueue.isEmpty())
									rect.set(mInvalidateQueue.poll());
							}
							else
								rect.set(mInvalidateQueue.poll());
						}
					//Log.log("thread - rect %d %d %d %d", rect.left, rect.top, rect.width(), rect.height());
					synchronized (mLock)
					{
						Canvas canvas = null;
						recalcRect(rect);
						if (rect.isEmpty())
							canvas = mSurfaceHolder.lockCanvas(null);
						else
							canvas = mSurfaceHolder.lockCanvas(rect);
						if (canvas != null)
						{
							//Log.log("thread2 - rect %d %d %d %d", rect.left, rect.top, rect.width(), rect.height());
							drawSurface(canvas, rect);
							mSurfaceHolder.unlockCanvasAndPost(canvas);
						}
					}
				}
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}



