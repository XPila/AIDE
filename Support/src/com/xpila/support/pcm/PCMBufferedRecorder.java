package com.xpila.support.pcm;


public class PCMBufferedRecorder
extends PCMThreadedRecorder
{
	protected IPCMConsumer mOut = null;
	protected byte[] mByteBuffer = null;
	protected short[] mShortBuffer = null;
	protected int mChunkSize = 0;
	@Override public boolean open(int audioSource, PCMFormat format, int bufferSize)
	{ return open(audioSource, format, bufferSize, 0); }
	public boolean open(int audioSource, PCMFormat format, int bufferSize, int chunkSize)
	{
		if (!super.open(audioSource, format, bufferSize)) return false;
		if (chunkSize == 0) chunkSize = mBufferSize;
		mChunkSize = chunkSize;
		if (mFormat.bits == 8)
			mByteBuffer = new byte[mChunkSize];
		else if (mFormat.bits == 16)
			mShortBuffer = new short[mChunkSize / 2];
		return true;
	}
	@Override public void close()
	{
		super.close();
		mByteBuffer = null;
		mShortBuffer = null;
		mChunkSize = 0;
	}
	public void connect(IPCMConsumer out)
	{ mOut = out; }
	public int getChunkSize()
	{ return mChunkSize; }
	public void setChunkSize(int chunkSize)
	{ mChunkSize = chunkSize; }
	protected void recording()
	{
		int readed = 0;
		int written = 0;
		if (mFormat.bits == 8)
		{
			readed = mRecord.read(mByteBuffer, 0, mChunkSize);
			if (mOut != null)
				if (readed > 0)
					written = mOut.write(mByteBuffer, 0, readed);
			if (readed < mChunkSize)
				mRecording = false;
		}
		if (mFormat.bits == 16)
		{
			readed = mRecord.read(mShortBuffer, 0, mChunkSize / 2);
			if (mOut != null)
				if (readed > 0)
					written = mOut.write(mShortBuffer, 0, readed);
			if (readed < mChunkSize / 2)
				mRecording = false;
		}
		if (readed != written)
			mRecording = false;
	}
}
