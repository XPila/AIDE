package com.xpila.support.jni;

import android.content.Context;
import com.xpila.support.log.Log;

//This class allows to run native console/terminal application in java thread.
//Application must be compiled as library.
//Java activity or service can interacts with the native application through stdin/stdout.
//Libname is library name without prefix 'lib' and extension '.so'.
public class JNIMain
extends Thread
{
	public interface Listener
	{
		public void onJNIMainRun(JNIMain jnimain, String libname, String cmdline);
		public void onJNIMainEnd(JNIMain jnimain, int exitcode);
		public void onJNIMainLog(JNIMain jnimain, String logmsg);
	}
	private Context mContext = null;
	private Listener mListener = null;
	private String mLibName = null;
	private String mCmdLine = null;
	private boolean mRunning = false;
	public JNIMain(Context context, Listener listener, String libname, String cmdline)
	{
		mContext = context;
		mListener = listener;
		mLibName = libname;
		mCmdLine = cmdline;
	}
	@Override public void start()
	{
		System.loadLibrary(mLibName);
		if (mRunning) return;
		super.start();
		synchronized (this)
		{
			try { wait(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	@Override public void run()
	{
		mRunning = true;
		synchronized (this)
		{ notify(); }
		if (mListener != null) mListener.onJNIMainRun(this, mLibName, mCmdLine);
		Object[] args = null;
		if ((mCmdLine != null) && (mCmdLine.length() > 0))
		{
			String[] cmdline = mCmdLine.split(" ");
			args = new Object[cmdline.length + 1];
			int j = 1;
			for (int i = 0; i < cmdline.length; i++)
				if ((cmdline[i] != null) && (cmdline[i].length() > 0))
					args[j++] = cmdline[i];
		}
		else
			args = new Object[1];
		args[0] = mLibName;
		int exitcode = main(args);
		if (mListener != null) mListener.onJNIMainEnd(this, exitcode);
		mRunning = false;		
	}
	public static native int main(Object[] args);
}

