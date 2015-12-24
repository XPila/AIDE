package com.xpila.support.pcm;


import java.io.OutputStream;
import java.io.IOException;


public class PCMStreamRecorder
extends PCMThreadedRecorder
{
	protected OutputStream mOut = null;
	protected byte[] mBuffer = null;
	protected int mChunkSize = 0;
	@Override public boolean open(int audioSource, PCMFormat format, int bufferSize)
	{ return open(audioSource, format, bufferSize, 0); }
	public boolean open(int audioSource, PCMFormat format, int bufferSize, int chunkSize)
	{
		if (!super.open(audioSource, format, bufferSize)) return false;
		if (chunkSize == 0) chunkSize = mBufferSize;
		mChunkSize = chunkSize;
		mBuffer = new byte[mChunkSize];
		return true;
	}
	@Override public void close()
	{
		super.close();
		mBuffer = null;
		mChunkSize = 0;
	}
	public void connect(OutputStream out)
	{ mOut = out; }
	public int getChunkSize()
	{ return mChunkSize; }
	public void setChunkSize(int chunkSize)
	{ mChunkSize = chunkSize; }
	protected void recording()
	{
		int readed = 0;
		readed = mRecord.read(mBuffer, 0, mChunkSize);
		if (mOut != null)
			if (readed > 0)
				try
				{ mOut.write(mBuffer, 0, readed); }
				catch (IOException e)
				{}
		if (readed < mChunkSize)
			mRecording = false;
	}
}
