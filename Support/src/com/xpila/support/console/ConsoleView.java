package com.xpila.support.console;


import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.util.AttributeSet;
import android.text.InputType;
import android.text.InputFilter;
import android.text.Spanned;
import android.graphics.Rect;
import android.graphics.Typeface;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.CharBuffer;

import com.xpila.support.io.Pipe;
import com.xpila.support.log.Log;


public class ConsoleView
extends EditText
{	
	public InputStream in;
	public PrintStream out;
	private static int MSG_APPEND_OUT = 1;
	private static int MSG_APPEND_LF = 2;
	private InputMethodManager mIMM;
	private ConsoleInputFilter[] mFilters;
	private ConsoleHandler mHandler;
	private Pipe mInPipe;
	private Pipe mOutPipe;
	private InputStreamReader mOutReader;
	private OutputStreamWriter mInWriter;
	private CharBuffer mBuffer;
	private ConsoleInputStream mInStream;
	private ConsoleOutputStream mOutStream;
	private boolean mLineMode = true;
	private boolean mFilterBS = false;
	private boolean mLocalEcho = true;
	private boolean mInputVisible = false;
	private int mInputStart = 0;
	public ConsoleView(Context context)
	{
		super(context);
		Log.log("ConsoleView.ConsoleView(context)");
		init(context, null);
	}
	public ConsoleView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		Log.log("ConsoleView.ConsoleView(context, attrs)");
		init(context, attrs);
	}
	public void showInput()
	{
		Log.log("ConsoleView.showInput()");
		//mInputVisible = mIMM.isActive(this);
		if (!mInputVisible)
			mIMM.toggleSoftInput(0, 0);
		mInputVisible = mIMM.isActive(this);
		if (mInputVisible) onShowInput();
	}
	public void hideInput()
	{
		Log.log("ConsoleView.hideInput()");
		//mInputVisible = mIMM.isActive(this);
		if (mInputVisible)
			mIMM.toggleSoftInput(0, 0);
		mInputVisible = !mIMM.isActive(this);
		if (!mInputVisible) onHideInput();
	}
	protected void onShowInput()
	{
		Log.log("ConsoleView.onShowInput()");
		//setTextColor(0xff80c040);
	}
	protected void onHideInput()
	{
		Log.log("ConsoleView.onHideInput()");
		//setTextColor(0xff40c080);
	}
	private void init(Context context, AttributeSet attrs)
	{
		mIMM = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mFilters = new ConsoleInputFilter[1];
		mFilters[0] = new ConsoleInputFilter();
		mHandler = new ConsoleHandler();
		mInPipe = new Pipe(1024);
		mInWriter = new OutputStreamWriter(mInPipe.out);
		mInStream = new ConsoleInputStream();
		in = mInStream;
		//in = mInPipe.in;
		mOutPipe = new Pipe(1024);
		mOutReader = new InputStreamReader(mOutPipe.in);
		mBuffer = CharBuffer.allocate(1024);
		mOutStream = new ConsoleOutputStream();
		out = new PrintStream(mOutStream);
		setGravity(Gravity.TOP);
		setBackgroundColor(0xff000000);
		setTextColor(0xff808080);
		setTextSize(15);
		setTypeface(Typeface.MONOSPACE);
		setFilters(mFilters);
		setPadding(0, 0, 0, 0);
		setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.log("ConsoleView.onKeyDown(keyCode=%d)", keyCode);
		if (keyCode == 67)//backspace
		{
			if (mFilterBS)
				return true;
			else if (mLineMode)
			{
				if (mInputStart >= length())
						return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		Log.log("ConsoleView.onKeyUp(keyCode=%d)", keyCode);
		if (keyCode == 67)//backspace
		{
			if (mFilterBS)
			{
				try { mInPipe.out.write(0x08); }
				catch (IOException e) { e.printStackTrace(); }
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override public boolean onTouchEvent(MotionEvent event)
	{
		return super.onTouchEvent(event);
		/*if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			hideInput();
		}
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			showInput();
		}
		return false;*/
	}

	@Override protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
	{
		Log.log("ConsoleView.onFocusChanged(focused=%d, direction=%d)", focused?1:0, direction);
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		/*if (!focused)
		{
			mInputVisible = false;
			onHideInput();
		}*/
	}
	@Override public boolean onKeyPreIme(int keyCode, KeyEvent event)
	{
		Log.log("ConsoleView.onKeyPreIme(keyCode=%d)", keyCode);
		if (event.getAction() == KeyEvent.ACTION_UP)
			if (keyCode == 4)//back
				if (mInputVisible)
				{
					mInputVisible = false;
					onHideInput();
				}
		return super.onKeyPreIme(keyCode, event);
	}
	class ConsoleHandler
	extends Handler
	{		
		public boolean sendMessage(int arg1)
		{
			Log.log("ConsoleView.ConsoleHandler.sendMessage(arg1=%d)", arg1);
			Message msg = new Message();
			msg.arg1 = arg1;
			return mHandler.sendMessage(msg);
		}
		@Override public void handleMessage(Message msg)
		{
			Log.log("ConsoleView.ConsoleHandler.handleMessage(arg1=%d)", msg.arg1);
			if (msg.arg1 == MSG_APPEND_OUT)
			{
				boolean enabled = mFilters[0].mEnabled;
				mFilters[0].mEnabled = false;
				try
				{
					int available = mOutPipe.in.available();
					if (available > 0)
					{
						int chars = mOutReader.read(mBuffer.array(), 0, available);
						append(mBuffer, 0, chars);
						mInputStart = length();
						setSelection(mInputStart);
					}
				}
				catch (IOException e) { e.printStackTrace(); }
				mFilters[0].mEnabled = enabled;
			}
			else if (msg.arg1 == MSG_APPEND_LF)
			{
				boolean enabled = mFilters[0].mEnabled;
				mFilters[0].mEnabled = false;
				append("\n");
				mInputStart = length();
				setSelection(mInputStart);								
				mFilters[0].mEnabled = enabled;
			}
		}
	}
	class ConsoleInputFilter
	implements InputFilter
	{
		public boolean mEnabled = true;
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
		{
			Log.log("ConsoleView.ConsoleInputFilter.filter(source=%s, start=%d, end=%d, dest=%s, dstart=%d, dend=%d)", source.toString(), start, end, dest.toString(), dstart, dend);
			if (mEnabled)
			{
				try
				{
					if ((start == 0) && (end == 1))
					{
						if (mLineMode)
						{
							if (getSelectionStart() < mInputStart)
								return "";
							if (source.charAt(0) == '\n')
							{
								String line = getText().subSequence(mInputStart, length()).toString();
								mInWriter.write(line + "\n");
								mInWriter.flush();
								mHandler.sendMessage(MSG_APPEND_LF);
								return "";
							}
						}
						else
							mInPipe.out.write((int)source.charAt(0));
					}
				}
				catch (IOException e)
				{ e.printStackTrace(); }
				return mLocalEcho?null:"";
			}
			return null;
		}
	}
	class ConsoleOutputStream
	extends OutputStream
	{
		public void write(int oneByte) throws IOException
		{
			mOutPipe.out.write(oneByte);
			mHandler.sendMessage(MSG_APPEND_OUT);
		}
		@Override public void write(byte[] buffer) throws IOException
		{
			mOutPipe.out.write(buffer);
			mHandler.sendMessage(MSG_APPEND_OUT);
		}
		@Override public void write(byte[] buffer, int offset, int count) throws IOException
		{
			mOutPipe.out.write(buffer, offset, count);
			mHandler.sendMessage(MSG_APPEND_OUT);
		}
	}
	class ConsoleInputStream
	extends InputStream
	{
		public int available() throws IOException
		{
			return mInPipe.in.available();
		}		
		public int read() throws IOException
		{
			return mInPipe.in.read();
		}
		public int read(byte[] b, int off, int len) throws IOException
		{
			return mInPipe.in.read(b, off, len);
		}
	}
}
	

