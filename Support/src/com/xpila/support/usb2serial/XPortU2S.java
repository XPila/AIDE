package com.xpila.support.usb2serial;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;

import com.xpila.support.log.Log;
import com.xpila.support.xport.XPortJNI;
import com.xpila.support.usb2serial.Usb2Serial;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;

public class XPortU2S
extends XPortJNI
{
	private UsbSerialPort mPort = null;
	
	private boolean mOpened = false;
	private String mDeviceName = null;
	private int mTimeout = 500;
	private byte[] mRxBuffer = null;
	private int mRxBufPos = 0;
	private int mRxBufCnt = 0;
	protected XPortU2S(UsbSerialPort port, String devicename)
	{
		Log.log("XPortU2S constructor, devicename='%s'", devicename);
		mPort = port;
		if (mPort == null)
			Log.log("XPortU2S constructor, mPort=null");
		else
			Log.log("XPortU2S constructor, mPort=OK");
		
		mDeviceName = devicename;
	}
	public boolean init()
	{
		Log.log("XPortU2S.init");
		if (mOpened) return true;
		super.init();
		if (mPort != null)
		{
			UsbSerialDriver driver = mPort.getDriver();
		if (driver == null)
			Log.log("XPortU2S init, getDriver=null");
		else
			Log.log("XPortU2S init, getDriver=OK");

			UsbDevice device = driver.getDevice();
		if (device == null)
			Log.log("XPortU2S init, getDevice=null");
		else
			Log.log("XPortU2S init, getDevice=OK");
			
//			UsbDeviceConnection connection = Usb2Serial.mUsbManager.openDevice(mPort.getDriver().getDevice());
			UsbDeviceConnection connection = Usb2Serial.mUsbManager.openDevice(device);
			if (connection == null)
				Log.log("connection is null");
			else
			try
			{
				mPort.open(connection);
				mPort.setRTS(true);
				mPort.setParameters(9600, mPort.DATABITS_8, mPort.STOPBITS_1, mPort.PARITY_NONE);
				mRxBuffer = new byte[16384];
				mRxBufPos = 0;
				mRxBufCnt = 0;
				mOpened = true;
				return true;
			}
			catch (IOException e) { e.printStackTrace(); }
		}
		return false;
	}
	public void done()
	{
		Log.log("XPortU2S.done");
		if (!mOpened) return;
		if (mPort != null)
			try
			{
				mPort.close();
			}
			catch (IOException e) { e.printStackTrace(); }
		super.done();
		mOpened = false;
	}
	public String getDeviceName()
	{
		return mDeviceName;
	}
	public boolean getOpened()
	{
		return mOpened;
	}
	public int XPort_Rx(byte[] buf, int pos, int cnt)
	{
		Log.log("XPortU2S.XPort_Rx(%d)", cnt);
		if (mPort == null) return 0;
		int readed = 0;
		long tstart = System.currentTimeMillis();
		long tend = tstart + mTimeout;
		do
		{
			if (mRxBufCnt > 0)
			{
				if (mRxBufCnt > (cnt - readed))
				{
					System.arraycopy(mRxBuffer, mRxBufPos, buf, pos, (cnt - readed));
					mRxBufCnt -= (cnt - readed);
					mRxBufPos += (cnt - readed);
					return cnt;
				}
				else
				{
					System.arraycopy(mRxBuffer, mRxBufPos, buf, pos, mRxBufCnt);
					readed += mRxBufCnt;
					pos += mRxBufCnt;
					mRxBufCnt = 0;
					mRxBufPos = 0;
					if (readed >= cnt) return readed;
				}
			}
			try
			{
				Log.log("before mPort.read");
				mRxBufCnt = mPort.read(mRxBuffer, mTimeout);
				Log.log("mPort.read returned %d", mRxBufCnt);
			}
			catch (IOException e) { e.printStackTrace(); }
//			if (mRxBufCnt == 0)
//				try { Thread.sleep(10); continue; } catch (InterruptedException e) { e.printStackTrace(); }
			if (mRxBufCnt >= (cnt - readed))
				continue;
		}
		while ((readed < cnt) && (System.currentTimeMillis() < tend));
		Log.log("XPortU2S.XPort_Rx returned %d", readed);
	return readed;
		
/*		Log.log("XPortU2S.XPort_Rx(%d)", cnt);
		if (mPort == null) return 0;
		byte[] data = new byte[cnt];
		int rx = 0;
		try
		{
			rx = mPort.read(data, mTimeout);
			if (rx > 0)
				System.arraycopy(data, 0, buf, pos, rx);
//				System.arraycopy(buf, pos, data, 0, rx);
		}
		catch (IOException e) { e.printStackTrace(); }
		return rx;*/
	}
	public int XPort_Tx(byte[] buf, int pos, int cnt)
	{
		Log.log("XPortU2S.XPort_Tx(%d)", cnt);
		
		if (mPort == null) return 0;		
		byte[] data = new byte[cnt];
		int tx = 0;
		try
		{
			System.arraycopy(buf, pos, data, 0, cnt);
			tx = mPort.write(data, mTimeout);
		}
		catch (IOException e) { e.printStackTrace(); }
		Log.log("XPortU2S.XPort_Tx returned %d", tx);
		return tx;
	}	
}
