package com.xpila.support.pcm;


public interface IPCMConsumer
{
	public IPCMData inputData();
	public PCMFormat inputFormat();
	public int write(Object buffer, int position, int size);
}


