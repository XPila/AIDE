package com.xpila.support.charset;

import android.view.View;
import android.graphics.Typeface;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;

import com.xpila.support.log.Log;


public class CharsetGenerator
{
	public final static int ENCODING_ASCII = 0;
	public final static int ENCODING_ANSI = 1;
	private CharsetGenerator() {}
	public static char encode_2UNICODE(int ch, int encoding)
	{
		switch (encoding)
		{
		case ENCODING_ASCII: return encode_ASCII2UNICODE(ch);
		case ENCODING_ANSI: return encode_ANSI2UNICODE(ch);
		}
		return (char)ch;
	}
	public static char encode_ASCII2UNICODE(int ch)
	{
		if (ch < 0x20) //control characters
			switch (ch)
			{
			case 0x00: return 0x0000; //(000) [NUL]
			//case 0x01: return 0x263a; //(001) [SOH] open smiley face
			//case 0x02: return 0x263b; //(002) [STX] solid smiley face
			case 0x01: return 0x2460; //(001) [SOH]
			case 0x02: return 0x2461; //(002) [STX]
			case 0x03: return 0x2665; //(003) [ETX] heart suit symbol
			case 0x04: return 0x2662; //(004) diamond suit symbol
			case 0x05: return 0x2663; //(005) club suit symbol
			case 0x06: return 0x2660; //(006) spades suit symbol
			case 0x07: return 0x0000; //(007) [BEL]
			case 0x08: return 0x0000; //(008) [BS]
			case 0x09: return 0x0000; //(009) [HT]
			case 0x0a: return 0x0000; //(010) [LF]
			case 0x0b: return 0x0000; //(011) [VT]
			case 0x0c: return 0x0000; //(012) [FF]
			case 0x0d: return 0x0000; //(013) [CR]
			case 0x0e: return 0x266c; //(014) [SO] music symbol
			case 0x0f: return 0x2600; //(015) [SI] sun symbol
			
			case 0x10: return 0x25b6; //(016) [DLE]
			case 0x11: return 0x25c0; //(017) [DC1]
			case 0x12: return 0x2195; //(018) [DC2]
			case 0x13: return 0x203c; //(019) [DC3]
					
			}
		else if (ch < 0x7f) //printable characters
			return (char)ch;
		else
			switch (ch)
			{
			case 0x7f: return 0x0000; //(127) ???
			case 0x80: return 0x00c7; //(128)
			case 0x81: return 0x00fc; //(129) ??
			case 0x82: return 0x00e9; //(130)
			case 0x83: return 0x00e2; //(131)
			case 0x84: return 0x00e4; //(132)
			case 0x85: return 0x00e0; //(133) 
			case 0x86: return 0x00e5; //(134)
			case 0x87: return 0x00e7; //(135)
			case 0x88: return 0x00ea; //(136)
			case 0x89: return 0x00eb; //(137)
			case 0x8a: return 0x00e8; //(138)
			case 0x8b: return 0x00cf; //(139)
			case 0x8c: return 0x00ce; //(140)
			case 0x8d: return 0x00cc; //(141)
			case 0x8e: return 0x00c4; //(142)
			case 0x8f: return 0x00c2; //(143)

			case 0x90: return 0x00c9; //(144)
			case 0x91: return 0x00e6; //(145)
			case 0x92: return 0x00c6; //(146)
			case 0x93: return 0x00f4; //(147)
			case 0x94: return 0x00f6; //(148) ??
			case 0x95: return 0x00f2; //(149) 
			case 0x96: return 0x00fb; //(150)
			case 0x97: return 0x00f9; //(151)
			case 0x98: return 0x00ff; //(152)
			case 0x99: return 0x00f6; //(153)
			case 0x9a: return 0x00fc; //(154) ??
			case 0x9b: return 0x00a2; //(155)
			case 0x9c: return 0x0000; //(156) ???
			case 0x9d: return 0x00a5; //(157)
			case 0x9e: return 0x0000; //(158) ???
			case 0x9f: return 0x0192; //(159)

			case 0xa0: return 0x00e1; //(160)
			case 0xa1: return 0x00ed; //(161)
			case 0xa2: return 0x00f3; //(162)
			case 0xa3: return 0x00fa; //(163)
			case 0xa4: return 0x00f1; //(164)
			case 0xa5: return 0x00d1; //(165) 
			case 0xa6: return 0x0000; //(166) ???
			case 0xa7: return 0x0000; //(167) ???
			case 0xa8: return 0x00bf; //(168)
			case 0xa9: return 0x0000; //(169) ???
			case 0xaa: return 0x00ac; //(170)
			case 0xab: return 0x00bd; //(171)
			case 0xac: return 0x00bc; //(172)
			case 0xad: return 0x00a1; //(173)
			case 0xae: return 0x00ab; //(174)
			case 0xaf: return 0x00bb; //(175)

			case 0xb0: return 0x0000; //(176)
			case 0xb1: return 0x2592; //(177)
			case 0xb2: return 0x2593; //(178)
			case 0xb3: return 0x2502; //(179)
			case 0xb4: return 0x2524; //(180)
			case 0xb5: return 0x2561; //(181)
			case 0xb6: return 0x2562; //(182)
			case 0xb7: return 0x2556; //(183)
			case 0xb8: return 0x2555; //(184)
			case 0xb9: return 0x2563; //(185)
			case 0xba: return 0x2551; //(186)
			case 0xbb: return 0x2557; //(187)
			case 0xbc: return 0x255d; //(188)
			case 0xbd: return 0x255c; //(189)
			case 0xbe: return 0x255b; //(190)
			case 0xbf: return 0x2510; //(191)
			
			case 0xc0: return 0x2514; //(192)
			case 0xc1: return 0x2534; //(193)
			case 0xc2: return 0x252c; //(194)
			case 0xc3: return 0x251c; //(195)
			case 0xc4: return 0x2500; //(196)
			case 0xc5: return 0x253c; //(197)
			case 0xc6: return 0x255e; //(198)
			case 0xc7: return 0x255f; //(199)
			case 0xc8: return 0x255a; //(200)
			case 0xc9: return 0x2554; //(201)
			case 0xca: return 0x2569; //(202)
			case 0xcb: return 0x2566; //(203)
			case 0xcc: return 0x2560; //(204)
			case 0xcd: return 0x2550; //(205)
			case 0xce: return 0x256c; //(206)
			case 0xcf: return 0x2567; //(207)
			
			case 0xd0: return 0x2568; //(208)
			case 0xd1: return 0x2564; //(209)
			case 0xd2: return 0x2565; //(210)
			case 0xd3: return 0x2559; //(211)
			case 0xd4: return 0x2558; //(212)
			case 0xd5: return 0x2552; //(213)
			case 0xd6: return 0x2553; //(214)
			case 0xd7: return 0x256b; //(215)
			case 0xd8: return 0x256a; //(216)
			case 0xd9: return 0x2518; //(217)
			case 0xda: return 0x250c; //(218)
			case 0xdb: return 0x2588; //(219)
			case 0xdc: return 0x2584; //(220)
			case 0xdd: return 0x258c; //(221)
			case 0xde: return 0x258c; //(222) ??
			case 0xdf: return 0x2584; //(223) ??
				
			}
		return 0x0000;
	}
	public static char encode_ANSI2UNICODE(int ch)
	{
		// todo
		return (char)ch;
	}
	public static Paint createPaint(float textSize, float textScaleX)
	{
		Paint paint = new Paint();
		paint.setColor(0xffffffff);
		paint.setAntiAlias(true);
		paint.setTypeface(Typeface.MONOSPACE);
		paint.setTextSize(textSize);
		paint.setTextScaleX(textScaleX);
		return paint;
	}
	public static int calcCellWidth(float textSize, float textScaleX, float cellScaleX)
	{
		Paint paint = createPaint(textSize, textScaleX);
		float[] char_widths = new float[1];
		paint.breakText(" ", true, 128, char_widths);
		float char_width = char_widths[0];		
		int cell_width = (int)(char_width * cellScaleX);
		return cell_width;
	}
	public static int calcCellHeight(float textSize, float cellScaleY)
	{
		Paint paint = createPaint(textSize, 1);
		Paint.FontMetrics fontmetrics = paint.getFontMetrics();
		float char_height = fontmetrics.bottom - fontmetrics.top;
		int cell_height = (int)(char_height * cellScaleY);
		return cell_height;
	}
	public static Bitmap createCharset(int encoding, float textSize, float textScaleX, float textShiftY, float cellScaleX, float cellScaleY)
	{
		Paint paint = createPaint(textSize, textScaleX);
		Paint.FontMetrics fontmetrics = paint.getFontMetrics();
		float[] char_widths = new float[1];
		paint.breakText(" ", true, 128, char_widths);
		float char_width = char_widths[0];
		float char_height = fontmetrics.bottom - fontmetrics.top;
		int cell_width = (int)(char_width * cellScaleX);
		int cell_height = (int)(char_height * cellScaleY);
//		int char_base = (int)(-fontmetrics.top * cellScaleY);
		int char_base = (int)(-fontmetrics.top);
		int char_left = (int)(((float)cell_width - char_widths[0]) / 2);
		Bitmap charset = Bitmap.createBitmap(cell_width, cell_height * 0x100, Bitmap.Config.ALPHA_8);
		Canvas canvas = new Canvas(charset);
		char[] ch = new char[1];
		int yshift = (int)(textShiftY * cell_height);
		for (int i = 0; i < 0x100; i++)
		{
			ch[0] = encode_2UNICODE(i, encoding);
			paint.breakText(ch, 0, 1, 128, char_widths);
			Paint paint_draw = paint;
			int xo = 0;
			int yo = 0;
			if (char_widths[0] > cell_width)
			{
				paint_draw = new Paint(paint);
				paint_draw.setTextScaleX(textScaleX * cell_width / char_widths[0]);
			}
			if (i == 0xde) xo = (int)((float)cell_width / 2 + 0.5);
			if (i == 0xdf) yo = (int)((float)-cell_height / 2 + 0.5);
			canvas.clipRect(0, cell_height * i, cell_width, charset.getHeight());
			canvas.drawText(ch, 0, 1, char_left + xo, cell_height * i + char_base + yo + yshift, paint_draw);
		}
		return charset;
	}
	public static void drawChar(Canvas canvas, Bitmap charset, int ch, int x, int y, float z, Paint paint_back, Paint paint_fore)
	{
		ch &= 0xff;
		int cell_width = charset.getWidth();
		int cell_height = charset.getHeight() >> 8;
		Rect src = new Rect(0, cell_height * ch, cell_width, cell_height * (ch + 1));
		Rect dst = new Rect(x, y, x + (int)(cell_width * z), y + (int)(cell_height * z));
		canvas.drawRect(dst, paint_back);
		canvas.drawBitmap(charset, src, dst, paint_fore);
	}
}

