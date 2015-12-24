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
	public float getLatency()
	{ return (float)mBufferSize / (mFormat.frequency * mFormat.channels * mFormat.bits / 8); }
}
