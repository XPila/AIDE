package com.xpila.support.pcm;


import android.media.AudioTrack;


public abstract class PCMThreadedPlayer
extends PCMPlayer
{
	protected PlayerThread mThread = null;
	protected boolean mPlaying = false;
	protected boolean mResult = false;
	protected PCMThreadedRecorder mSlaveRecorder = null;
	public boolean start()
	{
		if (mPlaying) return true;
		mThread = new PlayerThread();
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
		if (!mPlaying) return true;
		mPlaying = false;
		try
		{ mThread.join(); }
		catch (InterruptedException e)
		{ e.printStackTrace(); }
		mThread = null;
		return mResult;
	}
	protected boolean startPlaying()
	{
		mTrack.play();
		mResult = (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING);
		mPlaying = mResult;
		return mResult;
	}
	protected boolean stopPlaying()
	{
		if (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
			mTrack.stop();
		mResult = (mTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED);
		return mResult;
	}
	protected abstract void playing();
	protected class PlayerThread
	extends Thread
	{
		@Override public void run()
		{
			startPlaying();
			if (mSlaveRecorder != null)
				mSlaveRecorder.startRecording();
			synchronized (this)
			{ notify(); }
			while (mPlaying)
			{
				playing();
				if (mSlaveRecorder != null)
					mSlaveRecorder.recording();
			}
			stopPlaying();
			if (mSlaveRecorder != null)
				mSlaveRecorder.stopRecording();
		}
	}
}
