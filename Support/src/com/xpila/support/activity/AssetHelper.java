package com.xpila.support.activity;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.xpila.support.log.Log;

public class AssetHelper
{
	public static boolean deployFiles(Context context, String fileName_List)
	{
		Log.log("AssetHelper.deployFiles");
		if (fileName_List == null) fileName_List = "files";
		if (extractFile(context, fileName_List))
		{
			String[] list = loadText(context, fileName_List).split("\\r?\\n");
			return extractFiles(context, list);
		}
		return false;
	}
	public static boolean extractFiles(Context context, String[] fileNames)
	{
		Log.log("AssetHelper.extractFiles");
		boolean result = true;
		for (int i = 0; i < fileNames.length; i++)
			result &= extractFile(context, fileNames[i]);
		return result;
	}
	public static boolean extractFile(Context context, String fileName)
	{
		Log.log("AssetHelper.extractFile %s", fileName);
		InputStream in = null;
		OutputStream out = null;
		try {
			in = context.getAssets().open(fileName);
			out = context.openFileOutput(fileName, 0);
			long copy = copyData(in, out);
			in.close();
			out.close();
			return copy >= 0;
		} catch (IOException e) { e.printStackTrace(); }
		if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
		if (out != null) try { out.close(); } catch (IOException e) { e.printStackTrace(); }
		return false;
	}
	public static String loadText(Context context, String fileName)
	{
		InputStream in = null;
		try {
			in = context.openFileInput(fileName);
			String text = readText(in);
			in.close();
			return text;
		} catch (IOException e) { e.printStackTrace(); }
		if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
		return null;
	}
	public static long copyData(InputStream in, OutputStream out)
	{
		int buffSize = 512;
		byte[] buff = new byte[buffSize];
		long copy = 0;
		try {
			int avail = 0;
			while ((avail = in.available()) > 0)
			{
				int read = in.read(buff);
				out.write(buff, 0, read);
				copy += read;
			}
			return copy;
		} catch (IOException e) { e.printStackTrace(); }
		return -1;
	}
	public static String readText(InputStream in)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));			
		try {
			String text = "";
			String line;
			while ((line = reader.readLine()) != null)
				text += line;
			return text;
		} catch (IOException e) { e.printStackTrace(); }
		return null;
	}	
}
