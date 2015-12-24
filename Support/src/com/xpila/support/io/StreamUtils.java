package com.xpila.support.io;


import java.io.InputStream;
import java.io.OutputStream;
//import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InterruptedIOException;

	
public class StreamUtils
{
	public static String readText(InputStream in)
	{
		try
		{
			int available = in.available();
			if (available > 0)
			{
				char[] text = new char[available];
				InputStreamReader reader = new InputStreamReader(in);
				int readed = reader.read(text, 0, available);
				return new String(text);
			}
		}
		catch (IOException e) { }
		return null;
	}
	public static boolean writeText(OutputStream out)
	{
		return true;
	}
	public static int streamCopy(InputStream in, OutputStream out, byte[] buffer)
	{
		int total = 0;
		try
		{
			int available = in.available();
			int chunk = buffer.length;
			while (available > 0)
			{
				if (chunk > available) chunk = available;
				if (chunk > 0)
				{
					int readed = in.read(buffer, 0, chunk);
					if (readed == 0) break;
					out.write(buffer, 0, readed);
					total += readed;
				}
				available -= chunk;
			}
		}
		catch (InterruptedIOException e) { return -1; }
		catch (IOException e) { }
		return total;
	}

}
