package com.xpila.support.io;


import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;


public class Pipe
{
	public PipedOutputStream out;
	public PipedInputStream in;
	public Pipe(int size)
	{
		out = new PipedOutputStream();
		try
		{ in = new PipedInputStream(out, size); }
		catch (IOException e)
		{}
	}
}


