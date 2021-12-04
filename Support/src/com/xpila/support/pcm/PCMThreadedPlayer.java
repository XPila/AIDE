package com.xpila.support.pcm;


import android.media.AudioTrack;
import com.xpila.support.log.Log;


public abstract class PCMThreadedPlayer
extends PCMPlayer
{
	protected PlayerThread mThread = null;
	protected boolean mPlaying = false;
	protected boolean mPaused = false;
	protected boolean mPause = false;
	protected boolean mPlay = false;
	protected boolean mResult = false;
	protected PCMThreadedPlayer mSlavePlayer = null;
	protected PCMThreadedRecorder mSlaveRecorder = null;
	
	public PCMThreadedPlayer getSlavePlayer() { return mSlavePlayer; }
	public void setSlavePlayer(PCMThreadedPlayer slavePlayer) { mSlavePlayer = slavePlayer; }
	public PCMThreadedRecorder getSlaveRecorder() { return mSlaveRecorder; };
	public void setSlaveRecorder(PCMThreadedRecorder slaveRecorder) { mSlaveRecorder = slaveRecorder; };
	
	public boolean start()
	{
//		Log.log("PCMThreadedPlayer::start()");
		if (mPlaying) return true;
		mThread = new PlayerThread();
		mThread.start();
		synchronized (mThread)
		{
			try
			{ mThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return mPlaying;
	}
	public boolean stop()
	{
		Log.log("PCMThreadedPlayer::stop()");
		if (!mPlaying) return true;
		mPlaying = false;
		try
		{ mThread.join(); }
		catch (InterruptedException e)
		{ e.printStackTrace(); }
		mThread = null;
		return mResult;
	}
	public boolean pause()
	{
		Log.log("PCMThreadedPlayer::pause()");
		if (!mPlaying)
		{
			mPaused = true;
			return true;
		}
		if (mPaused) return true;
		mPause = true;
		synchronized (mThread)
		{
			try
			{ mThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return mPaused;
	}
	public boolean play()
	{
		Log.log("PCMThreadedPlayer::play()");		
		if (!mPlaying) return false;
		if (!mPaused) return true;
		mPlay = true;
		synchronized (mThread)
		{
			try
			{ mThread.wait(); }
			catch (InterruptedException e)
			{ e.printStackTrace(); }
		}
		return mPlaying && !mPaused;
	}
	protected boolean startPlaying()
	{
		Log.log("PCMThreadedPlayer::startPlaying()");
		boolean res = false;
		int playState = mTrack.getPlayState();
		if (playState == AudioTrack.PLAYSTATE_STOPPED)
			Log.log("PCMThreadedPlayer::startPlaying() PLAYSTATE_STOPPED");
		else if (playState == AudioTrack.PLAYSTATE_PAUSED)
			Log.log("PCMThreadedPlayer::startPlaying() PLAYSTATE_PAUSED");		
		if ((playState == AudioTrack.PLAYSTATE_STOPPED) ||
			(playState == AudioTrack.PLAYSTATE_PAUSED))
		{
			mTrack.play();
			res = (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING);
			Log.log("PCMThreadedPlayer::startPlaying() res1 = %d", res?1:0);
			if (mSlavePlayer != null)
				res &= mSlavePlayer.startPlaying();
			Log.log("PCMThreadedPlayer::startPlaying() res2 = %d", res?1:0);
			if (mSlaveRecorder != null)
				res &= mSlaveRecorder.startRecording();
			Log.log("PCMThreadedPlayer::startPlaying() res3 = %d", res?1:0);
			if (res)
			{
//				if (playState == AudioTrack.PLAYSTATE_STOPPED)
					mPlaying = true;
//				else if (playState == AudioTrack.PLAYSTATE_PAUSED)
					mPaused = false;
			}
		}
		return res;
	}
	protected boolean stopPlaying()
	{
		Log.log("PCMThreadedPlayer::stopPlaying()");
		boolean res = false;
		if (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
		{
			mTrack.stop();
			res = (mTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED);
			if (mSlavePlayer != null)
				res &= mSlavePlayer.stopPlaying();
			if (mSlaveRecorder != null)
				res &= mSlaveRecorder.stopRecording();
			if (res) mPlaying = false;
		}
		return res;
	}
	protected boolean pausePlaying()
	{
		Log.log("PCMThreadedPlayer::pausePlaying()");
		boolean res = false;
		if (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
		{
			mTrack.pause();
			res = (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED);
			if (mSlavePlayer != null)
				res &= mSlavePlayer.pausePlaying();
			//if (mSlaveRecorder != null)
			//	res = mSlaveRecorder.stopRecording();
			if (res) mPaused = true;
		}
		return res;
	}
	protected abstract void playing();
	protected class PlayerThread
	extends Thread
	{
		@Override public void run()
		{
			if (!mPaused)
				mPlaying = startPlaying();
			else
				mPlaying = true;
			synchronized (this)
			{ notify(); }
			while (mPlaying)
			{
				if (!mPaused)
				{
					playing();
					if (mPause)
					{
						pausePlaying();
						mPause = false;
						synchronized (this)
						{ notify(); }
					}
				}
				else
				{
					try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
					if (mPlay)
					{
						startPlaying();
						mPlay = false;
						synchronized (this)
						{ notify(); }
					}
				}
			}
			mResult = stopPlaying();
		}
	}
}
