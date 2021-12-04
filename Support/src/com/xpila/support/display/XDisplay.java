package com.xpila.support.display;

import android.view.Display;
import android.graphics.Point;


public class XDisplay
{
	//display resolution constants
	//resolution is encoded as two 12bit numbers (bit 0-11 = X, bit 12-23 = Y)
	public static final int RES_X_MSK   = 0x00000fff;
	public static final int RES_Y_MSK   = 0x00fff000;
	public static final int RES_MSK     = RES_X_MSK | RES_Y_MSK;
	//LANDSCAPE (x > y)
	public static final int RES_LQQVGA  = 160 + (120 << 12);
	public static final int RES_LHQVGA  = 240 + (160 << 12);
	public static final int RES_LQVGA   = 320 + (240 << 12);
	public static final int RES_LWQVGA  = 384 + (240 << 12);
	public static final int RES_LWQVGA1 = 360 + (240 << 12);
	public static final int RES_LWQVGA2 = 400 + (240 << 12);
	public static final int RES_LHVGA   = 480 + (320 << 12);
	public static final int RES_LVGA    = 640 + (480 << 12);
	public static final int RES_LWVGA   = 768 + (480 << 12);
	public static final int RES_LWVGA1  = 720 + (480 << 12);
	public static final int RES_LWVGA2  = 800 + (480 << 12);
	public static final int RES_LFWVGA  = 854 + (480 << 12);
	public static final int RES_LSVGA   = 800 + (600 << 12);
	public static final int RES_LDVGA   = 960 + (640 << 12);
	public static final int RES_LWSVGA  = 1024 + (576 << 12);
	public static final int RES_LWSVGA1 = 1024 + (600 << 12);
	public static final int RES_LXGA    = 1024 + (768 << 12);
	public static final int RES_LWXGA   = 1152 + (768 << 12);
	public static final int RES_LWXGA1  = 1280 + (768 << 12);
	public static final int RES_LWXGA2  = 1280 + (800 << 12);
	public static final int RES_LWXGA3  = 1360 + (768 << 12);
	public static final int RES_LFWXGA  = 1366 + (768 << 12);
	public static final int RES_LXGAp   = 1152 + (864 << 12);
	public static final int RES_LWXGAp  = 1440 + (900 << 12);
	public static final int RES_LWSXGA  = 1440 + (960 << 12);
	public static final int RES_LSXGA   = 1280 + (1024 << 12);
	public static final int RES_LSXGAp  = 1400 + (1050 << 12);
	public static final int RES_LWSXGAp = 1680 + (1050 << 12);
	public static final int RES_LUXGA   = 1600 + (1200 << 12);
	public static final int RES_LWUXGA  = 1920 + (1200 << 12);
	//PORTRAIT (y > x)
	public static final int RES_PQQVGA  = 120 + (160 << 12);
	public static final int RES_PHQVGA  = 160 + (240 << 12);
	public static final int RES_PQVGA   = 240 + (320 << 12);
	public static final int RES_PWQVGA  = 240 + (384 << 12);
	public static final int RES_PWQVGA1 = 240 + (360 << 12);
	public static final int RES_PWQVGA2 = 240 + (400 << 12);
	public static final int RES_PHVGA   = 320 + (480 << 12);
	public static final int RES_PVGA    = 480 + (640 << 12);
	public static final int RES_PWVGA   = 480 + (768 << 12);
	public static final int RES_PWVGA1  = 480 + (720 << 12);
	public static final int RES_PWVGA2  = 480 + (800 << 12);
	public static final int RES_PFWVGA  = 480 + (854 << 12);
	public static final int RES_PSVGA   = 600 + (800 << 12);
	public static final int RES_PDVGA   = 640 + (960 << 12);
	public static final int RES_PWSVGA  = 576 + (1024 << 12);
	public static final int RES_PWSVGA1 = 600 + (1024 << 12);
	public static final int RES_PXGA    = 768 + (1024 << 12);
	public static final int RES_PWXGA   = 768 + (1152 << 12);
	public static final int RES_PWXGA1  = 768 + (1280 << 12);
	public static final int RES_PWXGA2  = 800 + (1280 << 12);
	public static final int RES_PWXGA3  = 768 + (1360 << 12);
	public static final int RES_PFWXGA  = 768 + (1366 << 12);
	public static final int RES_PXGAp   = 864 + (1152 << 12);
	public static final int RES_PWXGAp  = 900 + (1440 << 12);
	public static final int RES_PWSXGA  = 960 + (1440 << 12);
	public static final int RES_PSXGA   = 1024 + (1280 << 12);
	public static final int RES_PSXGAp  = 1050 + (1400 << 12);
	public static final int RES_PWSXGAp = 1050 + (1680 << 12);
	public static final int RES_PUXGA   = 1200 + (1600 << 12);
	public static final int RES_PWUXGA  = 1200 + (1920 << 12);
	//display density constant
	//density is encoded as 6bit number (bit 24-29), the number is real density divided by 40
	public static final int DNS_MSK     = 0x2f000000;
	public static final int DNS_LDPI    = ((120/40) << 24); // 120/160=0.75
	public static final int DNS_MDPI    = ((160/40) << 24); // 160/160=1
	public static final int DNS_HDPI    = ((240/40) << 24); // 240/160=1.5
	public static final int DNS_XHDPI   = ((320/40) << 24); // 320/160=2
	public static final int DNS_XXHDPI  = ((480/40) << 24); // 480/160=3
	public static final int DNS_XXXHDPI = ((640/40) << 24); // 640/160=4
	//display orientation constants
	//orientation is encoded as 2bit number (bit 30-31)
	public static final int ORI_MSK = 0xc0000000;
	public static final int ORI_0   = (0 << 30); //0 degress
	public static final int ORI_90  = (1 << 30); //90 degress
	public static final int ORI_180 = (2 << 30); //180 degress
	public static final int ORI_270 = (3 << 30); //270 degress
	//public members
	public int disp = 0;
	public int width = 0;
	public int height = 0;
	public int orientation = 0;
	public Point realSize = new Point(0, 0);
	public int realWidth = 0;
	public int realHeight = 0;
	//constructor
	public XDisplay(int d)
	{
		disp = d;
		width = d & RES_X_MSK;
		height = (d & RES_Y_MSK) >> 12;
		orientation = (d & ORI_MSK) >> 30;
	}
	public XDisplay(Display display)
	{
		width = display.getWidth();
		height = display.getHeight();
		orientation = display.getOrientation();
		int w = width;
		int h = height;
		//DisplayMetrics metrics = new DisplayMetrics();
		//display.getMetrics(metrics);
		try
		{
			display.getRealSize(realSize);
			realWidth = realSize.x;
			realHeight = realSize.y;
			w = realWidth;
			h = realHeight;
		}		
		catch (NoSuchMethodError err) {}
		int disp_res = w | (h << 12);
		int disp_dns = 0;
		int disp_rot = (orientation << 30);
		disp = disp_res | disp_dns | disp_rot;
	}
	//compare method
	public int compare(XDisplay xdisplay)
	{
		if (xdisplay == null) return -1;
		if (xdisplay.disp != disp) return 1;
		return 0;
	}
	//returns resolution string
	public static String dispResStr(int disp)
	{
		int w = disp & RES_X_MSK;
		int h = (disp & RES_Y_MSK) >> 12;
		if (w < h)
		{ //switch w and h
			w += h;
			h = w - h;
			w -= h;
		}		
		int disp_res = w | (h << 12);
		switch (disp_res)
		{
			case RES_LQQVGA: return "QQVGA";
			case RES_LHQVGA: return "HQVGA";
			case RES_LQVGA: return "QVGA";
			case RES_LWQVGA: return "WQVGA";
			case RES_LWQVGA1: return "WQVGA1";
			case RES_LWQVGA2: return "WQVGA2";
			case RES_LHVGA: return "HVGA";
			case RES_LVGA: return "VGA";
			case RES_LWVGA: return "WVGA";
			case RES_LWVGA1: return "WVGA1";
			case RES_LWVGA2: return "WVGA2";
			case RES_LFWVGA: return "FWVGA";
			case RES_LSVGA: return "SVGA";
			case RES_LDVGA: return "DVGA";
			case RES_LWSVGA: return "WSVGA";
			case RES_LWSVGA1: return "WSVGA1";
			case RES_LXGA: return "XGA";
			case RES_LWXGA: return "WXGA";
			case RES_LWXGA1: return "WXGA1";
			case RES_LWXGA2: return "WXGA2";
			case RES_LWXGA3: return "WXGA3";
			case RES_LFWXGA: return "FWXGA";
			case RES_LXGAp: return "XGA+";
			case RES_LWXGAp: return "WXGA+";
			case RES_LWSXGA: return "WSXGA";
			case RES_LSXGA: return "SXGA";
			case RES_LSXGAp: return "SXGA+";
			case RES_LWSXGAp: return "WSXGA+";
			case RES_LUXGA: return "UXGA";
			case RES_LWUXGA: return "WUXGA";
		}
		return "";
	}
	//returns density string
	public static String dispDnsStr(int disp)
	{
		switch (disp & DNS_MSK)
		{
			case DNS_LDPI: return "LDPI";
			case DNS_MDPI: return "MDPI";
			case DNS_HDPI: return "HDPI";
			case DNS_XHDPI: return "XHDPI";
			case DNS_XXHDPI: return "XXHDPI";
			case DNS_XXXHDPI: return "XXXHDPI";
		}
		return "";
	}
}


