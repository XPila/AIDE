package com.xpila.support.os;

//import java.io.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;



public class CPULoadMonitor
{
	public interface Listener
	{
	public void onCPULoadNotify(float cpuLoad);
	}
	protected int mPeriode_ms = 0;
	protected MonitorThread mThread = null;	
	protected RandomAccessFile mStat = null;
	protected boolean mRunning = false;
	protected long mIdle = 0;
	protected long mCPU = 0;
	protected float mCPULoad = 0;
	protected Listener mListener = null;
	public CPULoadMonitor(int periode_ms)
	{
		mPeriode_ms = periode_ms;
	}
	public CPULoadMonitor(int periode_ms, Listener listener)
	{
		mPeriode_ms = periode_ms;
		mListener = listener;
	}
	public float getCPULoad()
	{
		return mCPULoad;
	}
	public boolean start()
	{
		if (mRunning) return true;
		try
		{			
			mStat = new RandomAccessFile("/proc/stat", "r");
		}
		catch (IOException e) { e.printStackTrace(); return false; }
		mThread = new MonitorThread();
		mThread.start();
		synchronized (mThread)
		{
			try
			{ mThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return mRunning;
	}
	public boolean stop()
	{
		if (!mRunning) return true;
		mRunning = false;
		try
		{ mThread.join(); }
		catch (InterruptedException e)
		{ e.printStackTrace(); }
		mThread = null;
		try { mStat.close(); } catch (IOException e) { e.printStackTrace(); }
		mStat = null;		
		return true;
	}
	protected boolean update()
	{
		if (mStat != null)
		{
			try
			{
				mStat.seek(0);
				String line = mStat.readLine();
				if (line != null)
				{
					String[] toks = line.split(" ");
					long idle = Long.parseLong(toks[5]);
					long cpu = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
					if ((mIdle != 0) && (mCPU != 0))
					{
						mCPULoad = (float)(cpu - mCPU) / ((cpu + idle) - (mCPU + mIdle));
						if (mListener != null) mListener.onCPULoadNotify(mCPULoad);
					}
					mIdle = idle;
					mCPU = cpu;
					return true;
				}
			}
			catch (IOException e) { e.printStackTrace(); }
		}		
		return false;
	}
	protected class MonitorThread
	extends Thread
	{
		@Override public void run()
		{
			mRunning = true;
			synchronized (this)
			{ notify(); }
			while (mRunning)
			{
				try { Thread.sleep(mPeriode_ms); } catch (InterruptedException e) { e.printStackTrace(); }
				if (!update()) break;
//				synchronized (this)
//				{ notify(); }
			}
		}
	}
}
	

