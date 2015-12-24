package com.xpila.support.console;

/*Console*/
//import android.os.Handler;
//import android.os.Message;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.EditText;
//import android.content.Context;
//import android.util.AttributeSet;
//import android.text.InputFilter;
//import android.text.Spanned;
//import android.graphics.Typeface;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.io.PipedInputStream;
//import java.io.PipedOutputStream;
//import java.io.IOException;
//import javax.crypto.*;
//import android.graphics.*;
//import java.net.*;
import java.io.*;


public class ConsoleHelper
{
	public InputStream in = null;
	public PrintStream out = null;
	public ConsoleHelper putch(char ch)
	{ if (out != null) out.write(ch); return this; }
	public ConsoleHelper format(String format, Object... args)
	{ if (out != null) out.format(format, args); return this; }
	public ConsoleHelper printf(String format, Object... args)
	{ if (out != null) out.printf(format, args); return this; }
	public char getch()
	{
		if (in == null) return 0;
		try
		{ return (char)in.read(); }
		catch (IOException e)
		{ return 0; }
	}
	public String readLine(String format, Object... args)
	{
		format(format, args);
		return readLine();
	}
	public String readLine()
	{
		String line = "";
		int ch = 0;
		while (ch != '\n')
		{
			try
			{ ch = in.read(); }
			catch (IOException e)
			{ break; }
			out.write(ch);
			line += ch;
		}
		return line;
	}
	public String readPassword(String format, Object... args)
	{
		format(format, args);
		return readPassword();
	}
	public String readPassword()
	{
		String line = "";
		int ch = 0;
		while (ch != '\n')
		{
			try
			{ ch = in.read(); }
			catch (IOException e)
			{ break; }
			out.write('*');
			line += ch;
		}
		return line;
	}
}
