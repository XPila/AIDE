package com.xpila.support.pcm;


import android.media.AudioRecord;


public class PCMSimpleRecorder
extends PCMRecorder
implements IPCMProducer
{
	public boolean start()
	{	mRecord.startRecording();
		return (mRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING); }
	public boolean stop()
	{	mRecord.stop();
		return (mRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED); }
	public boolean pause()
	{	mRecord.stop();
		return (mRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED); }
	public IPCMData outputData()
	{ return null; }	
	public PCMFormat outputFormat()
	{ return mFormat; }
	public int read(Object buffer, int position, int size)
	{
		if (mFormat.bits == 8)
			return mRecord.read((byte[])buffer, position, size);
		else if (mFormat.bits == 16)
			return mRecord.read((short[])buffer, position, size);
		return 0;
	}
}
