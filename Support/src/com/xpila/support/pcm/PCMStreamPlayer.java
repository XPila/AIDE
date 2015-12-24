package com.xpila.support.pcm;


import java.io.InputStream;
import java.io.IOException;


public class PCMStreamPlayer
extends PCMThreadedPlayer
{
	protected InputStream mIn = null;
	protected byte[] mBuffer = null;
	protected int mChunkSize = 0;
	@Override public boolean open(int streamType, PCMFormat format, int bufferSize)
	{ return open(streamType, format, bufferSize, 0); }
	public boolean open(int streamType, PCMFormat format, int bufferSize, int chunkSize)
	{
		if (!super.open(streamType, format, bufferSize)) return false;
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
	public void connect(InputStream in)
	{ mIn = in; }
	public int getChunkSize()
	{ return mChunkSize; }
	public void setChunkSize(int chunkSize)
	{ mChunkSize = chunkSize; }
	protected void playing()
	{
		int readed = 0;
		if (mIn != null)
			try
			{ readed = mIn.read(mBuffer, 0, mChunkSize); }
			catch (IOException e)
			{}
		if (readed > 0)
			mTrack.write(mBuffer, 0, readed);
		if (readed < mChunkSize)
			mPlaying = false;
	}
}
