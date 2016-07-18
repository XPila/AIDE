package com.xpila.aascope;

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
import android.widget.Button;

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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.xpila.support.pcm.PCMScopeView;
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
public class MainActivity
extends Activity
implements MDTDetector.IMDTListener
{
	private LinearLayout mllMenu = null;
	private LinearLayout mllView = null;
	private TextView mtvLevelDiv = null;
	private TextView mtvTimeDiv = null;
	private PCMScopeView mvScope = null;
	private Button mbtSamplingOn = null;
	private Button mbtSamplingOff = null;
	private TextView mtv = null;
	private PCMView mPCM = null;
	
	private PCMFormat mFormat = null;
	private PCMBufferedRecorder mRecorder = null;
	
	private MDTDetector mMDTDView = null;

	private static final int[] mLevelDivTable = {256, 512, 1024, 2048, 4096, 8192};
	private int mLevelDivIndex = mLevelDivTable.length - 1;
	private static final float[] mTimeDivTable = { 0.1F, 0.2F, 0.5F,    1,    2,    5,   10,   20,   50,  100,  200,  500, 1000};
	private int mTimeDivIndex = 3;
	private static final int[] mChunkSizeTable = {   44,   88,  221,  441,  882, 2205, 4410,  441,  441,  441,  441,  441,  441};

	private PCMSwitch mPCMSwitch = null;
	
	private boolean mSamplingEnabled = false;
	
	@Override protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		Log.init("/mnt/sdcard/" + getClass().getName() + ".log");		
		setContentView(R.layout.main);
		
		mllMenu = (LinearLayout) findViewById(R.id.llMenu);
		mllView = (LinearLayout) findViewById(R.id.llView);
		mtvTimeDiv = (TextView) findViewById(R.id.tvTimeDiv);
		mtvLevelDiv = (TextView) findViewById(R.id.tvLevelDiv);
		mvScope = (PCMScopeView) findViewById(R.id.vScope);
		mbtSamplingOn = (Button) findViewById(R.id.btSamplingOn);
		mbtSamplingOff = (Button) findViewById(R.id.btSamplingOff);
		mtv = (TextView) findViewById(R.id.tv);
		//mPCM = (PCMView) findViewById(R.id.vPCM);
		
		mMDTDView = new MDTDetector(this, mllView, MDTDetector.LEFT, 10, 10);
		
		mFormat = new PCMFormat(16, 2, 44100);
		mRecorder = new PCMBufferedRecorder();
		mPCMSwitch = new PCMSwitch();
		int minBufferSize = mRecorder.getMinBufferSize(mFormat);
		Log.log("minBufferSize = %d bytes", minBufferSize);
		int maxChunkSize = 0;
		for (int i = 0; i < mChunkSizeTable.length; i++)
		{
			int chunkSize = mFormat.channels * mFormat.bits * mChunkSizeTable[i] / 8;
			if (maxChunkSize < chunkSize)
				maxChunkSize = chunkSize;
		}
		if (minBufferSize < maxChunkSize)
			minBufferSize = maxChunkSize;
		mRecorder.open(1, mFormat, minBufferSize, maxChunkSize);
		mPCMSwitch.open(mFormat);
		mvScope.open(mFormat);
		mtvLevelDiv.setText(Integer.toString(mLevelDivTable[mLevelDivIndex]));
		mvScope.setLevelDiv(mLevelDivTable[mLevelDivIndex]);
		mtvTimeDiv.setText(Float.toString(mTimeDivTable[mTimeDivIndex]));
		mvScope.setTimeDiv(mTimeDivTable[mTimeDivIndex]);		
		//mRecorder.connect(mvScope);
		mRecorder.connect(mPCMSwitch);
		mPCMSwitch.connect(mvScope);
		mRecorder.setChunkSize(mFormat.channels * mFormat.bits * mChunkSizeTable[mTimeDivIndex] / 8);
		//mPCM.open(mFormat);
	}

	@Override protected void onDestroy()
	{
		super.onDestroy();
		mRecorder.close();
	}
	@Override protected void onStart()
	{
		super.onStart();
		//mRecorder.start();
	}
	@Override protected void onStop()
	{
		super.onStop();
		if (mSamplingEnabled)
			mRecorder.stop();
	}
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		return false;
	}
				  
	public void SamplingOn(View view)
	{
		if (!mSamplingEnabled)
		{
			mRecorder.start();
			mbtSamplingOn.setBackgroundColor(0xff804040);
			mbtSamplingOff.setBackgroundColor(0xff404040);
//			mbtOnOff.setText("Off");
		}
		mSamplingEnabled = !mSamplingEnabled;

	}
	public void SamplingOff(View view)
	{
		if (mSamplingEnabled)
		{
			mRecorder.stop();
			mbtSamplingOn.setBackgroundColor(0xff404040);
			mbtSamplingOff.setBackgroundColor(0xff804040);
//			mbtOnOff.setText("On");
		}
		mSamplingEnabled = !mSamplingEnabled;

	}
	public void LevelDivMinus(View view)
	{
		if (mLevelDivIndex > 0)
		{
			mLevelDivIndex--;
			mtvLevelDiv.setText(Integer.toString(mLevelDivTable[mLevelDivIndex]));
			mvScope.setLevelDiv(mLevelDivTable[mLevelDivIndex]);
		}
	}
	public void LevelDivPlus(View view)
	{
		if (mLevelDivIndex < mLevelDivTable.length - 1)
		{
			mLevelDivIndex++;
			mtvLevelDiv.setText(Integer.toString(mLevelDivTable[mLevelDivIndex]));
			mvScope.setLevelDiv(mLevelDivTable[mLevelDivIndex]);
		}
	}
	public void TimeDivMinus(View view)
	{
		if (mTimeDivIndex > 0)
		{
			mTimeDivIndex--;
			mtvTimeDiv.setText(Float.toString(mTimeDivTable[mTimeDivIndex]));
			int chunkSize = mFormat.channels * mFormat.bits * mChunkSizeTable[mTimeDivIndex] / 8;
			Log.log("chunkSize = %d bytes", chunkSize);
			mRecorder.setChunkSize(chunkSize);
			mRecorder.pauseLoop();
			mvScope.setTimeDiv(mTimeDivTable[mTimeDivIndex]);
			mRecorder.continueLoop();
		}
	}
	public void TimeDivPlus(View view)
	{
		if (mTimeDivIndex < mTimeDivTable.length - 1)
		{
			mTimeDivIndex++;
			mtvTimeDiv.setText(Float.toString(mTimeDivTable[mTimeDivIndex]));
			int chunkSize = mFormat.channels * mFormat.bits * mChunkSizeTable[mTimeDivIndex] / 8;
			Log.log("chunkSize = %d bytes", chunkSize);
			mRecorder.setChunkSize(chunkSize);
			mRecorder.pauseLoop();
			mvScope.setTimeDiv(mTimeDivTable[mTimeDivIndex]);
			mRecorder.continueLoop();
		}
	}
	public void onDragMargin(View view, int margin, boolean outside)
	{
		if ((view == mllView) && (margin == MDTDetector.LEFT))
		{
			mllMenu.setVisibility(outside?View.GONE:View.VISIBLE);
		}
	}
	public void Test(View view)
	{
		mPCMSwitch.setState(!mPCMSwitch.getState());
		//mvScope.invalidateSurface(null);
	}
}

