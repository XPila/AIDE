package com.xpila.support.xbus;

import com.xpila.support.xbus.XBusPort;
import com.xpila.support.xbus.XBusCycler;


public class XBusSlave
extends XBusCycler
{
	public XBusPort port = null;
	public int nodeID = 0;
	protected void cycle()
	{
		XBusPort.Message msg = port.new Message();
		if (port.rxMessage(msg))
			onMessage(msg);
	}
	protected boolean onMessage(XBusPort.Message msg)
	{
		if ((msg.nodeID != nodeID) && (nodeID != 0)) return false;
		if (msg.rdFlag && !msg.wrFlag)
		{
			msg.rdFlag = false;
			msg.data = new byte[msg.dataSize];
			if (onRdObject(msg.nodeID, msg.msgID, msg.data))
				return port.txMessage(msg);
		}
		else if (!msg.rdFlag && msg.wrFlag)
			return onWrObject(msg.nodeID, msg.msgID, msg.data);
		else if (!msg.rdFlag && !msg.wrFlag)
			return onCommand(msg.nodeID, msg.msgID, msg.data);
		return false;
	}
	protected boolean onRdObject(int nodeID, int msgID, byte[] data)
	{
		return false;
	}
	protected boolean onWrObject(int nodeID, int msgID, byte[] data)
	{
		return false;
	}
	protected boolean onCommand(int nodeID, int msgID, byte[] data)
	{
		return false;
	}
}
	

