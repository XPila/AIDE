package com.xpila.support.log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;


public class Log
implements Thread.UncaughtExceptionHandler
{	
	private final static Log log = new Log();
	private File file = null;
	private ReentrantLock lock = null;
	private PrintStream out = null;
	private LogOutputStream sysout = null;
	private LogOutputStream syserr = null;
	private String datetimeFormat = null;
	private Log() {}
	public static boolean init(String filename)
	{ return init(filename, true, true, true); }
	public static boolean init(String filename, boolean append, boolean logSysOutErr, boolean logThreadExceptions)
	{
		if (log.out != null) done();
		log.file = new File(filename);
		log.lock = new ReentrantLock();
		try
		{
			if (!log.file.exists())
				log.file.createNewFile();
			if (log.open(append, logSysOutErr))
			{
				if (logThreadExceptions)
					Thread.setDefaultUncaughtExceptionHandler(log);
				return true;
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		log.sysout = null;
		log.syserr = null;
		log.out = null;
		log.lock = null;
		log.file = null;
		return false;
	}
	public static void done()
	{
		log.close();
		log.lock = null;
	}
	public static boolean clear()
	{
		if (log.file == null) return false;
		if (log.out != null)
		{
			log.lock.lock();
			boolean logSysOutErr = (log.sysout != null) && (log.syserr != null);
			log.close();
			boolean result = log.open(false, logSysOutErr);
			log.lock.unlock();
			return result;
		}
		try
		{
			log.file.createNewFile();
			return true;
		}
		catch (IOException e) { e.printStackTrace(); }
		return false;
	}
	public static boolean show()
	{
		try
		{
			Runtime.getRuntime().exec("am start -n com.xpila.alogview/.MainActivity -a android.intent.action.VIEW -d file://" + log.file.getAbsolutePath());
			return true;
		}
		catch (IOException e) { e.printStackTrace(); }
		return false;
	}
	public static void exit(int exitCode)
	{
		Runtime.getRuntime().exit(-1);
	}
	
	public static void log(String text)
	{
		if (log.out == null) return;
		log.lock.lock();
		Date now = new Date();
		log.out.printf("L %s %s\n", datetimeStr(now), text);
		//log.out.flush();
		log.lock.unlock();
	}
	public static void log(String format, Object... args)
	{
		if (log.out == null) return;
		log.lock.lock();
		Date now = new Date();
		log.out.printf("L %s %s\n", datetimeStr(now), String.format(format, args));
		//log.out.flush();
		log.lock.unlock();
	}
	public static void log(Thread thread, Throwable ex)
	{
		if (log.out == null) return;
		log.lock.lock();
		Date now = new Date();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.printf("Exception: %s\n", ex.getClass().getName());
		pw.printf("\tthread: %s\n", thread.getName());
		pw.printf("\tmessage: %s\n", ex.getMessage());
		pw.printf("\tstack trace:\n");
		String text = sw.toString();
		sw = new StringWriter();
		pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String trace = sw.toString().replace("\n", "\n\t\t");
		trace = trace.substring(0, trace.length() - 2);
		log.out.printf("L %s %s\t\t%s", datetimeStr(now), text, trace);
		//log.out.flush();
		log.lock.unlock();
	}
	public void uncaughtException(Thread thread, Throwable ex)
	{
		log(thread, ex);
		done();
		show();
		exit(-1);
	}
	private class LogOutputStream
	extends OutputStream
	{
		private PrintStream stream = null;
		private char prefix = 0;
		private boolean newLine = false;
		public LogOutputStream(PrintStream stream, char prefix)
		{
			super();
			this.stream = stream;
			this.prefix = prefix;
			newLine = true;
		}
		public void write(int oneByte) throws IOException
		{
			stream.write(oneByte);
			log.lock.lock();
			if (newLine) log.out.printf("%c %s ", prefix, datetimeStr(new Date()));
			log.out.write(oneByte);
			log.lock.unlock();
			newLine = (oneByte == '\n');
		}
	}
	private boolean open(boolean append, boolean logSysOutErr)
	{
		try
		{
			if (!file.exists())
				file.createNewFile();
			try
			{
				out = new PrintStream(new FileOutputStream(log.file, append));
				if (logSysOutErr)
				{
					sysout = log.new LogOutputStream(System.out, 'O');
					System.setOut(new PrintStream(log.sysout));
					syserr = log.new LogOutputStream(System.err, 'E');
					System.setErr(new PrintStream(log.syserr));
				}
				datetimeFormat = "%tF %tT.%tL";
				log("Log started.");
				return true;
			}
			catch (FileNotFoundException e) { e.printStackTrace(); }
		}
		catch (IOException e) { e.printStackTrace(); }
		return false;
	}
	private void close()
	{
		if (log.sysout != null)
			System.setOut(log.sysout.stream);
		if (log.syserr != null)
			System.setErr(log.syserr.stream);
		if (log.out != null)
		{
			log.log("Log finished.");
			log.out.close();
		}
		log.sysout = null;
		log.syserr = null;
		log.out = null;
	}
	private static String datetimeStr(Date date)
	{ return String.format(log.datetimeFormat, date, date, date, date); }
}

