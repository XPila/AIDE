package com.xpila.support.console;


//import android.os.Handler;
//import android.os.Message;
//import android.os.ResultReceiver;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.content.Context;
import android.util.AttributeSet;
import android.text.InputFilter;
import android.text.Spanned;
import android.graphics.Typeface;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

import com.xpila.support.io.Pipe;
import java.nio.CharBuffer;
import android.text.*;
import android.graphics.*;
import android.os.*;
import android.app.*;
import android.view.*;
import android.view.inputmethod.*;
import android.webkit.*;


public class ConsoleView
extends EditText
{
	public InputStream in;
	public PrintStream out;
	private InputMethodManager mIMM;
	private ConsoleInputFilter[] mFilters;
	private ConsoleHandler mHandler;
	private Pipe mInPipe;
	private Pipe mOutPipe;
	private InputStreamReader mOutReader;
	private OutputStreamWriter mInWriter;
	private CharBuffer mBuffer;
	private ConsoleOutputStream mOutStream;
	private boolean mLineMode = true;
	private boolean mFilterBS = false;
	private boolean mLocalEcho = true;
	private boolean mInputVisible = false;
	public ConsoleView(Context context)
	{
		super(context);
		init(context, null);
	}
	public ConsoleView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}
	public void showInput()
	{
		//mInputVisible = mIMM.isActive(this);
		if (!mInputVisible)
			mIMM.toggleSoftInput(0, 0);
		mInputVisible = mIMM.isActive(this);
		if (mInputVisible) onShowInput();
	}
	public void hideInput()
	{
		//mInputVisible = mIMM.isActive(this);
		if (mInputVisible)
			mIMM.toggleSoftInput(0, 0);
		mInputVisible = !mIMM.isActive(this);
		if (!mInputVisible) onHideInput();
	}
	protected void onShowInput()
	{
		//setTextColor(0xff80c040);
	}
	protected void onHideInput()
	{
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
		in = mInPipe.in;
		mOutPipe = new Pipe(1024);
		mOutReader = new InputStreamReader(mOutPipe.in);
		mBuffer = CharBuffer.allocate(1024);
		mOutStream = new ConsoleOutputStream();
		out = new PrintStream(mOutStream);
		setGravity(Gravity.TOP);
		setBackgroundColor(0xff000000);
		setTextColor(0xff40c040);
		setTextSize(13);
		setTypeface(Typeface.MONOSPACE);
		setFilters(mFilters);
		setPadding(0, 0, 0, 0);
		setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
	}
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == 67)//backspace
		{
			if (mFilterBS)
				return true;
			else if (mLineMode)
			{
				String text = getText().toString();
				int lastLF = text.lastIndexOf('\n');
				if (lastLF + 1 >= text.length())
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == 67)//backspace
		{
			if (mFilterBS)
			{
				try { mInPipe.out.write(0x08); }
				catch (IOException e) {}
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			hideInput();
		}
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			showInput();
		}
		return true;
	}

	@Override protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
	{
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (!focused)
		{
			mInputVisible = false;
			onHideInput();
		}
	}
	@Override public boolean onKeyPreIme(int keyCode, KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_UP)
			if (keyCode == 4)//back
				if (mInputVisible)
				{
					mInputVisible = false;
					onHideInput();
				}
		return super.onKeyPreIme(keyCode, event);
	}
	class ConsoleInputFilter
	implements InputFilter
	{
		public boolean mEnabled = true;
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
		{
			if (mEnabled)
			{
				try
				{
					if ((start == 0) && (end == 1))
					{
						if (mLineMode)
						{
							if (source.charAt(0) == '\n')
							{
								String text = getText().toString();
								int lastLF = text.lastIndexOf('\n');
								//if (lastLF >= 0) text = text.substring(lastLF + 1, text.length());
								String line = text.substring(lastLF + 1, text.length());
								mInWriter.write(line + "\n");
								mInWriter.flush();
							}
						}
						else
							mInPipe.out.write((int)source.charAt(0));
					}
				}
				catch (IOException e)
				{}
				return mLocalEcho?null:"";
			}
			return null;
		}
	}
	class ConsoleHandler
	extends Handler
	{
		@Override public void handleMessage(Message msg)
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
					setSelection(length());
				}
			}
			catch (IOException e) {}
			mFilters[0].mEnabled = enabled;
		}
	}
	class ConsoleOutputStream
	extends OutputStream
	{
		protected void sendMessage()
		{
			Message msg = new Message();
			mHandler.sendMessage(msg);
		}
		public void write(int oneByte) throws IOException
		{
			mOutPipe.out.write(oneByte);
			sendMessage();
		}
		@Override public void write(byte[] buffer) throws IOException
		{
			mOutPipe.out.write(buffer);
			sendMessage();
		}
		@Override public void write(byte[] buffer, int offset, int count) throws IOException
		{
			mOutPipe.out.write(buffer, offset, count);
			sendMessage();
		}
	}
}
	

