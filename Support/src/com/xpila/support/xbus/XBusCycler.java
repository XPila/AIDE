package com.xpila.support.xbus;


public class XBusCycler
{
	protected XBusCyclerThread mThread = null;
	protected boolean mRunning = false;
	public void start()
	{
		if (mRunning) return;
		mThread = new XBusCyclerThread();
		mThread.start();
		synchronized (mThread)
		{ try
			{ mThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); } }
	}
	public void stop()
	{
		if (!mRunning) return;
		mRunning = false;
		try
		{ mThread.join(); }
		catch (InterruptedException e)
		{ e.printStackTrace(); }
		mThread = null;
	}
	protected void cycle()
	{

	}
	protected class XBusCyclerThread
	extends Thread
	{
		@Override public void run()
		{
			mRunning = true;
			synchronized (this)
			{ notify(); }
			while (mRunning)
				cycle();
		}
	}
}

