package com.xpila.support.os;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import com.xpila.support.log.Log;


public class Shell
{
	public InputStream in = null;
	public InputStream err = null;
	public PrintStream out = null;
	private ProcessBuilder mBuilder = null;
	private Process mProcess = null;
	private ShellThread mThread = null;
	private boolean mIsRunning = false;
	public Shell()
	{
		mBuilder = new ProcessBuilder();
	}
	public String getEnvVar(String name)
	{
		return mBuilder.environment().get(name);
	}
	public String setEnvVar(String name, String value)
	{
		return mBuilder.environment().put(name, value);
	}
	public boolean start(File source, boolean redirectErr)
	{
		try
		{
			List<String> command = new LinkedList<String>();
			command.add("sh");
			if (source != null)
			{
				if (source.isFile())
				{
					command.add(source.getAbsolutePath());
					mBuilder.directory(source.getParentFile());
				}
				else if (source.isDirectory())
				{
					command.add("-i");
					mBuilder.directory(source);
				}
			}
			else
				command.add("-i");
			mBuilder.command(command);
			mBuilder.redirectErrorStream(redirectErr);
			mProcess = mBuilder.start();
			in = mProcess.getInputStream();
			err = mProcess.getErrorStream();
			out = new PrintStream(mProcess.getOutputStream());
			mThread = new ShellThread();
			mThread.start();
			mIsRunning = true;
			return true;
		}
		catch (IOException e) { e.printStackTrace(); }
		return false;
	}
	public void stop()
	{
		try { mProcess.destroy(); }
		catch (Exception e) {}
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
			catch (InterruptedException e) { e.printStackTrace(); }
			mIsRunning = false;
		}
	}
}

