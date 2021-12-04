package com.xpila.support.activity;

import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Modal
{
	private Method mMsgQueueNextMethod = null;
	private Field mMsgTargetFiled = null;
	private boolean mQuitModal = false;
	public boolean prepare()
	{
		Class<?> clsMsgQueue = null;
		Class<?> clsMessage = null;
		try { clsMsgQueue = Class.forName("android.os.MessageQueue"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); return false; }
		try { clsMessage = Class.forName("android.os.Message"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); return false; }
		try { mMsgQueueNextMethod = clsMsgQueue.getDeclaredMethod("next", new Class[]{}); }
		catch (SecurityException e) { e.printStackTrace(); return false; }
		catch (NoSuchMethodException e) { e.printStackTrace(); return false; }
		mMsgQueueNextMethod.setAccessible(true);
		try { mMsgTargetFiled = clsMessage.getDeclaredField("target"); }
		catch (SecurityException e) { e.printStackTrace(); return false; }
		catch (NoSuchFieldException e) { e.printStackTrace(); return false; }
		mMsgTargetFiled.setAccessible(true);
		return true;
	}
	public void run()
	{
		mQuitModal = false;
		// get message queue associated with main UI thread
		MessageQueue queue = Looper.myQueue();
		while (!mQuitModal)
		{
			// call queue.next(), might block
			Message msg = null;
			try { msg = (Message)mMsgQueueNextMethod.invoke(queue, new Object[]{}); }
			catch (IllegalArgumentException e) { e.printStackTrace(); }
			catch (IllegalAccessException e) { e.printStackTrace(); }
			catch (InvocationTargetException e) { e.printStackTrace(); }
			if (null != msg)
			{
				Handler target = null;
				try { target = (Handler)mMsgTargetFiled.get(msg); }
				catch (IllegalArgumentException e) { e.printStackTrace(); }
				catch (IllegalAccessException e) { e.printStackTrace(); }
				if (target == null)
				{
					// No target is a magic identifier for the quit message.
					mQuitModal = true;
				}
				target.dispatchMessage(msg);
				msg.recycle();
			}
		}
	}
	public void quit()
	{
		mQuitModal = true;
	}
}
