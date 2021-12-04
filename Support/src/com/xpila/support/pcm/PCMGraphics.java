package com.xpila.support.pcm;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.xpila.support.log.Log;


public class PCMGraphics
{
	public static float[] samples2lines(byte[] buffer, int position, int size, int channels, int channel, float x, float y, float factorX, float factorY)
	{
		int count = size / channels;
		float[] points = new float[(4 * (count - 1))];
		int i = 0;
		while (i < count - 1)
		{	if (i == 0)
			{	points[4 * i + 0] = x + (i) * factorX;
				points[4 * i + 1] = y - (buffer[position + channels * i + channel]) * factorY; }
			else
			{	points[4 * i + 0] = points[4 * i - 2];
				points[4 * i + 1] = points[4 * i - 1]; }
			points[4 * i + 2] = x + ((i + 1)) * factorX;
			points[4 * i + 3] = y - (buffer[position + channels * (i + 1) + channel]) * factorY;
			i++; }
		return points;
	}
	public static float[] samples2points(byte[] buffer, int position, int size, int channels, int channel, float x, float y, float factorX, float factorY)
	{
		int count = size / channels;
		float[] points = new float[(2 * count)];
		int i = 0;
		while (i < count)
		{	points[2 * i + 0] = x + (i) * factorX;
			points[2 * i + 1] = y - (buffer[position + channels * i + channel]) * factorY;
			i++; }
		return points;
	}
	public static float[] samples2lines(short[] buffer, int position, int size, int channels, int channel, float x, float y, float factorX, float factorY)
	{
		int count = size / channels;
		float[] points = new float[(4 * (count - 1))];
		int i = 0;
		while (i < count - 1)
		{	if (i == 0)
			{	points[4 * i + 0] = x + (i) * factorX;
				points[4 * i + 1] = y - (buffer[position + channels * i + channel]) * factorY; }
			else
			{	points[4 * i + 0] = points[4 * i - 2];
				points[4 * i + 1] = points[4 * i - 1]; }
			points[4 * i + 2] = x + ((i + 1)) * factorX;
			points[4 * i + 3] = y - (buffer[position + channels * (i + 1) + channel]) * factorY;
			i++; }
		return points;
	}
	public static float[] samples2points(short[] buffer, int position, int size, int channels, int channel, float x, float y, float factorX, float factorY)
	{
		int count = size / channels;
		float[] points = new float[(2 * count)];
		int i = 0;
		while (i < count)
		{	points[2 * i + 0] = x + (i) * factorX;
			points[2 * i + 1] = y - (buffer[position + channels * i + channel]) * factorY;
			i++; }
		return points;
	}
	public static void drawSamples(Canvas canvas, Paint paint, byte[] buffer, int position, int size, int channels, int channel, float x, float y, float factorX, float factorY, boolean lines)
	{
		float[] points = null;
		if (lines) points = samples2lines(buffer, position, size, channels, channel, x, y, factorX, factorY);
		else points = samples2points(buffer, position, size, channels, channel, x, y, factorX, factorY);
		if (lines) canvas.drawLines(points, paint);
		else canvas.drawPoints(points, paint);
	}
	public static void drawSamples(Canvas canvas, Paint paint, short[] buffer, int position, int size, int channels, int channel, float x, float y, float factorX, float factorY, boolean lines)
	{
		float[] points = null;
		if (lines) points = samples2lines(buffer, position, size, channels, channel, x, y, factorX, factorY);
		else points = samples2points(buffer, position, size, channels, channel, x, y, factorX, factorY);
		if (lines) canvas.drawLines(points, paint);
		else canvas.drawPoints(points, paint);
	}
	public static void drawGrid(Canvas canvas, Paint paint, int w, int h, int divsX, int divsY)
	{
		float[] points = null;
		if (divsX > 0)
		{
			int i = 0;
			points = new float[(4 * (divsX + 1))];
			while (i <= divsX)
			{
				points[4 * i + 0] = i * (w - 1) / divsX;
				points[4 * i + 1] = 0;
				points[4 * i + 2] = points[4 * i + 0];
				points[4 * i + 3] = h - 1;
				i++;
			}
			canvas.drawLines(points, paint);
		}
		if (divsY > 0)
		{
			int i = 0;
			points = new float[(4 * (divsY + 1))];
			while (i <= divsY)
			{
				points[4 * i + 0] = 0;
				points[4 * i + 1] = i * (h - 1) / divsY;
				points[4 * i + 2] = w - 1;
				points[4 * i + 3] = points[4 * i + 1];
				i++;
			}
			canvas.drawLines(points, paint);
		}
	}
}

