package com.xpila.support.ahw.auart;


import java.io.*;
import com.xpila.support.io.*;
import com.xpila.support.pcm.*;


public class AUART
{
	public class AUARTConfig
	{
		public int baudRate = 9600;
		public int startBits = 1;
		public int dataBits = 8;
		public int stopBits = 1;
		public boolean MSBFirst = false;
	}
	abstract class AUARTEncoder
	{
		private AUARTConfig mCfg = null;
		private int mSampleRate = 0;
		private int mMSB = 0;
		private double mSamplesPerBaud = 0;
		private int mTotalSamples = 0;
		public AUARTEncoder(AUARTConfig cfg, int sampleRate)
		{
			mCfg = cfg;
			mSampleRate = sampleRate;
			mMSB = 1 << (mCfg.dataBits - 1);
			mSamplesPerBaud = (double)mSampleRate / mCfg.baudRate;
			mTotalSamples = (int)((mCfg.startBits + mCfg.dataBits + mCfg.stopBits) * mSamplesPerBaud + 0.5);
		}
		public int getTotalSamples()
		{ return mTotalSamples; }
		public void writeData(int data)
		{
			for (int i = 0; i < mTotalSamples; i ++)
			{
				int bit = (int)(i / mSamplesPerBaud) - mCfg.startBits;
				if (bit < 0)
					writeSample(true);
				else if (bit < mCfg.dataBits)
				{
					if (mCfg.MSBFirst)
						writeSample(((data & (mMSB >> bit)) == 0));
					else
						writeSample(((data & (1 << bit)) == 0));
				}
				else
					writeSample(false);
			}
		}
		protected abstract void writeSample(boolean sample);
	}
	abstract class AUARTDecoder
	{
		private AUARTConfig mCfg = null;
		private int mSampleRate = 0;
		private int mMSB = 0;
		private double mBaudsPerSample = 0;
		private int mTotalSamples = 0;
		private boolean mStarted = false;
		private double mTime = 0;
		private double mBit = 0;
		private int mData = 0;
		public AUARTDecoder(AUARTConfig cfg, int sampleRate)
		{
			mCfg = cfg;
			mSampleRate = sampleRate;
			mMSB = 1 << (mCfg.dataBits - 1);
			mBaudsPerSample = (double)mCfg.baudRate / mSampleRate;
			mTotalSamples = (int)((mCfg.startBits + mCfg.dataBits + mCfg.stopBits) / mBaudsPerSample + 0.5);
		}
		public int getTotalSamples()
		{ return mTotalSamples; }
		public void writeSample(boolean sample)
		{
			if (!mStarted && sample)
			{
				mStarted = true;
				mTime = -mCfg.startBits;
				mBit = 0;
				mData = 0;
			}
			if (mStarted)
			{
				if (mTime < 0)
					mStarted &= ((mTime > ((double)-mCfg.startBits / 2)) || sample);
				else if (mTime < mCfg.dataBits)
				{
					if (sample) mBit += mBaudsPerSample;
					if ((int)(mTime + mBaudsPerSample) > (int)(mTime))
					{
						if (mCfg.MSBFirst)
							mData = (mData << 1) | ((mBit > 0.5)?0:1);
						else
							mData = (mData >> 1) | ((mBit > 0.5)?0:mMSB);
						mBit = 0;
					}
				}
				else
				{
					if (sample) mBit += mBaudsPerSample;
					if (mTime >= (mCfg.dataBits + (double)mCfg.stopBits / 2))
					{
						if (mBit < 0.25)
							writeData(mData);
						mStarted = false;
					}
				}
				mTime += mBaudsPerSample;
			}
		}
		protected abstract void writeData(int data);
	}
	public class AUARTPCMEncoder
	extends AUARTEncoder
	{
		private PCMFormat mFormat = null;
		private int mChannel = 0;
		private byte mByte0 = 0;
		private byte mByte1 = 127;
		private short mShort0 = 0;
		private short mShort1 = 32767;
		private boolean mInverted = false;
		private boolean mDiffMode = false;
		private boolean mLastSample = false;
		private Object mBuffer = null;
		private int mPosition = 0;
		public AUARTPCMEncoder(AUARTConfig cfg, PCMFormat format, int channel)
		{
			super(cfg, format.frequency);
			mFormat = format;
			mChannel = channel;
		}
		protected void writeSample(boolean sample)
		{
			if (mFormat.bits == 8)
			{
				if (mDiffMode)
				{
					if (sample == mLastSample)
						writeSample(mByte0);
					else
						writeSample(mInverted?(sample?(byte)-mByte1:mByte1):(sample?mByte1:(byte)-mByte1));
				}
				else
					writeSample(mInverted?(sample?(byte)-mByte1:mByte0):(sample?mByte1:mByte0));
			}
			else if (mFormat.bits == 16)
			{
				if (mDiffMode)
				{
					if (sample == mLastSample)
						writeSample(mShort0);
					else
						writeSample(mInverted?(sample?(short)-mShort1:mShort1):(sample?mShort1:(short)-mShort1));
				}
				else
					writeSample(mInverted?(sample?(short)-mShort1:mShort0):(sample?mShort1:mShort0));
			}
			mLastSample = sample;
		}
		protected void writeSample(byte sample)
		{
			if (mBuffer == null) return;
			((byte[])mBuffer)[mPosition + mChannel] = (byte)(sample + 128);
			mPosition += mFormat.channels;
		}
		protected void writeSample(short sample)
		{
			if (mBuffer == null) return;
			((short[])mBuffer)[mPosition + mChannel] = sample;
			mPosition += mFormat.channels;
		}
		public void setOutBuffer(Object buffer, int position)
		{
			mBuffer = buffer;
			mPosition = position;
		}
	}
	public class AUARTPCMDecoder
	extends AUARTDecoder
	{
		private PCMFormat mFormat = null;
		private int mChannel = 0;
		private byte mByte0 = 0;
		private byte mByte1 = 127;
		private short mShort0 = 0;
		private short mShort1 = 32767;
		private boolean mInverted = true;
		private boolean mDiffMode = true;
		private boolean mLastSample = false;
		private byte mLastByteSample = 0;
		private short mLastShortSample = 0;
		private OutputStream mStream = null;
		public AUARTPCMDecoder(AUARTConfig cfg, PCMFormat format, int channel)
		{
			super(cfg, format.frequency);
			mFormat = format;
			mChannel = channel;
		}
		public void writeSample(byte sample)
		{
			if (mDiffMode)
			{
			}
			else
				writeSample((sample - 128) > (mInverted?(-mByte1 / 2):(mByte1 / 2)));
		}
		public void writeSample(short sample)
		{
			if (mDiffMode)
			{
				int delta = sample - mLastShortSample;
				if (delta > (mShort1 / 2))
					mLastSample = !mInverted;
				if (delta < (-mShort1 / 2))
					mLastSample = mInverted;
				writeSample(mLastSample);
			}
			else
				writeSample(sample < (mInverted?(-mShort1 / 2):(mShort1 / 2)));
			mLastShortSample = sample;
		}
		protected void writeData(int data)
		{
			if (mStream != null)
				try { mStream.write(data); }
				catch (IOException e) {}
		}
		public void setOutStream(OutputStream stream)
		{
			mStream = stream;
		}
	}
	public class AUARTEncDecTest
	{
		public AUARTConfig mCfg;
		public PCMFormat mFormat;
		public Pipe mPipe;
		public AUARTPCMEncoder mEnc;
		public AUARTPCMDecoder mDec;
		public short[] mBuffer;
		public AUARTEncDecTest()
		{
			mCfg = new AUARTConfig();
			mFormat = new PCMFormat(16, 1, 44100);
			mPipe = new Pipe(10);
			mEnc = new AUARTPCMEncoder(mCfg, mFormat, 0);
			mDec = new AUARTPCMDecoder(mCfg, mFormat, 0);
			mDec.setOutStream(mPipe.out);
			mBuffer = new short[1024];
		}
		public void write(int data)
		{
			mEnc.setOutBuffer(mBuffer, 0);
			mEnc.writeData(data);
			for (int i = 0; i < mEnc.getTotalSamples(); i++)
				mDec.writeSample(mBuffer[i]);
		}
	}
	
