package com.xpila.support.usb2serial;

//import android.os.AsyncTask;
import android.os.SystemClock;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;

import java.util.ArrayList;
import java.util.List;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import com.xpila.support.log.Log;

public class Usb2Serial
{
	protected static UsbManager mUsbManager = null;	
	protected static List<XPortU2S> mXPorts = null;
	public static void init(Context context)
	{
		Log.log("Usb2Serial.init");
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		SystemClock.sleep(500);
		enum();
	}
	public static void done()
	{
		Log.log("Usb2Serial.done");
		if (mXPorts != null)
			for (final XPortU2S xport : mXPorts)
				xport.done();
		mXPorts = null;
		mUsbManager = null;
	}
	public static void enum()
	{
		Log.log("Usb2Serial.enum");
		List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
		List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
		for (UsbSerialDriver driver : drivers)
		{
			List<UsbSerialPort> ports = driver.getPorts();
			Log.log(String.format("+ %s: %s port%s", driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
			for (UsbSerialPort port : ports)
			{
				Log.log("PortNumber:%d", port.getPortNumber());
				String serial = null;
				//try{
				//	serial = port.getSerial();
				//} catch (NullPointerException e) { e.printStackTrace(); }
				Log.log("SerialNumber:%s", (serial != null)?serial:"null");
				UsbDevice device = driver.getDevice();
				if (device != null)
				{
					Log.log("DeviceId:%d", device.getDeviceId());
					String devname = device.getDeviceName();
					Log.log("DeviceName:%s", (devname != null)?devname:"null");
				}
				else
				{
					Log.log("Device=null");
				}
			}
			result.addAll(ports);
		}
		List<XPortU2S> xports = new ArrayList<XPortU2S>();
		for (UsbSerialPort port : result)
		{
			UsbSerialDriver driver = port.getDriver();
			UsbDevice device = driver.getDevice();			
			String devicename = device.getDeviceName();
			XPortU2S xport = getXPortByDeviceName(devicename);
			if (xport == null)
				xport = new XPortU2S(port, devicename);
			else
				mXPorts.remove(xport);
			xports.add(xport);
			//xport.init();
		}
		if (mXPorts != null)
			for (XPortU2S xport : mXPorts)
				xport.done();
		mXPorts = xports;
	}
	public static int getXPortCount()
	{
		if (mXPorts == null) return 0;
		return mXPorts.size();
	}
	public static XPortU2S getXPortByIndex(int index)
	{
		if (mXPorts == null) return null;
		if ((index < 0) || (index >= mXPorts.size())) return null;
		return mXPorts.get(index);
	}
	public static XPortU2S getXPortByDeviceName(String devicename)
	{
		if (mXPorts == null) return null;
		int i = 0;
		for (final XPortU2S xport : mXPorts)
		{
			Log.log("getXPortByDeviceName port #%d '%s' '%s'", i, devicename, xport.getDeviceName());
			if (devicename.compareTo(xport.getDeviceName()) == 0)
				return xport;
		}
		Log.log("getXPortByDeviceName returning null");
		return null;
	}
	public static long getXPortPtrByDeviceName(String devicename)
	{
		Log.log("getXPortByDeviceName '%s'", devicename);
		if (mXPorts == null) return 0;
		Log.log("getXPortByDeviceName 1");
		XPortU2S xport = getXPortByDeviceName(devicename);
		if (xport == null) return 0;
		xport.init();
		Log.log("getXPortByDeviceName 2");
		return xport.getJNIXPortPtr();
	}
	public static long setLogPtr(long logptr)
	{
		return XPortU2S.setLogPtr(logptr);
	}	
}




