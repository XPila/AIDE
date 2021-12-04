package com.xpila.support.xport;

public interface XPort
{
	public int XPort_Rx(byte[] buf, int pos, int cnt);
	public int XPort_Tx(byte[] buf, int pos, int cnt);
}

