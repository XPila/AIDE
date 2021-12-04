package com.xpila.support.pcm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.lang.Integer;
import java.lang.Short;
import com.xpila.support.pcm.PCMFormat;
import com.xpila.support.pcm.IPCMConsumer;
import com.xpila.support.pcm.IPCMProducer;
import com.xpila.support.log.Log;
import java.nio.*;

	
public class PCMWaveFile
implements IPCMConsumer, IPCMProducer
{
	public File mFile = null;
	public RandomAccessFile mIn = null;
	public PCMWaveFileData mInPCMData = null;
	public RandomAccessFile mOut = null;
	public PCMWaveFileData mOutPCMData = null;
	public Header mHeader = null;
	public long mDataOffset = 0;
	public long mLen = 0;
	public long mPosRd = 0;
	public long mPosWr = 0;
	public PCMFormat mFormat = null;
	public ByteBuffer mRdByteBuffer = null;
	public ShortBuffer mRdShortBuffer = null;
	public ByteBuffer mWrByteBuffer = null;
	public ShortBuffer mWrShortBuffer = null;
	public PCMWaveFile()
	{
	}
	public boolean create(File file, PCMFormat format)
	{
		mFile = file;
		try
		{
			mFormat = format;
			mHeader = new Header();
			mHeader.setupPCM(mFormat.channels, mFormat.frequency, mFormat.bits);			
			mOut = new RandomAccessFile(mFile, "rw");
			mOutPCMData = new PCMWaveFileData(mOut);
			mIn = new RandomAccessFile(mFile, "r");
			mInPCMData = new PCMWaveFileData(mIn);
			Log.log("writeHeader %d", writeHeader());
			Log.log("readHeader %d", readHeader());
			mDataOffset = mIn.getFilePointer();
			return true;
		}
		catch (IOException e) { e.printStackTrace(); }
		return false;
	}
	public boolean open(File file, String mode)
	{
		mFile = file;
		if (mode.compareTo("r") == 0)
		{
			if (!mFile.exists()) return false;
			try
			{
				mIn = new RandomAccessFile(mFile, "r");
				mInPCMData = new PCMWaveFileData(mIn);
				mOut = null;
				mOutPCMData = null;
				mHeader = new Header();
				Log.log("readHeader %d", readHeader());
				mDataOffset = mIn.getFilePointer();				
				mFormat = new PCMFormat(mHeader.mBitsPerSample, mHeader.mNumChannels, mHeader.mSampleRate);
				return true;
			}
			catch (IOException e) { e.printStackTrace(); }
		}
		else if (mode.compareTo("rw") == 0)
		{
			try
			{
				mIn = new RandomAccessFile(mFile, "rw");
				mInPCMData = new PCMWaveFileData(mIn);
				mOut = new RandomAccessFile(mFile, "rw");
				mOutPCMData = new PCMWaveFileData(mOut);
				return true;
			}
			catch (IOException e) { e.printStackTrace(); }
		}
/*		if (mFile.exists())
		{
		}
		else
			try {
			if (mFile.createNewFile())
				mOut = new RandomAccessFile(mFile, mode);
			} catch (IOException e) { e.printStackTrace(); }*/
		return false;
	}
	public void close()
	{
		if (mIn != null)
			try { mIn.close(); } catch (IOException e) { e.printStackTrace(); }
		if (mIn == mOut) mOut = null;
		mIn = null;
		if (mOut != null)
			try { mOut.close(); } catch (IOException e) { e.printStackTrace(); }
		mOut = null;
		mFile = null;
	}
	public int writeHeader()
	{
		if (mOut == null) return 0;
		try
		{
			long pos = mOut.getFilePointer();
			mOut.seek(0);
			int ret = mHeader.write(mOut);
			mOut.seek(pos);
			return ret;
		}
		catch (IOException e) { e.printStackTrace(); }
		return 0;
	}
	public int readHeader()
	{
		if (mIn == null) return 0;
		try
		{
			long pos = mIn.getFilePointer();
			mIn.seek(0);
			int ret = mHeader.read(mIn);
			mIn.seek(pos);
			return ret;
		}
		catch (IOException e) { e.printStackTrace(); }
		return 0;
	}
	public void allocRdBuffer(int size)
	{
		mRdByteBuffer = ByteBuffer.allocate(2 * size);
		mRdByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		mRdShortBuffer = mRdByteBuffer.asShortBuffer();
	}
	public void allocWrBuffer(int size)
	{
		mWrByteBuffer = ByteBuffer.allocate(2 * size);
		mWrByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		mWrShortBuffer = mWrByteBuffer.asShortBuffer();
	}
	public IPCMData inputData()
	{ return mInPCMData; }
	public PCMFormat inputFormat()
	{ return mFormat; }
	public int write(Object buffer, int position, int size)
	{
		if (mOut == null) return 0;
		try
		{
			if (mFormat.bits == 8)
			{
				mOut.write((byte[])buffer, position, size);
				return size;
			}
			else if (mFormat.bits == 16)
			{
				if ((mWrShortBuffer == null) || (mWrShortBuffer.capacity() < size))
					allocWrBuffer(size);
				else
					mWrShortBuffer.rewind();
				mWrShortBuffer.put((short[])buffer, position, size);
				mOut.write(mWrByteBuffer.array(), 0, 2 * size);
				return size;
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		return 0;
	}
	public IPCMData outputData()
	{ return mOutPCMData; }	
	public PCMFormat outputFormat()
	{ return mFormat; }
	public int read(Object buffer, int position, int size)
	{
		if (mIn == null) return 0;
		try
		{
			if (mFormat.bits == 8)
				return mIn.read((byte[])buffer, position, size);
			else if (mFormat.bits == 16)
			{
				if ((mRdShortBuffer == null) || (mRdShortBuffer.capacity() < size))
					allocRdBuffer(size);
				else
					mRdShortBuffer.rewind();
				mIn.read(mRdByteBuffer.array(), 0, 2 * size);
				mRdShortBuffer.get((short[])buffer, position, size);
				return size;
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		return 0;
	}
	
	public class Header
	{
		public final static int CHUNKID_RIFF = 0x52494646; //"RIFF"
		public final static int FORMAT_WAVE = 0x57415645; //"WAVE"
		public final static int SUBCHUNK1ID_FMT = 0x666d7420; //"fmt "
		public final static int SUBCHUNK1SIZE_PCM = 16;
		public final static int AUDIOFORMAT_PCM = 1;
		public final static int SUBCHUNK2ID_DATA = 0x64617461; //"data"
		public int mChunkID = 0;
		public int mChunkSize = 0;
		public int mFormat = 0;
		public int mSubChunk1ID = 0;
		public int mSubChunk1Size = 0;
		public short mAudioFormat = 0;
		public short mNumChannels = 0;
		public int mSampleRate = 0;
		public int mByteRate = 0;
		public short mBlockAlign = 0;
		public short mBitsPerSample = 0;
		public int mSubChunk2ID = 0;
		public int mSubChunk2Size = 0;
		public Header()
		{
		}
		public void setupPCM(int numChannels, int sampleRate, int bitsPerSample)
		{
			mChunkID = CHUNKID_RIFF;
			mChunkSize = 0;
			mFormat = FORMAT_WAVE;
			mSubChunk1ID = SUBCHUNK1ID_FMT;
			mSubChunk1Size = SUBCHUNK1SIZE_PCM;
			mAudioFormat = AUDIOFORMAT_PCM;
			mNumChannels = (short)numChannels;
			mSampleRate = sampleRate;
			mByteRate = sampleRate * numChannels * bitsPerSample / 8;
			mBlockAlign = (short)(numChannels * bitsPerSample / 8);
			mBitsPerSample = (short)bitsPerSample;
			mSubChunk2ID = SUBCHUNK2ID_DATA;
			mSubChunk2Size = 0;
			
			mChunkSize = 4 + (8 + mSubChunk1Size) + (8 + mSubChunk2Size);
			
		}
		public void setLen(int len)
		{
			mSubChunk2Size = mBlockAlign * len;
			mChunkSize = 4 + (8 + mSubChunk1Size) + (8 + mSubChunk2Size);			
		}
		public int read(DataInput din)
		{
			int size = 0;
			try
			{
				mChunkID = din.readInt(); size += 4;
				if (mChunkID != CHUNKID_RIFF) return -size;
				mChunkSize = Integer.reverseBytes(din.readInt()); size += 4;
				mFormat = din.readInt(); size += 4;
				if (mFormat != FORMAT_WAVE) return -size;
				mSubChunk1ID = din.readInt(); size += 4;
				if (mSubChunk1ID != SUBCHUNK1ID_FMT) return -size;
				mSubChunk1Size = Integer.reverseBytes(din.readInt()); size += 4;
				if (mSubChunk1Size != SUBCHUNK1SIZE_PCM) return -size;
				mAudioFormat = Short.reverseBytes(din.readShort()); size += 2;
				if (mAudioFormat != AUDIOFORMAT_PCM) return -size;
				mNumChannels = Short.reverseBytes(din.readShort()); size += 2;
				mSampleRate = Integer.reverseBytes(din.readInt()); size += 4;
				mByteRate = Integer.reverseBytes(din.readInt()); size += 4;
				mBlockAlign = Short.reverseBytes(din.readShort()); size += 2;
				mBitsPerSample = Short.reverseBytes(din.readShort()); size += 2;
				mSubChunk2ID = din.readInt(); size += 4;
				if (mSubChunk2ID != SUBCHUNK2ID_DATA) return -size;
				mSubChunk2Size = Integer.reverseBytes(din.readInt()); size += 4;
				//todo
				//check mChunkSize <= filesize - 4;
				//check mBlockAlign == (short)(numChannels * bitsPerSample / 8);
				//check mByteRate == sampleRate * numChannels * bitsPerSample / 8;
				//check mNumChannels == {1, 2, 4}
				//check mSampleRate == {8000, 11025, 22050, 44100, 48000, 96000, 192000}
				//check mSubchunk2Size <= filesize - datapos
				return size;
			}
			catch (IOException e) { e.printStackTrace(); }
			return -size;
		}
		public int write(DataOutput dout)
		{
			int size = 0;
			try
			{
				dout.writeInt(mChunkID); size += 4;
				dout.writeInt(Integer.reverseBytes(mChunkSize)); size += 4;
				dout.writeInt(mFormat); size += 4;
				dout.writeInt(mSubChunk1ID); size += 4;
				dout.writeInt(Integer.reverseBytes(mSubChunk1Size)); size += 4;
				dout.writeShort(Short.reverseBytes(mAudioFormat)); size += 2;
				dout.writeShort(Short.reverseBytes(mNumChannels)); size += 2;
				dout.writeInt(Integer.reverseBytes(mSampleRate)); size += 4;
				dout.writeInt(Integer.reverseBytes(mByteRate)); size += 4;
				dout.writeShort(Short.reverseBytes(mBlockAlign)); size += 2;
				dout.writeShort(Short.reverseBytes(mBitsPerSample)); size += 2;
				dout.writeInt(mSubChunk2ID); size += 4;
				dout.writeInt(Integer.reverseBytes(mSubChunk2Size)); size += 4;
				return size;
			}
			catch (IOException e) { e.printStackTrace(); }
			return -size;
		}
		public void print(PrintStream p)
		{
			p.printf("ChunkID=%08x\n", mChunkID);
			p.printf("ChunkSize=%d\n", mChunkSize);
			p.printf("Format=%08x\n", mFormat);
			p.printf("Subchunk1ID=%08x\n", mSubChunk1ID);
			p.printf("Subchunk1Size=%d\n", mSubChunk1Size);
			p.printf("AudioFormat=%d\n", mAudioFormat);
			p.printf("NumChannels=%d\n", mNumChannels);
			p.printf("SampleRate=%d [samples/s]\n", mSampleRate);
			p.printf("ByteRate=%d [bytes/s]\n", mByteRate);
			p.printf("BlockAlign=%d [bytes]\n", mBlockAlign);
			p.printf("BitsPerSample=%d [bits]\n", mBitsPerSample);
			p.printf("Subchunk2ID=%08x\n", mSubChunk2ID);
			p.printf("Subchunk2Size=%d\n", mSubChunk2Size);
		}
	}
	protected class PCMWaveFileData
	implements IPCMData
	{
		public RandomAccessFile mRAFile = null;		
		protected PCMWaveFileData(RandomAccessFile file)
		{
			mRAFile = file;		
		}
		public long getLength()
		{
			try
			{
				long fileLen = mRAFile.length();
				return (fileLen - mDataOffset) / mHeader.mBlockAlign;
			}
			catch (IOException e) { e.printStackTrace(); }
			return -1;
		}
		public boolean setLength(long newLength)
		{
			try
			{
				long fileLen = mRAFile.length();
				long newFileLen = mDataOffset + (newLength * mHeader.mBlockAlign);
				mRAFile.setLength(newFileLen);
				return true;
			}
			catch (IOException e) { e.printStackTrace(); }
			return false;
		}
		public long getPosition()
		{
			try
			{
				long filePos = mRAFile.getFilePointer();
				return (filePos - mDataOffset) / mHeader.mBlockAlign;
			}
			catch (IOException e) { e.printStackTrace(); }
			return -1;
		}
		public boolean setPosition(long newPosition)
		{
			try
			{
				long filePos = mRAFile.getFilePointer();
				long newFilePos = mDataOffset + (newPosition * mHeader.mBlockAlign);
				mRAFile.seek(newFilePos);
				return true;
			}
			catch (IOException e) { e.printStackTrace(); }
			return false;
		}		
	}
}


