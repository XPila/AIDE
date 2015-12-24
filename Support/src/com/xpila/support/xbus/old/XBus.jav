//package com.xpila.support.xbus;
package com.xpila.avplotter;
//import java.net.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.io.IOException;


public class XBus
{
	public static XBus x = new XBus();
	
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
			catch (SocketException e) { e.printStackTrace(); }
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
				msg.dataSize = ((data[4] & 8) != 0)?(data[4] & 7) + 1:0;
				msg.rdFlag = (data[4] & 16) != 0;
				msg.wrFlag = (data[4] & 32) != 0;
				int datasize = (!msg.rdFlag)?msg.dataSize:0;
				if (packet.getLength() != (1 + 4 + datasize)) return false;
				msg.data = new byte[datasize];
				for (int i = 0; i < datasize; i++)
					msg.data[i] = data[i + 5];
				mLastRxRemoteAddr = packet.getSocketAddress();
				return true;
			}
			catch (IOException e) { e.printStackTrace(); }
			return false;
		}
		public boolean txMessage(Message msg)
		{
			System.out.printf("txMessage\r\n");
			int datasize = (!msg.rdFlag)?(msg.dataSize):0;
			byte[] data = new byte[1 + 4 + datasize];
			data[0] = (byte)0xaa;
			data[1] = (byte)msg.nodeID;
			data[2] = (byte)(msg.msgID & 0xff);
			data[3] = (byte)((msg.msgID >> 8) & 0xff);
			data[4] = (byte)(((msg.dataSize > 0)?((msg.dataSize - 1) & 7) | 8:0) | (msg.rdFlag?16:0) | (msg.wrFlag?32:0));
			for (int i = 0; i < datasize; i++)
				data[i + 5] = msg.data[i];
			try
			{
				DatagramPacket packet = new DatagramPacket(data, data.length, (mRemoteAddr != null)?mRemoteAddr:mLastRxRemoteAddr);
				mSocket.send(packet);
				return true;
			}
			catch (SocketException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
			return false;
		}
	}
	public class XBusMaster
	{
		public XBusPort port = null;
		public int nodeID = 0;
		protected XBusMasterThread mThread = null;
		protected boolean mRunning = false;
		public void start()
		{
			if (mRunning) return;
			mThread = new XBusMasterThread();
			mThread.start();
			synchronized(mThread)
			{ try { mThread.wait(); } catch (InterruptedException e) { e.printStackTrace(); } }
		}
		public void stop()
		{
			if (!mRunning) return;
			mRunning = false;
			try { mThread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
			mThread = null;
		}
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
		protected class XBusMasterThread
		extends Thread
		{
			@Override public void run()
			{
				mRunning = true;
				synchronized(this)
				{ notify(); }
				while (mRunning)
					cycle();
			}
		}
	}
	public class XBusSlave
	{
		public XBusPort port = null;
		public int nodeID = 0;
		protected XBusSlaveThread mThread = null;
		protected boolean mRunning = false;
		public void start()
		{
			if (mRunning) return;
			mThread = new XBusSlaveThread();
			mThread.start();
			synchronized(mThread)
			{ try { mThread.wait(); } catch (InterruptedException e) { e.printStackTrace(); } }
		}
		public void stop()
		{
			if (!mRunning) return;
			mRunning = false;
			try { mThread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
			mThread = null;
		}
		protected void cycle()
		{
			System.out.printf("running\r\n");
			XBusPort.Message msg = port.new Message();
			if (port.rxMessage(msg))
				onMessage(msg);
		}
		protected boolean onMessage(XBusPort.Message msg)
		{
			System.out.printf("onMessage\r\n");
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
		protected class XBusSlaveThread
		extends Thread
		{
			@Override public void run()
			{
				mRunning = true;
				synchronized(this)
				{ notify(); }
				while (mRunning)
					cycle();
			}
		}
	}
}

