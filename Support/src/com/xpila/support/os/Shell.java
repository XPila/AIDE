package com.xpila.support.os;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;


public class Shell
{
	public InputStream in = null;
	public InputStream err = null;
	public PrintStream out = null;
	private Process mProcess = null;
	private ShellThread mThread = null;
	private boolean mIsRunning = false;
	public boolean start(File directory, boolean redirectErr)
	{
		try
		{
			//mProcess = Runtime.getRuntime().exec("sh");
			ProcessBuilder builder = new ProcessBuilder("sh");
			if (directory != null) builder.directory(directory);
			builder.redirectErrorStream(redirectErr);
			mProcess = builder.start();
			in = mProcess.getInputStream();
			err = mProcess.getErrorStream();
			out = new PrintStream(mProcess.getOutputStream());
			mThread = new ShellThread();
			mThread.start();
			mIsRunning = true;
			return true;
		}
		catch (IOException e) {}
		return false;
	}
	public void stop()
	{
		try {
			mProcess.destroy();
		} catch (Exception e) {}
		mProcess = null;
		in = null;
		err = null;
		out = null;
	}
	public boolean isRunning()
	{
		return mIsRunning;
	}
	private class ShellThread
	extends Thread
	{
		@Override public void run()
		{
			try
			{
				if (mProcess != null)
					mProcess.waitFor();
			}
			catch (InterruptedException e) {}
			mIsRunning = false;
		}
	}
}

