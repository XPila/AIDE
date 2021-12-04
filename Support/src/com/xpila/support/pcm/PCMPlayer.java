package com.xpila.support.pcm;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class PCMPlayer
{
	public static final int STREAM_MUSIC = AudioManager.STREAM_MUSIC;
	protected PCMFormat mFormat;
	protected AudioTrack mTrack;
	protected int mBufferSize = 0;
	protected float mVolume = 0;
	public int getMinBufferSize(PCMFormat format)
	{ return AudioTrack.getMinBufferSize(format.frequency, PCMFormat.channelConfigOut(format.channels), PCMFormat.audioFormat(format.bits)); }
	public boolean open(int streamType, PCMFormat format, int bufferSize)
	{
		close();
		int sampleRateInHz = format.frequency;
		int channelConfig = PCMFormat.channelConfigOut(format.channels);
		int audioFormat = PCMFormat.audioFormat(format.bits);
		int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		if (bufferSize <= minBufferSize) bufferSize = minBufferSize;
		mTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
		mFormat = format;
		mBufferSize = bufferSize;
		return true;
	}
	public void close()
	{
		if (mTrack != null)
			mTrack.release();
		mTrack = null;
		mFormat = null;
		mBufferSize = 0;
	}
	public boolean pause()
	{
		mTrack.pause();
		return (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED);
	}
	public boolean play()
	{
		mTrack.play();
		return (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING);
	}
	public float getLatency()
	{ return (float)mBufferSize / (mFormat.frequency * mFormat.channels * mFormat.bits / 8); }
	public int getPlaybackRate()
	{ return mTrack.getPlaybackRate(); }
	public void setPlaybackRate(int playbackRate)	
	{ mTrack.setPlaybackRate(playbackRate); }
	public float getVolume()
	{ return mVolume; }
	public void setVolume(float volume)	
	{
		mVolume = volume;
		float leftVolume = mVolume;
		float rightVolume = mVolume;
		mTrack.setStereoVolume(leftVolume, rightVolume);
	}
	public int getAudioSessionId()
	{ return mTrack.getAudioSessionId(); }
	public int attachAuxEffect(int effectId)
	{ return mTrack.attachAuxEffect(effectId); }
}
