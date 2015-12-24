package com.xpila.support.pcm;


import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.media.AudioRecord;


public class PCMRecorder
{
	public static final int SOURCE_MIC = MediaRecorder.AudioSource.MIC;
	protected PCMFormat mFormat = null;
	protected AudioRecord mRecord = null;
	protected int mBufferSize = 0;
	public int getMinBufferSize(PCMFormat format)
	{ return AudioRecord.getMinBufferSize(format.frequency, PCMFormat.channelConfigIn(format.channels), PCMFormat.audioFormat(format.bits)); }
	public boolean open(int audioSource, PCMFormat format, int bufferSize)
	{
		close();
		int sampleRateInHz = format.frequency;
		int channelConfig = PCMFormat.channelConfigIn(format.channels);
		int audioFormat = PCMFormat.audioFormat(format.bits);
		int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		if (bufferSize <= minBufferSize) bufferSize = minBufferSize;
		mRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSize);
		mFormat = format;
		mBufferSize = bufferSize;
		return true;
	}
	public void close()
	{
		if (mRecord != null)
			mRecord.release();
		mRecord = null;
		mFormat = null;
		mBufferSize = 0;
	}
	public float getLatency()
	{ return (float)mBufferSize / (mFormat.frequency * mFormat.channels * mFormat.bits / 8); }
}
