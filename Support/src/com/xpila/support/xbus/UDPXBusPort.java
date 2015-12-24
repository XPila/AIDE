package com.xpila.support.xbus;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.io.IOException;

import com.xpila.support.xbus.XBusPort;


public class UDPXBusPort
extends XBusPort
{
	protected DatagramSocket mSocket = null;
	protected SocketAddress mLocalAddr = null;
	protected SocketAddress mRemoteAddr = null;
	protected SocketAddress mLastRxRemoteAddr = null;
	public boolean open(SocketAddress local, SocketAddress remote)
	{
		mLocalAddr = local;
		mRemoteAddr = remote;
		try
		{
			mSocket = new DatagramSocket(mLocalAddr);
			//mSocket = new DatagramSocket();
			mSocket.setSoTimeout(2000);
			return true;
		}
		catch (SocketException e)
		{ e.printStackTrace(); }
		return false;
	}
	public void close()
	{
		if (mSocket != null)
			mSocket.close();
		mSocket = null;
		mLocalAddr = null;
		mRemoteAddr = null;
		mLastRxRemoteAddr = null;
	}
	public boolean rxMessage(Message msg)
	{
		System.out.printf("rxMessage\r\n");
		byte[] data = new byte[1 + 4 + 8];
		try
		{
			DatagramPacket packet = new DatagramPacket(data, data.length);
			mSocket.receive(packet);
			data = packet.getData();
			if (packet.getLength() < (1 + 4)) return false;
			if (data[0] != (byte)0xaa) return false;
			msg.nodeID = data[1];
			msg.msgID = (short)(data[2] | (data[3] << 8));
			msg.dataSize = ((data[4] & 8) != 0) ?(data[4] & 7) + 1: 0;
			msg.rdFlag = (data[4] & 16) != 0;
			msg.wrFlag = (data[4] & 32) != 0;
			int datasize = (!msg.rdFlag) ?msg.dataSize: 0;
			if (packet.getLength() != (1 + 4 + datasize)) return false;
			msg.data = new byte[datasize];
			for (int i = 0; i < datasize; i++)
				msg.data[i] = data[i + 5];
			mLastRxRemoteAddr = packet.getSocketAddress();
			return true;
		}
		catch (IOException e)
		{ e.printStackTrace(); }
		return false;
	}
	public boolean txMessage(Message msg)
	{
		System.out.printf("txMessage\r\n");
		int datasize = (!msg.rdFlag) ?(msg.dataSize): 0;
		byte[] data = new byte[1 + 4 + datasize];
		data[0] = (byte)0xaa;
		data[1] = (byte)msg.nodeID;
		data[2] = (byte)(msg.msgID & 0xff);
		data[3] = (byte)((msg.msgID >> 8) & 0xff);
		data[4] = (byte)(((msg.dataSize > 0) ?((msg.dataSize - 1) & 7) | 8: 0) | (msg.rdFlag ?16: 0) | (msg.wrFlag ?32: 0));
		for (int i = 0; i < datasize; i++)
			data[i + 5] = msg.data[i];
		try
		{
			DatagramPacket packet = new DatagramPacket(data, data.length, (mRemoteAddr != null) ?mRemoteAddr: mLastRxRemoteAddr);
			mSocket.send(packet);
			return true;
		}
		catch (SocketException e)
		{ e.printStackTrace(); }
		catch (IOException e)
		{ e.printStackTrace(); }
		return false;
	}
}

