package com.xpila.support.pcm;


import android.media.AudioFormat;


public class PCMFormat
{
	public int bits = 0;
	public int channels = 0;
	public int frequency = 0;
	public boolean isValid()
	{
		if ((bits == 8) || (bits == 16))
			if ((channels == 1) || (channels == 2))
				if ((frequency == 11025) || (frequency == 22050) || (frequency == 44100))
					return true;
		return false;
	}
	public PCMFormat(int bits, int channels, int frequency)
	{
		this.bits = bits;
		this.channels = channels;
		this.frequency = frequency;
	}
	public static int channelConfigIn(int channels)
	{
		if (channels == 1) return AudioFormat.CHANNEL_IN_MONO;
		else if (channels == 2) return AudioFormat.CHANNEL_IN_STEREO;
		return AudioFormat.CHANNEL_INVALID;
	}
	public static int channelConfigOut(int channels)
	{
		if (channels == 1) return AudioFormat.CHANNEL_OUT_MONO;
		else if (channels == 2) return AudioFormat.CHANNEL_OUT_STEREO;
		else if (channels == 4) return AudioFormat.CHANNEL_OUT_QUAD;
		return AudioFormat.CHANNEL_INVALID;
	}
	public static int audioFormat(int bits)
	{
		if (bits == 8) return AudioFormat.ENCODING_PCM_8BIT;
		else if (bits == 16) return AudioFormat.ENCODING_PCM_16BIT;
		return AudioFormat.ENCODING_INVALID;
	}
}

