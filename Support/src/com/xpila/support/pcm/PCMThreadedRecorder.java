package com.xpila.support.pcm;


import android.media.AudioRecord;


public abstract class PCMThreadedRecorder
extends PCMRecorder
{
	protected RecorderThread mThread = null;
	protected boolean mRecording = false;
	protected boolean mResult = false;
	protected PCMThreadedPlayer mSlavePlayer = null;
	public boolean start()
	{
		if (mRecording) return true;
		mThread = new RecorderThread();
		mThread.start();
		synchronized (mThread)
		{ try
			{ mThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); } }
		return mResult;
	}
	public boolean stop()
	{
		if (!mRecording) return true;
		mRecording = false;
		try
		{ mThread.join(); }
		catch (InterruptedException e)
		{ e.printStackTrace(); }
		mThread = null;
		return mResult;
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
			}
			stopRecording();
			if (mSlavePlayer != null)
				mSlavePlayer.stopPlaying();
		}
	}
}
