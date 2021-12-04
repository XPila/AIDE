package com.xpila.support.pcm;


import android.media.AudioRecord;
import com.xpila.support.log.Log;


public abstract class PCMThreadedRecorder
extends PCMRecorder
{
	protected RecorderThread mThread = null;
	protected boolean mRecording = false;
	protected boolean mResult = false;
	protected PCMThreadedPlayer mSlavePlayer = null;
	protected boolean mPauseLoop = false;
	public boolean start()
	{
		if (mRecording) return true;
		mThread = new RecorderThread();
		mThread.start();
		synchronized (mThread)
		{
			try { mThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return mResult;
	}
	public boolean stop()
	{
		if (!mRecording) return true;
		mRecording = false;
		try { mThread.join(); }
		catch (InterruptedException e)
		{ e.printStackTrace(); }
		mThread = null;
		return mResult;
	}
	public boolean pauseLoop()
	{
		if (!mRecording) return false;
		if (mPauseLoop) return true;
		mPauseLoop = true;
		synchronized (mThread)
		{
			try
			{
				mThread.wait();
				return true;
			}
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return false;		
	}
	public boolean continueLoop()
	{
		if (!mRecording) return false;
		if (!mPauseLoop) return true;
		synchronized (mThread)
		{
			mThread.notify();
			try
			{
				mThread.wait();
				return true;
			}
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return false;		
	}
	protected boolean startRecording()
	{
		mRecord.startRecording();
		mResult = (mRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);
		mRecording = mResult;
		return mResult;
	}
	protected boolean stopRecording()
	{
		if (mRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
			mRecord.stop();
		mResult &= (mRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED);
		return mResult;
	}
	protected abstract void recording();
	protected class RecorderThread
	extends Thread
	{
		@Override public void run()
		{
			Log.log("Recorder run");
			startRecording();
			if (mSlavePlayer != null)
				mSlavePlayer.startPlaying();
			synchronized (this)
			{ notify(); }
			while (mRecording)
			{
				recording();
				if (mSlavePlayer != null)
					mSlavePlayer.playing();
				if (mPauseLoop)
					synchronized (this)
					{
						Log.log("mPauseLoop = true");
						notify();
						try { wait(); }
						catch (InterruptedException e)
						{ e.printStackTrace(); }
						mPauseLoop = false;
						notify();
					}
			}
			stopRecording();
			if (mSlavePlayer != null)
				mSlavePlayer.stopPlaying();
			Log.log("Recorder end");
		}
	}
}
