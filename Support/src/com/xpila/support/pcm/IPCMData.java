package com.xpila.support.pcm;


public interface IPCMData
{
	public long getLength();
	public boolean setLength(long newLength);
	public long getPosition();
	public boolean setPosition(long newPosition);
}

