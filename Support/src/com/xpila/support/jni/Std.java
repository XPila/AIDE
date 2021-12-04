package com.xpila.support.jni;

import com.xpila.support.log.Log;

public class Std
{
	static { System.loadLibrary("std-jni"); }
	public static native void initStd();
	public static native void doneStd();
	public static native String readStdOut();
	public static native int writeStdIn(String str);
	public static native int writeStdIn2(int chr);
}

