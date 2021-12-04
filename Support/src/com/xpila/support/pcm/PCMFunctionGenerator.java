package com.xpila.support.pcm;


import java.util.Random;


public class PCMFunctionGenerator
implements IPCMProducer
{
	public final static int FUNC_NONE = 0;
	public final static int FUNC_SINE = 1;
	public final static int FUNC_TRIANGLE = 2;
	public final static int FUNC_SQUARE = 3;
	public final static int FUNC_NOISE = 4;
	protected PCMFormat mFormat = null;
	protected int[] mFunc = null;
	protected double[] mPhase = null;
	protected double[] mAmplitude = null;
	protected double[] mPeriode = null;
	protected double[] mRatio = null;
	protected byte[] mByteBuffer = null;
	protected short[] mShortBuffer = null;
	public int getFunc(int channel)
	{ return mFunc[channel]; }
	public void setFunc(int channel, int func)
	{ mFunc[channel] = func; }
	public double getFreq(int channel)
	{ return mFormat.frequency / mPeriode[channel]; }
	public void setFreq(int channel, double freq)
	{ mPeriode[channel] = mFormat.frequency / freq; }
	public double getAmplitude(int channel)
	{ return mAmplitude[channel]; }
	public void setAmplitude(int channel, double amplitude)
	{ mAmplitude[channel] = amplitude; }
	public double getRatio(int channel)
	{ return mRatio[channel]; }
	public void setRatio(int channel, double ratio)
	{ mRatio[channel] = ratio; }
	public boolean open(PCMFormat format)
	{
		mFormat = format;
		mFunc = new int[mFormat.channels];
		mPhase = new double[mFormat.channels];
		mAmplitude = new double[mFormat.channels];
		mPeriode = new double[mFormat.channels];
		mRatio = new double[mFormat.channels];
		if (mFormat.channels == 1)
		{
			mFunc[0] = FUNC_SINE;
			mPhase[0] = 0;
			mAmplitude[0] = 0.5;
			mPeriode[0] = mFormat.frequency / 1000;
			mRatio[0] = 0.5;
		}
		if (mFormat.channels == 2)
		{
			mFunc[0] = FUNC_SINE;
			mPhase[0] = 0;
			mAmplitude[0] = 0.5;
			mPeriode[0] = mFormat.frequency / 1000;
			mRatio[0] = 0.5;
			mFunc[1] = FUNC_TRIANGLE;
			mPhase[1] = 0;
			mAmplitude[1] = 0.5;
			mPeriode[1] = mFormat.frequency / 500;
			mRatio[1] = 0.5;
		}
		return true;
	}
	public IPCMData outputData()
	{ return null; }
	public PCMFormat outputFormat()
	{ return mFormat; }
	public int read(Object buffer, int position, int size)
	{
		for (int i = 0; i < mFormat.channels; i++)
			switch (mFunc[i])
			{
				case FUNC_NONE:
					if (mFormat.bits == 8)
						PCMfillSilence((byte[])buffer, position, size, mFormat.channels, i);
					else if (mFormat.bits == 16)
						PCMfillSilence((short[])buffer, position, size, mFormat.channels, i);
					break;
				case FUNC_SINE:
					if (mFormat.bits == 8)
						mPhase[i] += PCMfillSine((byte[])buffer, position, size, mFormat.channels, i, mPhase[i], mPeriode[i], mAmplitude[i] * 127, mRatio[i]);
					else if (mFormat.bits == 16)
						mPhase[i] += PCMfillSine((short[])buffer, position, size, mFormat.channels, i, mPhase[i], mPeriode[i], mAmplitude[i] * 32767, mRatio[i]);
					break;
				case FUNC_TRIANGLE:
					if (mFormat.bits == 8)
						mPhase[i] += PCMfillTriangle((byte[])buffer, position, size, mFormat.channels, i, mPhase[i], mPeriode[i], mAmplitude[i] * 127, mRatio[i]);
					else if (mFormat.bits == 16)
						mPhase[i] += PCMfillTriangle((short[])buffer, position, size, mFormat.channels, i, mPhase[i], mPeriode[i], mAmplitude[i] * 32767, mRatio[i]);
					break;
				case FUNC_SQUARE:
					if (mFormat.bits == 8)
						mPhase[i] += PCMfillSquare((byte[])buffer, position, size, mFormat.channels, i, mPhase[i], mPeriode[i], mAmplitude[i] * 127, mRatio[i]);
					else if (mFormat.bits == 16)
						mPhase[i] += PCMfillSquare((short[])buffer, position, size, mFormat.channels, i, mPhase[i], mPeriode[i], mAmplitude[i] * 32767, mRatio[i]);
					break;
				case FUNC_NOISE:
					if (mFormat.bits == 8)
						PCMfillNoise((byte[])buffer, position, size, mFormat.channels, i, mAmplitude[i] * 127);
					else if (mFormat.bits == 16)
						PCMfillNoise((short[])buffer, position, size, mFormat.channels, i, mAmplitude[i] * 32767);
					mPhase[i] = 0;
					break;
			}
		return size;
	}
	public static void PCMfillSilence(byte[] buffer, int position, int size, int channels, int channel)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = (byte)128;
	}
	public static void PCMfillSilence(short[] buffer, int position, int size, int channels, int channel)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = 0;
	}
	public static double PCMfillSine(byte[] buffer, int position, int size, int channels, int channel, double phase, double periode, double amplitude, double ratio)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = (byte)(amplitude * sine(2 * Math.PI * (phase + (double)i / periode), ratio) + 128);
		return (double)count / periode;
	}
	public static double PCMfillSine(short[] buffer, int position, int size, int channels, int channel, double phase, double periode, double amplitude, double ratio)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = (short)(amplitude * sine(2 * Math.PI * (phase + (double)i / periode), ratio));
		return (double)count / periode;
	}
	public static double PCMfillTriangle(byte[] buffer, int position, int size, int channels, int channel, double phase, double periode, double amplitude, double ratio)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = (byte)(amplitude * triangle(2 * Math.PI * (phase + (double)i / periode), ratio) + 128);
		return (double)count / periode;
	}
	public static double PCMfillTriangle(short[] buffer, int position, int size, int channels, int channel, double phase, double periode, double amplitude, double ratio)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = (short)(amplitude * triangle(2 * Math.PI * (phase + (double)i / periode), ratio));
		return (double)count / periode;
	}
	public static double PCMfillSquare(byte[] buffer, int position, int size, int channels, int channel, double phase, double periode, double amplitude, double ratio)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = (byte)(amplitude * square(2 * Math.PI * (phase + (double)i / periode), ratio) + 128);
		return (double)count / periode;
	}
	public static double PCMfillSquare(short[] buffer, int position, int size, int channels, int channel, double phase, double periode, double amplitude, double ratio)
	{
		int count = size / channels;
		for (int i = 0; i < count; i ++)
			buffer[position + channels * i + channel] = (short)(amplitude * square(2 * Math.PI * (phase + (double)i / periode), ratio));
		return (double)count / periode;
	}
	public static int PCMfillNoise(byte[] buffer, int position, int size, int channels, int channel, double amplitude, Random random)
	{
		int count = size / channels;
		//for (int i = 0; i < count; i ++)
		//	buffer[position + channels * i + channel] = (byte)(amplitude * (2 * random.nextFloat() - 1) + 128);
		//(byte)(amplitude * square(2 * Math.PI * (phase + (double)i / periode), ratio) + 128);
		return count;
	}
	public static int PCMfillNoise(short[] buffer, int position, int size, int channels, int channel, double amplitude, Random random)
	{
		int count = size / channels;
		//for (int i = 0; i < count; i ++)
		//	buffer[position + channels * i + channel] = (short)(amplitude * (2 * random.nextFloat() - 1));
		//(short)(amplitude * square(2 * Math.PI * (phase + (double)i / periode), ratio));
		return count;
	}
	public static int PCMfillNoise(byte[] buffer, int position, int size, int channels, int channel, double amplitude)
	{
		int count = size / channels;
		//for (int i = 0; i < count; i ++)
		//	buffer[position + channels * i + channel] = (byte)(amplitude * (2 * rnd.nextFloat() - 1) + 128);
		return count;
	}
	public static int PCMfillNoise(short[] buffer, int position, int size, int channels, int channel, double amplitude)
	{
		int count = size / channels;
		//for (int i = 0; i < count; i ++)
		//	buffer[position + channels * i + channel] = (short)(amplitude * (2 * rnd.nextFloat() - 1));
		return count;
	}
	public static double square(double angle)
	{
		if (angle >= 2 * Math.PI) angle -= 2 * Math.PI * (int)(angle / (2 * Math.PI));
		else if (angle < 0) angle += 2 * Math.PI * (int)(-angle / (2 * Math.PI));
		return ((Math.PI - angle) >= 0) ?1: -1;
	}
	public static double triangle(double angle)
	{
		angle += Math.PI / 2;
		if (angle >= Math.PI) angle -= 2 * Math.PI * (int)(angle / (2 * Math.PI));
		else if (angle < 0) angle += 2 * Math.PI * (int)(-angle / (2 * Math.PI));
		return 1 - Math.abs(2 * (angle - Math.PI) / Math.PI);
	}
	public static double ratio(double angle, double ratio)
	{
		if (angle >= 2 * Math.PI) angle -= 2 * Math.PI * (int)(angle / (2 * Math.PI));
		else if (angle < 0) angle += 2 * Math.PI * (int)(-angle / (2 * Math.PI));
		double anglePos = 2 * Math.PI * ratio;
		double angleNeg = 2 * Math.PI - anglePos;
		if (angle >= anglePos)
			return Math.PI + Math.PI * ((angle - anglePos) / angleNeg);
		return Math.PI * (angle / anglePos);
	}
	public static double square(double angle, double ratio)
	{ return square(ratio(angle, ratio)); }
	public static double triangle(double angle, double ratio)
	{ return triangle(ratio(angle, ratio)); }
	public static double sine(double angle, double ratio)
	{ return Math.sin(ratio(angle, ratio)); }

}

