package com.xpila.support.pcm;


public class PCMBufferedPlayer
extends PCMThreadedPlayer
{
	protected IPCMProducer mIn = null;
	protected byte[] mByteBuffer = null;
	protected short[] mShortBuffer = null;
	protected int mChunkSize = 0;
	protected boolean mWriteToSlave = false;
	@Override public boolean open(int streamType, PCMFormat format, int bufferSize)
	{ return open(streamType, format, bufferSize, 0); }
	public boolean open(int streamType, PCMFormat format, int bufferSize, int chunkSize)
	{
		if (!super.open(streamType, format, bufferSize)) return false;
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
	public void connect(IPCMProducer in)
	{ mIn = in; }
	public int getChunkSize()
	{ return mChunkSize; }
	public void setChunkSize(int chunkSize)
	{ mChunkSize = chunkSize; }
	public boolean getWriteToSlave()
	{ return mWriteToSlave; }
	public void setWriteToSlave(boolean writeToSlave)
	{ mWriteToSlave = writeToSlave; }
	protected void playing()
	{
		int readed = 0;
		int written = 0;
		if (mFormat.bits == 8)
		{
			if (mIn != null)
				readed = mIn.read(mByteBuffer, 0, mChunkSize);
			if (readed > 0)
				written = mTrack.write(mByteBuffer, 0, readed);
			if (mSlavePlayer != null)
			{
				if (mWriteToSlave)
					mSlavePlayer.mTrack.write(mByteBuffer, 0, readed);
				else
					mSlavePlayer.playing();
			}
			if (readed < mChunkSize)
				mPlaying = false;
		}
		else if (mFormat.bits == 16)
		{
			if (mIn != null)
				readed = mIn.read(mShortBuffer, 0, mChunkSize / 2);
			if (readed > 0)
				written = mTrack.write(mShortBuffer, 0, readed);
			if (mSlavePlayer != null)
			{
				if (mWriteToSlave)
					mSlavePlayer.mTrack.write(mShortBuffer, 0, readed);
				else
					mSlavePlayer.playing();
			}
			if (readed < mChunkSize / 2)
				mPlaying = false;
		}
		if (readed != written)
			mPlaying = false;
	}
}
