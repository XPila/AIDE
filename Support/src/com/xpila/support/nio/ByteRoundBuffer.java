package com.xpila.support.nio;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


public class ByteRoundBuffer
{
	protected byte[] mArray = null;
	protected int mCapacity = 0;
	protected int mPosition = 0;
	protected int mAvailable = 0;
	public ByteRoundBuffer(byte[] array)
	{
		mArray = array;
		mCapacity = mArray.length;
		mPosition = 0;
		mAvailable = 0;
	}
	public int capacity()
	{
		return mCapacity;
	}
	public int position()
	{
		return mPosition;
	}
	public ByteRoundBuffer position(int newPosition)
	{
		mPosition = newPosition;
		return this;
	}
	public int available()
	{
		return mAvailable;
	}
	public ByteRoundBuffer available(int newAvailable)
	{
		mAvailable = newAvailable;
		return this;
	}
	public ByteRoundBuffer put(byte val) 
	{
		mArray[mPosition++] = val;
		if (mPosition >= mCapacity) mPosition = 0;
 		boolean overflow = (++mAvailable > mCapacity);
		if (overflow) throw new BufferOverflowException();
		return this;
	}
	public ByteRoundBuffer put(int pos, byte val)
	{
		mArray[pos] = val;
		return this;
	}
	public ByteRoundBuffer put(byte[] src)
	{
		return put(src, 0, src.length);
	}
	public ByteRoundBuffer put(byte[] src, int srcOffset, int count)
	{
		boolean overflow = false;
		int free = mCapacity - mAvailable;
		if (count > free)
		{
			count = free;
			overflow = true;
		}
		int toend = mCapacity - mPosition;
		if (toend >= count)
			System.arraycopy(src, srcOffset, mArray, mPosition, count);
		else
		{
			System.arraycopy(src, srcOffset, mArray, mPosition, toend);
			System.arraycopy(src, srcOffset + toend, mArray, 0, count - toend);
		}
		mPosition += count;
		mAvailable += count;
		if (mPosition >= mCapacity) mPosition -= mCapacity;
		if (overflow) throw new BufferOverflowException();
		return this;
	}
	public byte get()
	{
		if (mAvailable <= 0) throw new BufferUnderflowException();
		return (mPosition >= mAvailable)?mArray[mPosition - (mAvailable--)]:mArray[mCapacity + mPosition - (mAvailable--)];
	}
	public byte get(int pos)
	{
		return mArray[pos];
	}
	public ByteRoundBuffer get(byte[] dst)
	{
		return get(dst, 0, dst.length);
	}
	public ByteRoundBuffer get(byte[] dst, int dstOffset, int count)
	{
		boolean underflow = false;
		if (count > mAvailable)
		{
			count = mAvailable;
			underflow = true;
		}
		int srcOffset = (mPosition >= mAvailable)?mPosition - mAvailable:mCapacity + mPosition - mAvailable;
		int toend = mCapacity - srcOffset;
		if (toend >= count)
			System.arraycopy(mArray, srcOffset, dst, dstOffset, count);
		else
		{
			System.arraycopy(mArray, srcOffset, dst, dstOffset, toend);
			System.arraycopy(mArray, 0, dst, dstOffset + toend, count - toend);
		}
		mAvailable -= count;
		if (underflow) throw new BufferUnderflowException();
		return this;
	}
}

