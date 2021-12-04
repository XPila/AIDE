package com.xpila.support.activity;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.view.Display;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.graphics.Point;
import java.util.Stack;
import com.xpila.support.display.XDisplay;


public class XActivity
extends Activity
{
	public final static String xmlns = "http://schemas.android.com/apk/res/android";
	public interface OptionsMenuListener
	{
		public boolean onCreateOptionsMenu(Menu menu);
		public void onOptionsMenuClosed(Menu menu);
		public boolean onOptionsItemSelected(MenuItem item);
	}
	public interface ContextMenuListener
	{
		public boolean onCreateContextMenu(Menu menu);
		public void onContextMenuClosed(Menu menu);
		public boolean onContextItemSelected(MenuItem item);
	}
	protected static FrameLayout mDisplayFrame = null;
	protected static FrameLayout mContentFrame = null;
	protected static View mContentView = null;
	protected static Stack<View> mContentViews = null;
	protected OptionsMenuListener mOptionsMenuListener = null;
	protected ContextMenuListener mContextMenuListener = null;
	protected static XDisplay mDisplay = null;
	protected static XDisplay mFakeDisplay = null;
	protected boolean mDisplayChanged = false;
	@Override protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		WindowManager windowManager = getWindowManager();
		Display display = (windowManager != null)?windowManager.getDefaultDisplay():null;
		XDisplay xdisplay = new XDisplay(display);
		if (xdisplay.compare(mDisplay) != 0)
		{
			mDisplay = xdisplay;
			mDisplayChanged = true;
		}
	}
	@Override public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		if (mDisplayFrame != null)
		{
			super.setContentView(mDisplayFrame);
		}
		if (mContentView != null)
		{
			if (mContentFrame != null)
				mContentFrame.addView(mContentView);
			else
				super.setContentView(mContentView);
		}
	}
	@Override public void onDetachedFromWindow()
	{
		if (mContentView != null)
		{
			ViewGroup parent = (ViewGroup)mContentView.getParent();
			if (parent != null) parent.removeView(mContentView);
		}
		if (mDisplayFrame != null)
		{
			ViewGroup parent = (ViewGroup)mDisplayFrame.getParent();
			if (parent != null) parent.removeView(mDisplayFrame);
		}
		super.onDetachedFromWindow();
	}	
	@Override protected void onDestroy()
	{
		onDetachedFromWindow();
		if (isFinishing())
		{
			mDisplayFrame = null;
			mContentFrame = null;
			mContentView = null;
			mContentViews = null;
			mDisplay = null;
			mFakeDisplay = null;
		}
		super.onDestroy();
	}
	@Override public void onBackPressed()
	{
		if (popContentView() == null)
			super.onBackPressed();
	}	
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		if (mOptionsMenuListener != null)
			return mOptionsMenuListener.onCreateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override public void onOptionsMenuClosed(Menu menu)
	{
		super.onOptionsMenuClosed(menu);
	}
	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}
	@Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	@Override public void onContextMenuClosed(Menu menu)
	{
		super.onContextMenuClosed(menu);
	}
	@Override public boolean onContextItemSelected(MenuItem item)
	{
		return super.onContextItemSelected(item);
	}
	public XDisplay getXDisplay()
	{
		if (mFakeDisplay != null) return mFakeDisplay;
		return mDisplay;
	}
	public View popContentView()
	{
		if (mContentView != null)
		{
			ViewGroup parent = (ViewGroup)mContentView.getParent();
			if (parent != null) parent.removeView(mContentView);
		}
		if ((mContentViews != null) && (mContentViews.size() > 0) && ((mContentView = mContentViews.pop()) != null))
		{
			if (mContentFrame != null)
				mContentFrame.addView(mContentView);
			else
				super.setContentView(mContentView);
		}
		else
			mContentView = null;
		return mContentView;
	}
	public View pushContentView()
	{
		if (mContentViews == null)
			mContentViews = new Stack<View>();
		if (mContentView != null)
		{
			ViewGroup parent = (ViewGroup)mContentView.getParent();
			if (parent != null) parent.removeView(mContentView);
			mContentViews.push(mContentView);
		}
		return mContentView;
	}
	public View getContentView()
	{
		return mContentView;
	}
	public void setContentView(View view)
	{
		mContentView = view;
		if (mContentFrame != null)
			mContentFrame.addView(mContentView);
		else
			super.setContentView(mContentView);
	}
	public void setContentView(int layoutResID)
	{
		setContentView(View.inflate(this, layoutResID, null));
	}
	public void pushSetContentView(View view)
	{
		pushContentView();
		setContentView(view);
	}	
	public void setOptionsMenuListener(OptionsMenuListener listener)
	{
		mOptionsMenuListener = listener;
	}
	public void setContextMenuListener(ContextMenuListener listener)
	{
		mContextMenuListener = listener;
	}
	public void fakeDisplay(XDisplay fakeDisplay)
	{
		if (fakeDisplay != null)
		{
			if ((fakeDisplay.compare(mFakeDisplay) != 0) || mDisplayChanged)
			{
				if (mFakeDisplay != null)
				{
					if (mContentView != null)
					{
						ViewGroup parent = (ViewGroup)mContentView.getParent();
						if (parent != null) parent.removeView(mContentView);
					}
					if (mDisplayFrame != null)
					{
						ViewGroup parent = (ViewGroup)mDisplayFrame.getParent();
						if (parent != null) parent.removeView(mDisplayFrame);
					}
					mDisplayFrame = null;
					mContentFrame = null;
				}
				mDisplayFrame = new FrameLayout(this);
				mContentFrame = new FrameLayout(this);
				mDisplayFrame.addView(mContentFrame);
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(fakeDisplay.width, fakeDisplay.height);
				params.leftMargin = (mDisplay.width - fakeDisplay.width) / 2;
				params.topMargin = (mDisplay.height - fakeDisplay.height) / 2;
				if (params.leftMargin < 0) params.leftMargin = 0;
				if (params.topMargin < 0) params.topMargin = 0;
				mContentFrame.setLayoutParams(params);
				mFakeDisplay = fakeDisplay;
				mDisplayChanged = true;
			}
		}
		else
		{
			if (mContentView != null)
			{
				ViewGroup parent = (ViewGroup)mContentView.getParent();
				if (parent != null) parent.removeView(mContentView);
			}
			if (mDisplayFrame != null)
			{
				ViewGroup parent = (ViewGroup)mDisplayFrame.getParent();
				if (parent != null) parent.removeView(mDisplayFrame);
			}
			mDisplayFrame = null;
			mContentFrame = null;
		}
	}
}

