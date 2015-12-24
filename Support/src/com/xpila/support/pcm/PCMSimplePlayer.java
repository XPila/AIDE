package com.xpila.support.pcm;


import android.media.AudioTrack;


public class PCMSimplePlayer
extends PCMPlayer
implements IPCMConsumer
{
	public boolean start()
	{	mTrack.play();
		return (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING); }
	public boolean stop()
	{	mTrack.stop();
		return (mTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED); }
	public boolean pause()
	{	mTrack.pause();
		return (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED); }
	public PCMFormat inputFormat()
	{ return mFormat; }
	public int write(Object buffer, int position, int size)
	{
		if (mFormat.bits == 8)
			return mTrack.write((byte[])buffer, position, size);
		else if (mFormat.bits == 16)
			return mTrack.write((short[])buffer, position, size);
		return 0;
	}
}
