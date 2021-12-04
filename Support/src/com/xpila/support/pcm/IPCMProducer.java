package com.xpila.support.pcm;


public interface IPCMProducer
{
	public IPCMData outputData();	
	public PCMFormat outputFormat();
	public int read(Object buffer, int position, int size);
}