class PCMSwitch
implements IPCMConsumer
{
	protected PCMFormat mFormat = null;
	protected IPCMConsumer mConsumer =  null;
	protected boolean mState = false;
	public boolean open(PCMFormat format)
	{
		mFormat = format;
		return true;
	}
	public IPCMData inputData()
	{ return null; }
	public PCMFormat inputFormat()
	{ return mFormat; }
	public int write(Object buffer, int position, int size)
	{
		if (mState && (mConsumer != null))
			mConsumer.write(buffer, position, size);
		return size;
	}
	public void connect(IPCMConsumer consumer)
	{
		mConsumer = consumer;
	}
	public boolean getState()
	{
		return mState;
	}
	public void setState(boolean state)
	{
		mState = state;
	}
}
	
class PCMScopeTrigger
implements IPCMConsumer
{
	public static final int NEGATIVE = -1;
	public static final int FREE = 0;
	public static final int POSITIVE = 1;
	protected PCMFormat mFormat = null;
	protected int mMode = FREE;
	protected short mLevel = 0;
	protected boolean mStarted = false;
	protected IPCMConsumer mConsumer =  null;
	public boolean open(PCMFormat format)
	{
		return true;
	}
	public IPCMData inputData()
	{ return null; }
	public PCMFormat inputFormat()
	{ return mFormat; }
	public int write(Object buffer, int position, int size)
	{
		return size;
	}
	public void connect(IPCMConsumer mConsumer)
	{
		
	}
	public static int findEdge(short[] data, int position, int size, int stride, short lastSample, short level, int polarity)
	{
		for (int i = 0; i < size; i++)
		{
			short sample = data[position + i];
			if ((lastSample <= level) && (sample >= level))
				return i;
			lastSample = sample;
		}
		return -1;
	}
	
}
	
class CtlTrigger
extends FrameLayout
{
	public CtlTrigger(Context context)
	{
		super(context);
		initCtlTrigger(context);
	}
	public CtlTrigger(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		initCtlTrigger(context);
	}
	protected void initCtlTrigger(Context context)
	{
		//String displaytype = attrs.getAttributeValue(null, "displaytype");
		//if (displaytype == null) displaytype = "hvga";
		//if (displaytype.compareTo("hvga") == 0) inflate(context, R.layout.ctl_trigger, this);
		//else if (displaytype.compareTo("xga") == 0) inflate(context, R.layout.ctl_trigger, this);
		inflate(context, R.layout.ctl_trigger, this);
	}
}
