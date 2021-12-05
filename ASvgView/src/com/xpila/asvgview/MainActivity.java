package com.xpila.asvgview;


import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Picture;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import com.xpila.support.log.Log;
import com.xpila.support.view.ZoomAndShiftImageView;
import com.xpila.support.view.SwipeGestureDetector;
import com.xpila.support.bitmap.BitmapHelper;


import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;
import com.larvalabs.svgandroid.SVGParseException;


class Icon
{
	protected IconHeader mHeader = null;
	protected Bitmap mBitmap_AND = null;
	protected Bitmap mBitmap_XOR = null;
	public Icon()
	{

	}
	public Icon(int w, int h, int b)
	{

	}
	public Icon(Bitmap bitmap)
	{
		
	}
	public boolean LoadFromFile(String filename)
	{
		
		return false;
	}
	class IconHeader
	{
		protected int mReserved = 0;
		protected int mType = 0;
		protected int mImages = 0;
		public IconHeader()
		{
			
		}
		protected int read(InputStream in)
		{
			return 0;
		}
		protected int write(OutputStream out)
		{
			return 0;
		}
	}
	class IconEntry
	{
		protected int mWidth = 0;
		protected int mHeight = 0;
		public IconEntry()
		{

		}
		protected int read(InputStream in)
		{
			return 0;
		}
		protected int write(OutputStream out)
		{
			return 0;
		}
	}
}

