package com.xpila.support.xport;

import com.xpila.support.log.Log;
import com.xpila.support.xport.XPort;

public class XPortJNI
implements XPort
{
	protected XPort mSrcXPort = null;//target = XPort object - rxtx calls from JNI(c) to XPort(java)
	protected long mSrcXPortPtr = 0;//target = SXPort* pointer - rxtx calls from XPort(java) to JNI(c)
	protected long mJNIXPortPtr = 0;//SXPortJNI* pointer for current instance
	protected XPortJNI()
	{
		Log.log("XPortJNI() constructor");
	}
	public XPortJNI(XPort xport)
	{
		Log.log("XPortJNI(XPort xport) constructor");
		mSrcXPort = xport;
	}
	public XPortJNI(long xportptr)
	{
		Log.log("XPortJNI(long xportptr) constructor");
		mSrcXPortPtr = xportptr;
	}
	public boolean init()
	{
		Log.log("XPortJNI.init");
		mJNIXPortPtr = XPortJNIinit(this, this.getClass(), mJNIXPortPtr);
		Log.log("JNIXPortInit returned %d", mJNIXPortPtr);
		return true;
	}
	public void done()
	{
		Log.log("XPortJNI.done");
		if (mJNIXPortPtr != 0) XPortJNIdone(mJNIXPortPtr);
		mJNIXPortPtr = 0;
	}
	@Override protected void finalize() throws Throwable
	{
		Log.log("XPortJNI.finalize");
		done();
		super.finalize();
	}
	public int XPort_Rx(byte[] buf, int pos, int cnt)
	{
		Log.log("XPortJNI.XPort_Rx called (%d)", cnt);
		if (mSrcXPort != null) return mSrcXPort.XPort_Rx(buf, pos, cnt);
		if (mSrcXPortPtr != 0) return XPortRx(mSrcXPortPtr, buf, pos, cnt);
		return 0;
	}
	public int XPort_Tx(byte[] buf, int pos, int cnt)
	{
		Log.log("XPortJNI.XPort_Tx called (%d)", cnt);
		if (mSrcXPort != null) return mSrcXPort.XPort_Tx(buf, pos, cnt);
		if (mSrcXPortPtr != 0) return XPortTx(mSrcXPortPtr, buf, pos, cnt);
		return cnt;
	}
	public long getJNIXPortPtr()
	{
		return mJNIXPortPtr;
	}
	public static long setLogPtr(long logptr)
	{
		return XPortSetLog(logptr);
	}	
	static { System.loadLibrary("xport-jni"); }
	private static native long XPortJNIinit(Object obj, Class cls, long ptr);
	private static native void XPortJNIdone(long ptr);
	private static native int XPortRx(long ptr, byte[] buf, int pos, int cnt);
	private static native int XPortTx(long ptr, byte[] buf, int pos, int cnt);
	private static native long XPortSetLog(long logptr);
}

