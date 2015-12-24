package com.xpila.support.xbus;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


public class XBus
{
	public class XBusMessage
	{
		public byte nodeID;
		public short msgID;
		public byte dataSize;
		public boolean dataFlag;
		public boolean rdFlag;
		public boolean wrFlag;
		public byte[] data;
		/*public String toString()
		{
			String str = "";
			str += hex(nodeID) + " ";
			str += hex(msgID) + " ";
			str += "(";
			str += (char)((dataFlag?(dataSize + 1):0) + '0');
			str += rdFlag?'r':'-';
			str += wrFlag?'w':'-';
			str += ") ";
			if (data != null)
				str += hex(data);
			return str;
		}*/
	}
	public class XBus
	{
		public String name = "";
		public InputStream in = null;
		public OutputStream out = null;
		private boolean mAck = true;
		private boolean mChs = true;
		private byte[] mTxBuffer = null;
		private int mTimeouAtom = 10;
		private int mTimeoutStart = 200;
		private int mTimeoutHeader = 10;
		private int mTimeoutData = 10;
		private int mTimeoutChs = 10;
		private int mTimeoutAck = 200;
		private byte mCharStart = (byte)0xaa;
		private byte mCharAck = (byte)0x55;
		public XBus()
		{
			
		}
		public boolean rxMessage(XBusMessage message)
		{
			if (rxStart())
				if (rxHeader(message))
					if (rxData(message))
						if (txAck())
						{
							//System.out.println(name + " rxmsg " + message.toString());
							return true;
						}
			return false;
		}
		public boolean txMessage(XBusMessage message)
		{
			if (txStart())
				if (txHeader(message))
					if (txData(message))
						if (rxAck())
						{
							//System.out.println(name + " txmsg " + message.toString());
							return true;
						}
			return false;
		}
		private void idle()
		{
		//	sleep(mTimeouAtom);
		}
		private byte checksum(byte[] data)
		{
			byte checksum = 0;
			for (int i = 0; i < data.length; i++)
				checksum ^= data[i];
			return checksum;
		}
		private boolean rx(byte[] data, int position, int size, int timeout)
		{
			if (in == null) return false;
			long startTime = System.currentTimeMillis();
			try
			{
				while (in.available() < size)
				{
					if (System.currentTimeMillis() > (startTime + timeout))
					{
						//System.out.println(name + " rx timeout");
						return false;
					}
					idle();
				}
				if (in.read(data, position, size) != size) return false;
				//System.out.println(name + " rx " + hex(data, position, size));
				return true;
			}
			catch (IOException e) {}
			return false;
		}
		private boolean tx(byte[] data, int position, int size)
		{
			if (out == null) return false;
			try
			{
				out.write(data, position, size);
				return true;
			}
			catch (IOException e) {}
			return false;
		}
		private boolean rxStart()
		{
			byte[] start = {0};
			if (!rx(start, 0, 1, mTimeoutStart)) return false;
			return start[0] == mCharStart;
		}
		private boolean txStart()
		{
			byte[] start = {mCharStart};
			return tx(start, 0, 1);
		}
		private boolean rxHeader(XBusMessage message)
		{
			byte[] header = {0,0,0,0};
			if (!rx(header, 0, header.length, mTimeoutHeader)) return false;
			message.nodeID = header[0];
			message.msgID = (short)(header[1] | (header[2] << 8));
			message.dataSize = (byte)(header[3] & 7);
			message.dataFlag = (header[3] & 8) != 0;
			message.rdFlag = (header[3] & 16) != 0;
			message.wrFlag = (header[3] & 32) != 0;
			if (!mChs) return true;
			byte[] checksum = {0};
			if (!rx(checksum, 0, 1, mTimeoutChs)) return false;
			return checksum[0] == checksum(header);
		}
		private boolean txHeader(XBusMessage message)
		{
			byte[] header =
			{
				message.nodeID,
				(byte)(message.msgID & 0xff),
				(byte)((message.msgID >> 8) & 0xff),
				(byte)((message.dataSize & 7) |
				(message.dataFlag?8:0) |
				(message.rdFlag?16:0) |
				(message.wrFlag?32:0))
			};
			if (!tx(header, 0, header.length)) return false;
			if (!mChs) return true;
			byte[] checksum = {checksum(header)};
			return tx(checksum, 0, 1);
		}
		private boolean rxData(XBusMessage message)
		{
			if (!message.dataFlag || message.rdFlag) return true;
			message.data = new byte[message.dataSize + 1];
			if (!rx(message.data, 0, message.dataSize + 1, mTimeoutData)) return false;
			if (!mChs) return true;
			byte[] checksum = {0};
			if (!rx(checksum, 0, 1, mTimeoutChs)) return false;
			return checksum[0] == checksum(message.data);
		}
		private boolean txData(XBusMessage message)
		{
			if (!message.dataFlag || message.rdFlag) return true;
			if (!tx(message.data, 0, message.data.length)) return false;
			if (!mChs) return true;
			byte[] checksum = {checksum(message.data)};
			return tx(checksum, 0, 1);
		}
		private boolean txAck()
		{
			if (!mAck) return true;
			byte[] ack = {mCharAck};
			return tx(ack, 0, 1);
		}
		private boolean rxAck()
		{
			if (!mAck) return true;
			byte[] ack = {0};
			if (!rx(ack, 0, 1, mTimeoutAck)) return false;
			return ack[0] == mCharAck;
		}
	}
	public class XBusMaster
	extends XBus
	{
		public boolean rdObject(byte nodeID, short id, byte[] data, int position, int size)
		{
			XBusMessage message = new XBusMessage();
			message.nodeID = nodeID;
			message.msgID = id;
			message.dataSize = (size > 0)?(byte)(size - 1):0;
			message.dataFlag = size > 0;
			message.rdFlag = true;
			message.wrFlag = false;
			if (!txMessage(message)) return false;
			if (!rxMessage(message)) return false;
			System.arraycopy(message.data, 0, data, position, size);
			return true;
		}
		public boolean wrObject(byte nodeID, short id, byte[] data, int position, int size)
		{
			XBusMessage message = new XBusMessage();
			message.nodeID = nodeID;
			message.msgID = id;
			message.dataSize = (size > 0)?(byte)(size - 1):0;
			message.dataFlag = size > 0;
			message.rdFlag = false;
			message.wrFlag = true;
			message.data = new byte[size];
			System.arraycopy(data, position, message.data, 0, size);
			return txMessage(message);
		}
		public boolean command(byte nodeID, short id, byte[] data, int position, int size)
		{
			XBusMessage message = new XBusMessage();
			message.nodeID = nodeID;
			message.msgID = id;
			message.dataSize = (size > 0)?(byte)(size - 1):0;
			message.dataFlag = size > 0;
			message.rdFlag = false;
			message.wrFlag = false;
			message.data = new byte[size];
			System.arraycopy(data, position, message.data, 0, size);
			return txMessage(message);
		}
	}
	public class XBusSlave
	extends XBus
	{
		protected byte mNodeID = 1;
		XBusSlaveThread mThread = null;
		protected boolean mRunning = false;
		public byte[] obj = new byte[8];
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
		protected void running()
		{
			//System.out.println("running");
			XBusMessage message = new XBusMessage();
			if (rxMessage(message))
				onMessage(message);
		}
		protected boolean onMessage(XBusMessage message)
		{
			if (message.nodeID != mNodeID) return false;
			if (message.rdFlag && !message.wrFlag)
			{
				message.rdFlag = false;
				message.data = new byte[message.dataSize + 1];
				if (rdObject(message.msgID, message.data))
					return txMessage(message);
			}
			else if (!message.rdFlag && message.wrFlag)
				return wrObject(message.msgID, message.data);
			else if (!message.rdFlag && !message.wrFlag)
				return command(message.msgID, message.data);
			return false;
		}
		protected boolean rdObject(short id, byte[] data)
		{
			if (id == 7)
			{
				System.arraycopy(obj, 0, data, 0, data.length);
				return true;
			}
			return false;
		}
		protected boolean wrObject(short id, byte[] data)
		{
			if (id == 7)
			{
				System.arraycopy(data, 0, obj, 0, data.length);
				return true;
			}
			return false;
		}
		protected boolean command(short id, byte[] data)
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
					running();
			}
		}
	}
}