public class MainActivity
extends Activity
implements View.OnLongClickListener
{
	//SVG display mode constants
	public final static int MODE_ASVG = 0; //android svg
	public final static int MODE_XSVG = 1; //xsvg - not implemented
	public final static int MODE_WV_URL = 2; //webview, direct loadURL
	public final static int MODE_WV_HTM = 3; //webview, inside html pake
	//Transparency mode constants (for png export)
	public final static int TRAN_ALPHA = 0; //use alpha-channel
	public final static int TRAN_BLACK = 1; //black is transparent
	public final static int TRAN_WHITE = 2; //white is transparent
	
	protected int mMode = MODE_WV_HTM; //SVG display mode
	protected int mJPGQuality = 100; //output jpg quality
	protected int mTransparency = TRAN_ALPHA; //transparency mode (png export)
	protected File mInput = null; //input file
	protected LinearLayout mMain = null;
	protected ImageView mImgView = null;
	protected WebView mWebView = null;
	protected MainWebViewClient mWebViewClient = new MainWebViewClient();
	protected int mImgWidth = 0;
	protected int mImgHeight = 0;
	MainScriptInterface mMainSI = new MainScriptInterface();
	protected Drawable mSvgDrawable = null;
	@Override protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		Log.init("/mnt/sdcard/" + getClass().getName() + ".log");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Log.log("android.os.Build.VERSION.SDK_INT=%d", android.os.Build.VERSION.SDK_INT);
		//Log.log("android.os.Build.VERSION_CODES.GINGERBREAD=%d", android.os.Build.VERSION_CODES.GINGERBREAD);
		//Log.log("android.os.Build.VERSION_CODES.GINGERBREAD_MR1=%d", android.os.Build.VERSION_CODES.GINGERBREAD_MR1);
		
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			mMode = MODE_ASVG;
		}

		setContentView(R.layout.main);
		mMain = (LinearLayout)findViewById(R.id.llMain);

		if (!getFileStreamPath("files").exists())
		{
			deployFiles();
		}
		
		mWebView = (WebView)mMain.findViewById(R.id.wv);
		mWebView.setWebViewClient(mWebViewClient);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		//mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.addJavascriptInterface(mMainSI, "main");
		mWebView.setBackgroundColor(0x00000000);
		mWebView.setDrawingCacheQuality(WebView.DRAWING_CACHE_QUALITY_HIGH);

		mImgView = (ImageView)mMain.findViewById(R.id.iv);
			
		Intent intent = getIntent();
		String action = intent.getAction();
		if (Intent.ACTION_MAIN.equals(action))
		{
			if (getFileStreamPath("test.svg").exists())
			{
				mMode = MODE_ASVG;
				mInput = getFileStreamPath("test.svg");
			}
//			mInput = new File("/sdcard/projects/android/aide/asvgview/ic_launcher.svg");
		}
		
		if (Intent.ACTION_VIEW.equals(action))
		{
			try { mInput = new File(new URI(intent.getDataString())); }
			catch (URISyntaxException e) {}
		}
		if (mInput != null)
		{
			refresh();
		}
	}
	@Override protected void onDestroy()
	{
		Log.done();
		super.onDestroy();
	}
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.exportapkicons).setVisible(true);
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
			case R.id.refresh: refresh(); break;
			case R.id.savejpg: onMenu_SaveAsJPG(); break;
			case R.id.savepng: onMenu_SaveAsPNG(); break;
			case R.id.bkgnd_transparent: mWebView.setBackgroundColor(0x00000000); break;
			case R.id.bkgnd_black: mWebView.setBackgroundColor(0xff000000); break;
			case R.id.bkgnd_white: mWebView.setBackgroundColor(0xffffffff); break;
			case R.id.bkgnd_black_25: mWebView.setBackgroundColor(0xff404040); break;
			case R.id.bkgnd_black_50: mWebView.setBackgroundColor(0xff808080); break;
			case R.id.bkgnd_black_75: mWebView.setBackgroundColor(0xffc0c0c0); break;
			case R.id.trans_alpha: mTransparency = TRAN_ALPHA; break;
			case R.id.trans_black: mTransparency = TRAN_BLACK; break;
			case R.id.trans_white: mTransparency = TRAN_WHITE; break;
			case R.id.jpgq_30: mJPGQuality = 30; break;
			case R.id.jpgq_50: mJPGQuality = 50; break;
			case R.id.jpgq_75: mJPGQuality = 75; break;
			case R.id.jpgq_90: mJPGQuality = 90; break;
			case R.id.jpgq_100: mJPGQuality = 100; break;
			case R.id.vmode_asvg: mMode = MODE_ASVG; refresh(); break;
			case R.id.vmode_wvurl: mMode = MODE_WV_URL; refresh(); break;
			case R.id.vmode_wvhtm: mMode = MODE_WV_HTM; refresh(); break;
			case R.id.quit: finish(); break;
			case R.id.exportapkicons: onMenu_ExportApkIcons(); break;
		}
		return false;
	}

	@Override public boolean onLongClick(View p1)
	{
		return false;
	}
	protected void refresh()
	{
		mImgWidth = 0;
		mImgHeight = 0;
		mSvgDrawable = null;
		if (mMode == MODE_WV_HTM)
		{
			mWebView.setVisibility(View.VISIBLE);
			mImgView.setVisibility(View.GONE);			
			String baseURL = "file://" + mInput.getParent() + "/";
			String data = loadRawResourceAsText(R.raw.view);
			String svg = loadTextFile(mInput.getAbsolutePath());
			data = data.replace("<svg/>", svg);
			Log.log("DATA %s", data);
			Log.log("BASEURL %s", baseURL);
			mWebView.loadDataWithBaseURL(baseURL, data, "text/html", "ansi", baseURL);
		}
		else if (mMode == MODE_WV_URL)
		{
			mWebView.setVisibility(View.VISIBLE);
			mImgView.setVisibility(View.GONE);
			mWebView.loadUrl("file://" + mInput.getAbsolutePath());
		}
		else if (mMode == MODE_ASVG)
		{
			mWebView.setVisibility(View.GONE);
			mImgView.setVisibility(View.VISIBLE);
			Log.log("ASVG open file %s", mInput.getAbsolutePath());
			try
			{
				FileInputStream inputStream = new FileInputStream(mInput);
				Log.log(" FileInputStream - %s", (inputStream == null)?"NG":"OK");
				SVGBuilder builder = new SVGBuilder();
				builder.readFromInputStream(inputStream);
				SVG svg = builder.build();
				inputStream.close();
				Log.log(" svg - %s", (svg == null)?"NG":"OK");
				if (svg != null)
				{
					Drawable drawable = svg.getDrawable();
					Log.log(" drawable - %s", (svg == null)?"NG":"OK");
					if (drawable != null)
					{
						Rect bounds = drawable.getBounds();
						Log.log("  Bounds left:", bounds.left);
						Log.log("  Bounds top:", bounds.top);
						Log.log("  Bounds right:", bounds.right);
						Log.log("  Bounds bottom:", bounds.bottom);				
						mImgView.setImageDrawable(drawable);
						mSvgDrawable = drawable;
					}
				}
			} catch (IOException | SVGParseException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	protected void onMenu_SaveAsJPG()
	{
		Bitmap bitmap = getSvgBitmap();		
		String filename = mInput.getAbsolutePath() + ".jpg";
		saveBitmapToJpg(bitmap, filename, mJPGQuality);
	}
	boolean matchColor(int clr0, int clr1, float tolC, float tolI)
	{
		float r0 = (float)((clr0 >> 16) & 0xff) / 0xff;
		float g0 = (float)((clr0 >> 8) & 0xff) / 0xff;
		float b0 = (float)(clr0 & 0xff) / 0xff;
		float i0 = (r0 + g0 + b0) / 3;
		float r1 = (float)((clr1 >> 16) & 0xff) / 0xff;
		float g1 = (float)((clr1 >> 8) & 0xff) / 0xff;
		float b1 = (float)(clr1 & 0xff) / 0xff;
		float i1 = (r1 + g1 + b1) / 3;
		float dr = Math.abs(r1 - r0);
		float dg = Math.abs(g1 - g0);
		float db = Math.abs(b1 - b0);
		float di = Math.abs(i1 - i0);
		if (di > tolI) return false;
		if (dr > tolC) return false;
		if (dg > tolC) return false;
		if (db > tolC) return false;
		return true;
	}
	protected void onMenu_SaveAsPNG()
	{
		Bitmap bitmap = getSvgBitmap();
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		String filename = mInput.getAbsolutePath() + ".png";
		if (mTransparency == TRAN_WHITE)
		{
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++)
				{
					int c = bitmap.getPixel(x, y);
					//if ((c & 0xffffff) == 0xffffff)
					if (matchColor(c, 0xffffff, 0.1F, 0.1F))
						bitmap.setPixel(x, y, 0x00000000);
				}
		}
		saveBitmapToPng(bitmap, filename);
	}
	protected void onMenu_SaveAsICO()
	{
		Bitmap bitmap = getSvgBitmap();
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Icon icon = new Icon(bitmap);
	}
	protected void onMenu_ExportApkIcons()
	{
		/*
		new
		 LDPI    = 0.75 x MDPI    36
		 MDPI    =    1 x MDPI    48
		 TVDPI   = 1.33 x MDPI    64
		 HDPI    =  1.5 x MDPI    72
		 XHDPI   =    2 x MDPI    96
		 XXHDPI  =    3 x MDPI   144
		 XXXHDPI =    4 x MDPI   192
		
		old, maybe wrong
		 XXHDPI 1   144
		 LDPI   2/9 32
		 MDPI   1/3 48
		 HDPI   4/9 64
		 XHDPI  2/3 96
		 */
		
		Bitmap bitmap = getSvgBitmap();
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		String filename = mInput.getAbsolutePath() + ".png";
		if (mTransparency == TRAN_WHITE)
		{
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++)
				{
					int c = bitmap.getPixel(x, y);
					//if ((c & 0xffffff) == 0xffffff)
					if (matchColor(c, 0xffffff, 0.1F, 0.1F))
						bitmap.setPixel(x, y, 0x00000000);
				}
		}
		String dir = mInput.getParent() + "/res/";
		String name = mInput.getName().replaceFirst(".svg$", ".png");
		Log.log("dir: '%s'    name: '%s'", dir, name);
		BitmapHelper.saveBitmapResPngs(bitmap, dir, name);
//		saveBitmapToPng(bitmap, filename);
	}
	
	class MainWebViewClient
	extends WebViewClient
	{
		public void onPageFinished(WebView view, String url)
		{
		}	
	}
	class MainScriptInterface
	{
		public void imgWH(int w, int h)
		{
			mImgWidth = w;
			mImgHeight = h;
		}
	}

	protected Bitmap getSvgBitmap()
	{
		if ((mMode == MODE_WV_HTM) || (mMode == MODE_WV_URL))
		{
			Picture picture = mWebView.capturePicture();
			if (mImgWidth == 0) mImgWidth = picture.getWidth();
			if (mImgHeight == 0) mImgHeight = picture.getHeight();
			Bitmap bitmap = Bitmap.createBitmap(mImgWidth, mImgHeight, Bitmap.Config.ARGB_8888);
			bitmap.eraseColor(0x00000000);
			Canvas canvas = new Canvas(bitmap);
			picture.draw(canvas);
			return bitmap;
		}
		else if (mMode == MODE_ASVG)
		{
			Drawable drawable = mSvgDrawable;
			Rect bounds = drawable.getBounds();
			if (mImgWidth == 0) mImgWidth = bounds.width();
			if (mImgHeight == 0) mImgHeight = bounds.height();
			Bitmap bitmap = Bitmap.createBitmap(mImgWidth, mImgHeight, Bitmap.Config.ARGB_8888);
			bitmap.eraseColor(0x00000000);
			Canvas canvas = new Canvas(bitmap);
			drawable.draw(canvas);
			return bitmap;
		}
		return null;
	}
	protected String loadRawResourceAsText(int id)
	{
		InputStream in = null;
		try {
			in = getResources().openRawResource(id);
			String text = readText(in);
			in.close();
			return text;
		} catch (IOException e) { e.printStackTrace(); }
		if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
		return null;
	}
	protected boolean deployFiles()
	{
		Log.log("deployFiles");
		if (extractFile("files"))
		{
			String[] list = loadTextFile(getFileStreamPath("files").getAbsolutePath()).split("\\r?\\n");
			return extractFiles(list);
		}
		return false;
	}
	protected boolean extractFiles(String[] fileNames)
	{
		Log.log("extractFiles");
		boolean result = true;
		for (int i = 0; i < fileNames.length; i++)
			result &= extractFile(fileNames[i]);
		return result;
	}
	protected boolean extractFile(String fileName)
	{
		Log.log("extractFile %s", fileName);
		InputStream in = null;
		OutputStream out = null;
		try {
			in = getAssets().open(fileName);
			out = openFileOutput(fileName, 0);
			long copy = copyData(in, out);
			in.close();
			out.close();
			return copy >= 0;
		} catch (IOException e) { e.printStackTrace(); }
		if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
		if (out != null) try { out.close(); } catch (IOException e) { e.printStackTrace(); }
		return false;
	}
/*	protected String loadText(String fileName)
	{
		InputStream in = null;
		try {
			in = openFileInput(fileName);
			String text = readText(in);
			in.close();
			return text;
		} catch (IOException e) { e.printStackTrace(); }
		if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
		return null;
	}*/
	protected long copyData(InputStream in, OutputStream out)
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
	protected String readText(InputStream in)
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
	protected void chmod()
	{
		
		//getFileStreamPath(
	}
	protected String loadTextFile(String fileName)
	{
		InputStream in = null;
		try {
			in = new FileInputStream(fileName);
			String text = readText(in);
			in.close();
			return text;
		} catch (IOException e) { e.printStackTrace(); }
		if (in != null) try { in.close(); } catch (IOException e) { e.printStackTrace(); }
		return null;
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
}


