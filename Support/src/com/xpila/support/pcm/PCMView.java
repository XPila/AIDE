package com.xpila.support.pcm;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.xpila.support.log.Log;
import com.xpila.support.view.ThreadedSurfaceView;


public class PCMView
extends ThreadedSurfaceView
{
	protected int mWidth = 0; //width in pixels
	protected int mHeight = 0; //height in pixels	
	protected PCMFormat mFormat = null; //PCM format
	protected Bitmap mBitmap = null; //current screen bitmap
	protected Bitmap mBitmapBkgnd = null; //background bitmap (filled with mColorBkgnd and grid)
	protected Canvas mCanvas = null; //canvas for mBitmap
	protected Canvas mCanvasBkgnd = null; //canvas for mCanvasBkgnd
	protected int mColorBkgnd = 0; //background color
	protected int mColorGrid = 0; //grid color
	protected int[] mColorSamples = null; //sample colors for all channels
	protected Paint mPaintGrid = null; //grid paint
	protected Paint[] mPaintSamples = null; //sample paints for all channels
	protected int mGridDivsX = 0; //horizontal divs per screen
	protected int mGridDivsY = 0; //vertical divs per screen
	protected int mSamplesPerScreen = 0; //samples per screen
	protected int mLevelPerScreen = 0; //level per screen
	protected float mFactorX = 0; //horizontal pixel factor (=mW/mSamplesPerScreen)
	protected float mFactorY = 0; //vertical pixel factor (=mH/mLevelPerScreen)
	protected float[] mScreenPosY = null; //vertical origins for all channels
	protected boolean mLines = false; //sample painting method (false=pixels, true=lines)
	
	protected int mScreenSize = 0; //total size of screenbuffer (= mSamplesPerScreen * mFormat.channels)
	protected byte[] mByteDrawData = null; //8-bit screenbuffer
	protected short[] mShortDrawData = null; //16-bit screenbuffer
	protected int mDrawStart = 0; //offset of datablock in screenbuffer
	protected int mDrawSize = 0; //size of datablock in screenbuffer
	
	public PCMView(Context context)
	{
		super(context);
		Log.log("PCMView-constructor");
		initPCMView();
	}
	public PCMView(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		Log.log("PCMView-constructor");
		initPCMView();
	}
	protected void initPCMView()
	{
		Log.log("PCMView-init");
		mColorBkgnd = 0xff606060;
		mColorGrid = 0xff202020;
		mColorSamples = new int[2];
		mColorSamples[0] = 0xff00c000;
		mColorSamples[1] = 0xffc00000;
		mPaintGrid = new Paint();
		mPaintGrid.setColor(mColorGrid);
		mPaintGrid.setStyle(Style.STROKE);
		mPaintSamples = new Paint[2];
		mPaintSamples[0] = new Paint();
		mPaintSamples[1] = new Paint();
		mPaintSamples[0].setColor(mColorSamples[0]);
		mPaintSamples[0].setStyle(Style.STROKE);
//		mPaintSamples[0].setStrokeWidth(1); //slower drawing (default 0?)
		mPaintSamples[1].setColor(mColorSamples[1]);
		mPaintSamples[1].setStyle(Style.STROKE);
//		mPaintSamples[1].setStrokeWidth(1); //slower drawing (default 0?)
		mGridDivsX = 10;
		mGridDivsY = 8;
		mLines = true;
		//ThreadedSurfaceView
		mInvalidateRect = new Rect();
	}
	public boolean open(PCMFormat format)
	{
		synchronized (mLock)
		{
			mFormat = format;
			mSamplesPerScreen = mFormat.frequency / 10;
			mFactorX = (float)mWidth / mSamplesPerScreen;
			mFactorY = (float)mHeight / mLevelPerScreen;
			mScreenSize = mSamplesPerScreen * mFormat.channels;
			if (mFormat.bits == 8)
			{			
				mLevelPerScreen = 256;
				mByteDrawData = new byte[mScreenSize];
			}
			else if (mFormat.bits == 16)
			{
				mLevelPerScreen = 65536;
				mShortDrawData = new short[mScreenSize];
			}
			mScreenPosY = new float[mFormat.channels];
			if (mFormat.channels == 1)
				mScreenPosY[0] = 0;
			else if (mFormat.channels == 2)
			{
				mScreenPosY[0] = -0.25F;
				mScreenPosY[1] = 0.25F;
			}
			mDrawStart = 0;
			mDrawSize = mScreenSize;
			if (mFormat.bits == 8)
				PCMFunctionGenerator.PCMfillSine(mByteDrawData, 0, mDrawSize, 1, 0, 0, 1000, 64, 0.5);
			else if (mFormat.bits == 16)
				PCMFunctionGenerator.PCMfillSine(mShortDrawData, 0, mDrawSize, 1, 0, 0, 1000, 16384, 0.5);
		}
		return true;
	}	
	public int getSamplesPerScreen()
	{
		return mSamplesPerScreen;
	}
	public void setSamplesPerScreen(int samplesPerScreen)
	{
		synchronized (mLock)
		{
			mSamplesPerScreen = samplesPerScreen;
			mFactorX = (float)mWidth / mSamplesPerScreen;
			mScreenSize = mSamplesPerScreen * mFormat.channels;
			if (mFormat.bits == 8)
				mByteDrawData = new byte[mScreenSize];
			else if (mFormat.bits == 16)
				mShortDrawData = new short[mScreenSize];
			mDrawStart = 0;
		}
	}
	public int getLevelPerScreen()
	{
		return mLevelPerScreen;
	}
	public void setLevelPerScreen(int levelPerScreen)
	{
		synchronized (mLock)
		{
			mLevelPerScreen = levelPerScreen;
			mFactorY = (float)mHeight / mLevelPerScreen;			
			mDrawStart = 0;
		}
	}
	//View
	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		synchronized (mLock)
		{
			super.onSizeChanged(w, h, oldw, oldh);
			mWidth = w;
			mHeight = h;
			if (mSamplesPerScreen != 0)
				mFactorX = (float)mWidth / mSamplesPerScreen;
			if (mLevelPerScreen != 0)
				mFactorY = (float)mHeight / mLevelPerScreen;
			mBitmap = null;
			mBitmapBkgnd = null;
			mCanvas = null;
			mCanvasBkgnd = null;
			if (mWidth * mHeight > 0)
			{
				mBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
				mBitmapBkgnd = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
				mCanvas = new Canvas(mBitmap);
				mCanvasBkgnd = new Canvas(mBitmapBkgnd);
				drawBkgnd(mCanvasBkgnd);
			}
		}
	}
	//ThreadedSurfaceView
	@Override public void drawSurface(Canvas canvas, Rect rect)
	{ //called in drawing thread, synchronized by mLock
		//Log.log("drawSurface %d %d %d %d", rect.left, rect.top, rect.width(), rect.height());
		if (mDrawSize > 0)
		{
			if (mFormat.bits == 8)
			{
				if (mDrawStart == 0)
					mBitmap.eraseColor(0x00000000);
				drawData(mCanvas, mByteDrawData, mDrawStart, mDrawSize, mDrawStart * mFactorX / mFormat.channels);
			}
			else if (mFormat.bits == 16)
			{
				if (mDrawStart == 0)
					mBitmap.eraseColor(0x00000000);
				drawData(mCanvas, mShortDrawData, mDrawStart, mDrawSize, mDrawStart * mFactorX / mFormat.channels);
			}
		}
		if ((rect == null) || (rect.isEmpty()))
		{
			canvas.drawBitmap(mBitmapBkgnd, 0, 0, null);
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
		else
		{
			canvas.drawBitmap(mBitmapBkgnd, rect, rect, null);
			canvas.drawBitmap(mBitmap, rect, rect, null);
		}
	}
	//drawing subrutines
	protected void drawData(Canvas canvas, Object buffer, int position, int size, float x)
	{		
		for (int channel = 0; channel < mFormat.channels; channel++)
		{
			float y = (float)mHeight * (0.5F + mScreenPosY[channel]);
			if (mFormat.bits == 8)
				PCMGraphics.drawSamples(canvas, mPaintSamples[channel], (byte[])buffer, position, size, mFormat.channels, channel, x, y, mFactorX, mFactorY, mLines);
			else if (mFormat.bits == 16)
				PCMGraphics.drawSamples(canvas, mPaintSamples[channel], (short[])buffer, position, size, mFormat.channels, channel, x, y, mFactorX, mFactorY, mLines);
		}
	}
	protected void drawBkgnd(Canvas canvas)
	{
		canvas.drawColor(mColorBkgnd);
		PCMGraphics.drawGrid(canvas, mPaintGrid, mWidth, mHeight, mGridDivsX, mGridDivsY);
	}
}

