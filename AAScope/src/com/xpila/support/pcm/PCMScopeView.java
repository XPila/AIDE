package com.xpila.support.pcm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import android.transition.*;
import java.io.*;
import android.graphics.*;
import com.xpila.support.log.Log;
import com.xpila.support.pcm.PCMFormat;
import com.xpila.support.pcm.IPCMData;
import com.xpila.support.pcm.IPCMConsumer;
import com.xpila.support.pcm.IPCMProducer;
import com.xpila.support.pcm.PCMBufferedRecorder;
import com.xpila.support.pcm.PCMFunctionGenerator;
import com.xpila.support.view.MDTDetector;
import com.xpila.support.pcm.PCMView;
import com.xpila.support.nio.ByteRoundBuffer;
import com.xpila.support.nio.ShortRoundBuffer;
import com.xpila.support.nio.FloatRoundBuffer;
import com.xpila.support.os.CPULoadMonitor;

/*
XXHDPI 1   144
LDPI   2/9 32
MDPI   1/3 48
HDPI   4/9 64
XHDPI  2/3 96
*/
public class PCMScopeView
extends PCMView
implements IPCMConsumer, IPCMProducer
{
	
	protected int mColorInfo = 0xff00c0c0;
	protected Paint mPaintInfo = null;

	protected Object mRoundBuffer = null;
	protected int mRoundBufferCapacity = 0;
	protected int mRoundBufferAvailable = 0;
	protected ByteRoundBuffer mRoundByteBuffer = null;
	protected ShortRoundBuffer mRoundShortBuffer = null;
	protected CPULoadMonitor mCPULoad = null;
	protected long mLastFrameTime = 0;
	protected FloatSumFilter mFramesPerSecFilter = null;
	protected long mLastWriteTime = 0;
	protected long mLastWriteSize = 0;
	protected FloatSumFilter mWritesPerSecFilter = null;
	protected FloatSumFilter mBytesPerSecFilter = null;
	protected float mTimeDiv = 0; //time per div in seconds
	protected int mLevelDiv = 0; //level
	
	protected IPCMProducer mProducer = null;
	protected IPCMData mProducerData = null;
	protected long mViewPosition = 0;
	protected long mViewSize = 0;
	
	public PCMScopeView(Context context)
	{
		super(context);
		Log.log("PCMScopeView-constructor");
		initPCMScopeView();
	}
	public PCMScopeView(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		Log.log("PCMScopeView-constructor");
		initPCMScopeView();
	}
	protected void initPCMScopeView()
	{
		Log.log("PCMScopeView-init");
		mLock = new ReentrantLock();
		mPaintInfo = new Paint();
		mPaintInfo.setColor(mColorInfo);
		mPaintInfo.setStyle(Style.FILL_AND_STROKE);
		mCPULoad = new CPULoadMonitor(100);
		mCPULoad.start();
		mFramesPerSecFilter = new FloatSumFilter(100);
		mWritesPerSecFilter = new FloatSumFilter(200);
		mBytesPerSecFilter = new FloatSumFilter(200);
		
	}	
	public boolean open(PCMFormat format)
	{
		Log.log("PCMScopeView-open");
		synchronized (mLock)
		{
			if (!super.open(format)) return false;
			mRoundBufferCapacity = 10 * mFormat.frequency * mFormat.channels;
			mRoundBufferAvailable = 0;
			if (mFormat.bits == 8)
			{			
				mRoundByteBuffer = new ByteRoundBuffer(new byte[mRoundBufferCapacity]);
				mRoundBuffer = mRoundByteBuffer;
			}
			else if (mFormat.bits == 16)
			{
				mRoundShortBuffer = new ShortRoundBuffer(new short[mRoundBufferCapacity]);
				mRoundBuffer = mRoundShortBuffer;
			}
			mTimeDiv = (float)mSamplesPerScreen / (mFormat.frequency * mGridDivsX);
			mLevelDiv = mLevelPerScreen / mGridDivsY;
		}
		return true;
	}
	public void close()
	{
		synchronized (mLock)
		{
		}
	}
	public boolean connect(IPCMProducer producer)
	{
		synchronized (mLock)
		{
			mProducer = producer;
			mProducerData = (mProducer != null)?mProducer.outputData():null;
			if (mProducerData != null)
			{
				mViewSize = mProducerData.getLength();
				mViewPosition = 0;
				mDrawStart = 0;
				mDrawSize = mScreenSize;
				invalidateSurface(null);
			}
		}
		return true;
	}
	public float getTimeDiv()
	{ return mTimeDiv; }
	public void setTimeDiv(float timeDiv)
	{
		Log.log("setTimeDiv %.2f [ms]", timeDiv);
		synchronized (mLock)
		{
			mTimeDiv = timeDiv;
			setSamplesPerScreen((int)(mFormat.frequency * mTimeDiv * mGridDivsX / 1000 + 0.5));
		}
		Log.log("setTimeDiv after lock");
		synchronized (mRoundBuffer)
		{
			if (mFormat.bits == 8)
				mRoundByteBuffer.available(0);
			else if (mFormat.bits == 16)
				mRoundShortBuffer.available(0);
		}
		invalidateSurface(null);
		Log.log("setTimeDiv end");
	}
	public int getLevelDiv()
	{ return mLevelDiv; }
	public void setLevelDiv(int levelDiv)
	{
		synchronized (mLock)
		{
			mLevelDiv = levelDiv;
			mLevelPerScreen = mLevelDiv * mGridDivsY;
			mFactorY = (float)mHeight / mLevelPerScreen;
		}
		invalidateSurface(null);
	}
	public int getScreenSize()
	{
		return mScreenSize;		
	}
	// ThreadedSurfaceView
	@Override public void drawSurface(Canvas canvas, Rect rect)
	{
		// Measure framerate
		long time = System.nanoTime() / 1000;
		long delay = time - mLastFrameTime;
		if (mLastFrameTime != 0)
			mFramesPerSecFilter.put((float)delay / 1000000);
		mLastFrameTime = time;
		
		//Log.log("drawSurface %d %d %d %d", rect.left, rect.top, rect.width(), rect.height());
		super.drawSurface(canvas, rect);
//		if (mDrawStart == 0)
			drawInfo(canvas);
		if (mProducer == null)
		{
			mDrawStart += mDrawSize;
			mDrawSize = 0;
			if (mDrawStart >= mScreenSize)
				mDrawStart = 0;
		}
	}
	public void recalcRect(Rect rect)
	{
		if (mProducer == null)
		{
			synchronized (mRoundBuffer)
			{
				int freeSize = mScreenSize - mDrawStart;
				if (mFormat.bits == 8)
					mDrawSize = mRoundByteBuffer.available();
				else if (mFormat.bits == 16)
					mDrawSize = mRoundShortBuffer.available();
				while (mDrawSize > freeSize)
				{
					mDrawStart = 0;
					mDrawSize -= freeSize;
					freeSize = mScreenSize;
					if (mFormat.bits == 8)
						mRoundByteBuffer.available(mDrawSize);
					else if (mFormat.bits == 16)
						mRoundShortBuffer.available(mDrawSize);
				}
				if (mFormat.bits == 8)
					mRoundByteBuffer.get(mByteDrawData, mDrawStart, mDrawSize);
				else if (mFormat.bits == 16)
					mRoundShortBuffer.get(mShortDrawData, mDrawStart, mDrawSize);
			}
		}
	}
	// IPCMProducer
	public IPCMData outputData()
	{ return null; }
	public PCMFormat outputFormat()
	{ return mFormat; }
	public int read(Object buffer, int position, int size)
	{
		return size;
	}
	// IPCMConsumer
	public IPCMData inputData()
	{ return null; }
	public PCMFormat inputFormat()
	{ return mFormat; }
	public int write(Object buffer, int position, int size)
	{
		// Measure write frequency
		long time = System.nanoTime() / 1000;
		long delay = time - mLastWriteTime;
		if (mLastWriteTime != 0)
		{
			mWritesPerSecFilter.put((float)delay / 1000000);
			if (mFormat.bits == 8)
				mBytesPerSecFilter.put((float)delay / (1000000 * size));
			else if (mFormat.bits == 16)
				mBytesPerSecFilter.put((float)delay / (2000000 * size));
		}
		mLastWriteTime = time;
		mLastWriteSize = size;
		// Write sample data to roundbuffer
		synchronized (mRoundBuffer)
		{ 
			mRoundBufferAvailable += size;
			if (mRoundBufferAvailable > mRoundBufferCapacity)
				mRoundBufferAvailable = mRoundBufferCapacity;
			if (mFormat.bits == 8)
				mRoundByteBuffer.put((byte[])buffer, position, size);
			else if (mFormat.bits == 16)
				mRoundShortBuffer.put((short[])buffer, position, size);
		}
		// Invalidate surface
		invalidateSurface(null);
		return size;
	}
	
	protected void drawInfo(Canvas canvas)
	{
		canvas.drawText(String.format("time/div: %5.1f [ms]", mTimeDiv), 4, 20, mPaintInfo);
		canvas.drawText(String.format("level/div: %5d [1]", mLevelDiv), 4, 32, mPaintInfo);
		canvas.drawText(String.format("Framerate: %5.1f [FPS]", 1F / mFramesPerSecFilter.avg()), 4, 44, mPaintInfo);
		canvas.drawText(String.format("Blockrate: %5.1f [Blocks/s]", 1F / mWritesPerSecFilter.avg()), 4, 56, mPaintInfo);
		canvas.drawText(String.format("Byterate: %6d [Bytes/s]", (int)(1F / mBytesPerSecFilter.avg())), 4, 68, mPaintInfo);
		canvas.drawText(String.format("CPULoad: %3.1f [%%]", mCPULoad.getCPULoad() * 100), 4, 80, mPaintInfo);
		canvas.drawText(String.format("pos: %d", mRoundShortBuffer.position()), 4, 92, mPaintInfo);
		canvas.drawText(String.format("screensize: %d", mScreenSize), 4, 104, mPaintInfo);
		canvas.drawText(String.format("chunksize: %d", mLastWriteSize), 4, 116, mPaintInfo);
		canvas.drawText(String.format("Buffer: %.1f", (float)100 * mRoundBufferAvailable / mRoundBufferCapacity), 4, 128, mPaintInfo);
	}
	
	class RoundBufferPCMData
	implements IPCMData
	{
		protected long mPosition = 0;
		public long getLength()
		{ synchronized (mRoundBuffer) { return mRoundBufferAvailable; } }
		public boolean setLength(long newLength) { return false; }
		public long getPosition() { return mPosition; }
		public boolean setPosition(long newPosition) { mPosition = newPosition; return true; }
	}
}

class FloatSumFilter
{
	protected float[] mArray = null;
	protected FloatRoundBuffer mBuffer = null;
	protected float mSum = 0;
	FloatSumFilter(int length)
	{
		mArray = new float[length];
		mBuffer = new FloatRoundBuffer(mArray);
		mSum = 0;
	}
	public synchronized int count()
	{
		return mBuffer.available();
	}
	public synchronized float sum()
	{
		return mSum;
	}
	public synchronized float avg()
	{
		return mSum / mBuffer.available();
	}
	public synchronized void put(float value)
	{
		if (mBuffer.available() == mBuffer.capacity())
			mSum -= mBuffer.get();
		mBuffer.put(value);
		mSum += value;
	}
	public synchronized void reset()
	{
		mSum = 0;
		mBuffer.available(0);
	}
}