	public class AUARTPCMProducer
	implements IPCMProducer
	{
		private PCMFormat mFormat = null;
		private Pipe[] mPipe = null;
		private AUARTConfig[] mCfg = null;
		private AUARTPCMEncoder[] mEnc = null;
		public boolean open(PCMFormat format, int pipesize)
		{
			if (!format.isValid()) return false;
			mFormat = format;
			int channels = mFormat.channels;
			mPipe = new Pipe[channels];
			mCfg = new AUARTConfig[channels];
			mEnc = new AUARTPCMEncoder[channels];
			mPipe[0] = new Pipe(pipesize);
			mCfg[0] = new AUARTConfig();
			mEnc[0] = new AUARTPCMEncoder(mCfg[0], mFormat, 0);
			if (channels == 2)
			{
				mPipe[1] = new Pipe(pipesize);
				mCfg[1] = new AUARTConfig();
				mEnc[1] = new AUARTPCMEncoder(mCfg[1], mFormat, 1);
			}
			return true;
		}
		public void close()
		{
			mFormat = null;
			mPipe = null;
			mCfg = null;
			mEnc = null;
		}
		public int getSamplesPerByte(int channel)
		{ return mEnc[channel].getTotalSamples(); }
		public OutputStream getOutput(int channel)
		{ return mPipe[channel].out; }
		public IPCMData outputData()
		{ return null; }
		public PCMFormat outputFormat()
		{ return mFormat; }
		public int read(Object buffer, int position, int size)
		{
			int channels = mFormat.channels;
			int samples = size / channels;
			for (int channel = 0; channel < channels; channel ++)
			{
				if (mFormat.bits == 8)
					PCMFunctionGenerator.PCMfillSilence((byte[])buffer, position, size, channels, channel);
				else if (mFormat.bits == 16)
					PCMFunctionGenerator.PCMfillSilence((short[])buffer, position, size, channels, channel);
				mEnc[channel].setOutBuffer(buffer, position);
				int countBytes = 0;
				try { countBytes = mPipe[channel].in.available(); }
				catch (IOException e) {}
				int totalSamples = mEnc[channel].getTotalSamples();
				if (countBytes > 0)
				{
					int countSamples = totalSamples * countBytes;
					if (countSamples > samples)
						countBytes = samples / totalSamples;
					for (int i = 0; i < countBytes; i ++)
						try { mEnc[channel].writeData(mPipe[channel].in.read()); }
						catch (IOException e) {}
				}
			}
			return size;
		}
	}
	public class AUARTPCMConsumer
	implements IPCMConsumer
	{
		private PCMFormat mFormat = null;
		private Pipe[] mPipe = null;
		private AUARTConfig[] mCfg = null;
		private AUARTPCMDecoder[] mDec = null;
		public boolean open(PCMFormat format, int pipesize)
		{
			if (!format.isValid()) return false;
			mFormat = format;
			int channels = mFormat.channels;
			mPipe = new Pipe[channels];
			mCfg = new AUARTConfig[channels];
			mDec = new AUARTPCMDecoder[channels];
			mPipe[0] = new Pipe(pipesize);
			mCfg[0] = new AUARTConfig();
			mDec[0] = new AUARTPCMDecoder(mCfg[0], mFormat, 0);
			mDec[0].setOutStream(mPipe[0].out);
			if (channels == 2)
			{
				mPipe[1] = new Pipe(pipesize);
				mCfg[1] = new AUARTConfig();
				mDec[1] = new AUARTPCMDecoder(mCfg[1], mFormat, 1);
				mDec[1].setOutStream(mPipe[1].out);
			}
			return true;
		}
		public void close()
		{
			mFormat = null;
			mPipe = null;
			mCfg = null;
			mDec = null;
		}
		public int getSamplesPerByte(int channel)
		{ return mDec[channel].getTotalSamples(); }
		public InputStream getInput(int channel)
		{ return mPipe[channel].in; }
		public IPCMData inputData()
		{ return null; }		
		public PCMFormat inputFormat()
		{ return mFormat; }
		public int write(Object buffer, int position, int size)
		{
			//if (true) return size;
			int channels = mFormat.channels;
			int samples = size / channels;
			if (mFormat.bits == 8)
			{
				byte[] byteBuffer = (byte[])buffer;
				for (int channel = 0; channel < channels; channel ++)
					for (int i = 0; i < samples; i ++)
						mDec[channel].writeSample(byteBuffer[position + i * channels + channel]);
			}
			else if (mFormat.bits == 16)
			{
				short[] shortBuffer = (short[])buffer;
				for (int channel = 0; channel < channels; channel ++)
					for (int i = 0; i < samples; i ++)
						mDec[channel].writeSample(shortBuffer[position + i * channels + channel]);
			}
			return size;
		}
	}
	public class AUARTPCMOutput
	{
		private PCMFormat mFormat = null;
		private AUARTPCMProducer mProducer = null;
		private PCMBufferedPlayer mPlayer = null;
		public boolean open(PCMFormat format, int pipesize)
		{
			if (!format.isValid()) return false;
			mFormat = format;
			mProducer = new AUARTPCMProducer();
			mProducer.open(mFormat, pipesize);
			mPlayer = new PCMBufferedPlayer();
			int bufferSize = mPlayer.getMinBufferSize(mFormat);
			int chunkSize = mProducer.getSamplesPerByte(0) * mFormat.channels * mFormat.bits / 8;
			if (mFormat.channels == 2)
			{
				int chunkSize1 = mProducer.getSamplesPerByte(1) * mFormat.channels * mFormat.bits / 8;
				if (chunkSize1 > chunkSize)
					chunkSize = chunkSize1;
			}
			mPlayer.open(PCMPlayer.STREAM_MUSIC, mFormat, bufferSize, chunkSize);
			mPlayer.connect(mProducer);
			return true;
		}
		public void close()
		{
			if (mFormat == null) return;
			mProducer.close();
			mPlayer.close();
			mFormat = null;
			mProducer = null;
			mPlayer = null;
		}
		public float getLatency()
		{ return mPlayer.getLatency(); }
		public void start()
		{ mPlayer.start(); }
		public void stop()
		{ mPlayer.stop(); }
		public OutputStream getOutput()
		{ return mProducer.getOutput(0); }
		public OutputStream getOutput(int channel)
		{ return mProducer.getOutput(channel); }
		public void setSlave(AUARTPCMInput slave)
		{
		//	if (mPlayer != null)
		//		if (slave != null)
		//			mPlayer.mSlaveRecorder = slave.mRecorder;
		}
	}
	public class AUARTPCMInput
	{
		private PCMFormat mFormat = null;
		private AUARTPCMConsumer mConsumer = null;
		private PCMBufferedRecorder mRecorder = null;
		public boolean open(PCMFormat format, int pipesize)
		{
			if (!format.isValid()) return false;
			mFormat = format;
			mConsumer = new AUARTPCMConsumer();
			mConsumer.open(mFormat, pipesize);
			mRecorder = new PCMBufferedRecorder();
			int bufferSize = mRecorder.getMinBufferSize(mFormat);
			int chunkSize = mConsumer.getSamplesPerByte(0) * mFormat.channels * mFormat.bits / 8;
			if (mFormat.channels == 2)
			{
				int chunkSize1 = mConsumer.getSamplesPerByte(1) * mFormat.channels * mFormat.bits / 8;
				if (chunkSize1 > chunkSize)
					chunkSize = chunkSize1;
			}
			mRecorder.open(PCMRecorder.SOURCE_MIC, mFormat, bufferSize, chunkSize);
			mRecorder.connect(mConsumer);
			return true;
		}
		public void close()
		{
			if (mFormat == null) return;
			mConsumer.close();
			mRecorder.close();
			mConsumer = null;
			mRecorder = null;
		}
		public float getLatency()
		{ return mRecorder.getLatency(); }
		public void start()
		{ mRecorder.start(); }
		public void stop()
		{ mRecorder.stop(); }
		public InputStream getInput()
		{ return mConsumer.getInput(0); }
		public InputStream getInput(int channel)
		{ return mConsumer.getInput(channel); }
		public void setSlave(AUARTPCMOutput slave)
		{
		//	if (mRecorder != null)
		//		if (slave != null)
		//			mRecorder.mSlavePlayer = slave.mPlayer;
		}
	}
	
	public class AUART
	{
		
	}

}

