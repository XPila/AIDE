package com.xpila.support.xbus;

import com.xpila.support.xbus.XBusPort;
import com.xpila.support.xbus.XBusCycler;


public class XBusMaster
extends XBusCycler
{
	public XBusPort port = null;
	public int nodeID = 0;
	protected void cycle()
	{

	}
	public boolean rdObject(int nodeID, int msgID, byte[] data, int position, int size)
	{
		XBusPort.Message msg = port.new Message();
		msg.nodeID = nodeID;
		msg.msgID = msgID;
		msg.dataSize = size;
		msg.rdFlag = true;
		msg.wrFlag = false;
		if (!port.txMessage(msg)) return false;
		if (!port.rxMessage(msg)) return false;
		System.arraycopy(msg.data, 0, data, position, size);
		return true;
	}
	public boolean wrObject(int nodeID, int msgID, byte[] data, int position, int size)
	{
		XBusPort.Message msg = port.new Message();
		msg.nodeID = nodeID;
		msg.msgID = msgID;
		msg.dataSize = size;
		msg.rdFlag = false;
		msg.wrFlag = true;
		msg.data = new byte[size];
		System.arraycopy(data, position, msg.data, 0, size);
		return port.txMessage(msg);
	}
	public boolean command(byte nodeID, short msgID, byte[] data, int position, int size)
	{
		XBusPort.Message message = port.new Message();
		message.nodeID = nodeID;
		message.msgID = msgID;
		message.dataSize = size;
		message.rdFlag = false;
		message.wrFlag = false;
		message.data = new byte[size];
		System.arraycopy(data, position, message.data, 0, size);
		return port.txMessage(message);
	}
}

