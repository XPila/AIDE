package com.xpila.support.jni;

import android.app.Activity;
import android.content.Context;
import android.widget.ScrollView;
import android.widget.HorizontalScrollView;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.View;
import android.view.Gravity;

import java.io.*;

import com.xpila.support.log.Log;
import com.xpila.support.console.ConsoleView;
import com.xpila.support.io.StreamUtils;

import com.xpila.support.jni.JNIMain;

//
public class JNIConsoleActivity
	extends Activity
	implements JNIMain.Listener
{
	protected ConsoleView mvConsole = null;
	protected ConsoleThread mConsoleThread = null;
	protected JNIMain mJNIMain = null;
    @Override public void onCreate(Bundle savedInstanceState)
    {
		Log.log("JNIConsoleActivity OnCreate");
        super.onCreate(savedInstanceState);
		createConsole();
		Log.log("Console initialized");
    }

	@Override protected void onDestroy()
	{
		Log.log("JNIConsoleActivity OnDestroy");
		destroyConsole();
		Log.log("Console terminated");
		super.onDestroy();
	}
	
	protected void createConsole()
	{
		if (mvConsole == null)
		{
			mvConsole = new ConsoleView(this);		
			setContentView(mvConsole);
		}
		getWindow().setSoftInputMode(3);
		mvConsole.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		mvConsole.setGravity(Gravity.TOP);
		mvConsole.setBackgroundColor(0xff101010);
		mvConsole.setTextColor(0xff808080);
		mvConsole.setTextSize(16);
		mvConsole.setTypeface(Typeface.MONOSPACE);
		mvConsole.setPadding(4, 0, 0, 0);

		Std.initStd();
		Std.readStdOut();
		mConsoleThread = new ConsoleThread();
		mConsoleThread.start();
	}
	protected void destroyConsole()
	{
		Std.doneStd();
		if (mConsoleThread.isAlive())
		{
			mConsoleThread.interrupt();
			try { mConsoleThread.join(500); }
			catch (InterruptedException e) {}
		}
		mvConsole.hideInput();
	}
	private class ConsoleThread
	extends Thread
	{
		@Override public void run()
		{
			while (true)
			{
				String textout = Std.readStdOut();
				if ((textout != null) && (textout.length() > 0))
					mvConsole.out.print(textout);
				try {
					while (mvConsole.in.available() > 0)
						Std.writeStdIn2(mvConsole.in.read());
				} catch (IOException e) {e.printStackTrace();}
				if (isInterrupted()) break;
				try { sleep(20); } catch (InterruptedException e) { break; }
			}
			finish();
		}
	}
	protected void startJNIMain(String libname, String cmdline)
	{
		if (mJNIMain != null) return;
		mJNIMain = new JNIMain(this, this, libname, cmdline);
		mJNIMain.start();
	}
	@Override public void onJNIMainRun(JNIMain jnimain, String libname, String cmdline)
	{
		Log.log("onJNIMainRun %s %s", libname, cmdline);
	}
	@Override public void onJNIMainEnd(JNIMain jnimain, int exitcode)
	{
		Log.log("onJNIMainEnd %d", exitcode);
		mJNIMain = null;
	}
	@Override public void onJNIMainLog(JNIMain jnimain, String logmsg)
	{
		Log.log("onJNIMainLog %s", logmsg);
	}
	
}

