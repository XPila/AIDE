package com.xpila.support.bitmap;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import java.nio.ByteBuffer;

import java.io.FileOutputStream;

import com.xpila.support.log.Log;


public class BitmapHelper
{
	public static void saveBitmapResPngs(Bitmap bmXXHDPI, String dstResDir, String dstResName)
	{
		int w = bmXXHDPI.getWidth();
		int h = bmXXHDPI.getHeight();		
		Bitmap bmLDPI = Bitmap.createScaledBitmap(bmXXHDPI, w*2/9, h*2/9, true);
		Bitmap bmMDPI = Bitmap.createScaledBitmap(bmXXHDPI, w*1/3, h*1/3, true);
		Bitmap bmHDPI = Bitmap.createScaledBitmap(bmXXHDPI, w*4/9, h*4/9, true);
		Bitmap bmXHDPI = Bitmap.createScaledBitmap(bmXXHDPI, w*2/3, h*2/3, true);
		saveBitmapToPng(bmLDPI, dstResDir + "drawable-ldpi/" + dstResName);
		saveBitmapToPng(bmMDPI, dstResDir + "drawable-mdpi/" + dstResName);
		saveBitmapToPng(bmHDPI, dstResDir + "drawable-hdpi/" + dstResName);
		saveBitmapToPng(bmXHDPI, dstResDir + "drawable-xhdpi/" + dstResName);
		saveBitmapToPng(bmXXHDPI, dstResDir + "drawable-xxhdpi/" + dstResName);
	}
	public static boolean resizeBitmapFile(String srcFileName, String dstFileName, int dstWidth, int dstHeight, Bitmap.CompressFormat format, int quality)
	{
		Bitmap srcBitmap = BitmapFactory.decodeFile(srcFileName);
		Bitmap dstBitmap = Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, true);
		return saveBitmapToFile(dstBitmap, dstFileName, format, quality);
	}
	public static boolean saveBitmapToPng(Bitmap bitmap, String fileName)
	{ return saveBitmapToFile(bitmap, fileName, Bitmap.CompressFormat.PNG, 100); }
	public static boolean saveBitmapToJpg(Bitmap bitmap, String fileName, int quality)
	{ return saveBitmapToFile(bitmap, fileName, Bitmap.CompressFormat.JPEG, quality); }
	public static boolean saveBitmapToFile(Bitmap bitmap, String fileName, Bitmap.CompressFormat format, int quality)
	{
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(fileName);
			bitmap.compress(format, quality, out);
			out.close();
			return true;
		}
		catch (Exception e) { e.printStackTrace(); }
		try { out.close(); } catch(Throwable ignore) {}
		return false;
	}
	
	public static void rawARGB8888_Grayscale(byte[] pixels)
	{
		int c = pixels.length;
		for (int i = 0; i < c; i += 4)
			pixels[i+0] = pixels[i+1] = pixels[i+2] = 
				(byte)(((pixels[i+0]&255) + (pixels[i+1]&255) + (pixels[i+2]&255)) / 3);
	}
	public static void rawARGB8888_Negative(byte[] pixels)
	{
		int c = pixels.length;
		for (int i = 0; i < c; i++)
			if ((i % 4) < 3)
				pixels[i] ^= -1;
	}
	public static byte[] rawPixelsFromBitmap(Bitmap bitmap)
	{
		byte[] pixels = new byte[bitmap.getRowBytes() * bitmap.getHeight()];
		bitmap.copyPixelsToBuffer(ByteBuffer.wrap(pixels));
		return pixels;
	}
	public static Bitmap bitmapFromRawPixels(byte[] pixels, int w, int h, Bitmap.Config config)
	{
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pixels));
		return bitmap;
	}
	public static void rawPixelsToBitmap(byte[] pixels, Bitmap bitmap)
	{ bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pixels)); }
	public static void bitmapToRawPixels(Bitmap bitmap, byte[] pixels)
	{ bitmap.copyPixelsToBuffer(ByteBuffer.wrap(pixels)); }




	
	public static Bitmap AlpahaTest(int w, int h)
	{
		
		//Bitmap dst = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
		Bitmap dst = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		//Canvas cnv = new Canvas(dst);
		//Paint pnt = new Paint();
		//pnt.setColor(0xff000000);
		//cnv.drawColor(0x00000000);
		//dst.
		//cnv.drawCircle(w / 2, h / 2, w / 2, pnt);
		for (int y = 0; y < h; y++)
		{
			//cnv.draw
			//int a = y * 255 / h;
			//pnt.setColor(a << 23);
			//cnv.drawLine(0, y, w - 1, y, pnt);
			for (int x = 0; x < w; x++)
			{
				int r = 0;
				int g = 0;
				int b = 0;
				int a = y * 255 / h;
				//r = g = b = a;
				//a = 0xff;
				//r = a;
				//a = 0;
				int c = b | (g << 8) | (r << 16) | (a << 24);
				dst.setPixel(x, y, c);
				//dst.setPixel(x, y, 0xffffffff);
			}
		}
	/*	dst = dst.extractAlpha();
		Log.log("bytecnt %d", dst.getRowBytes() * dst.getHeight());
		dst = dst.copy(Bitmap.Config.ALPHA_8, true);
		Log.log("bytecnt %d", dst.getRowBytes() * dst.getHeight());
		byte[] pix = PixelsFromBitmap(dst);
		dst = BitmapFromPixels(pix, w, h, Bitmap.Config.ALPHA_8);
		Log.log("bytecnt %d", dst.getRowBytes() * dst.getHeight());
		dst = dst.copy(Bitmap.Config.ARGB_8888, true);
		Log.log("bytecnt %d", dst.getRowBytes() * dst.getHeight());
		
		Bitmap dst2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		for (int y = 0; y < h; y++)
		{
			//cnv.draw
			//pnt.setColor(a << 23);
			//cnv.drawLine(0, y, w - 1, y, pnt);
			for (int x = 0; x < w; x++)
			{
				int c = dst.getPixel(x, y);
				//int c = 0;
				int b = c & 0xff;
				int g = (c >> 8) & 0xff;
				int r = (c >> 16) & 0xff;
				int a = (c >> 24) & 0xff;
				//int a = y * 255 / h;
				//int a = dst.getPixel(x, y);
				r = g = b = a;
				//r = g = b = a;
				a = 0xff;
				//r = a;
				//a = 0;
				c = b | (g << 8) | (r << 16) | (a << 24);
				dst2.setPixel(x, y, c);
				//dst.setPixel(x, y, 0xffffffff);
			}
		}
		return dst2;*/
		return null;
	}
}
