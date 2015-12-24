package com.xpila.support.xbus;


public abstract class XBusPort
{
	public class Message
	{
		public int nodeID = 0;
		public int msgID = 0;
		public int dataSize = 0;
		public boolean rdFlag = false;
		public boolean wrFlag = false;
		public byte[] data = null;
	}
	public abstract boolean rxMessage(Message message);
	public abstract boolean txMessage(Message message);
}
	
