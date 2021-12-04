package com.xpila.support.view;


import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;;


public class ViewHelper
{
	public static int findChildrenBySimpleClassName(ViewGroup group, ArrayList<View> list, String name, int depth)
	{
		int found = 0;
		int count = group.getChildCount();
		for (int i = 0; i < count; i++)
		{ 
			View v = group.getChildAt(i);
			if (v.getClass().getSimpleName().compareTo(name) == 0)
				found += list.add(v)?1:0;
			else if ((depth > 0) && (v instanceof ViewGroup))
				found += findChildrenBySimpleClassName((ViewGroup)v, list, name, depth - 1);
		}
		return found;
	}
	public static void setOnClickListener(ArrayList<View> list, View.OnClickListener listener)
	{
		int size = list.size();
		for (int i = 0; i < size; i++)
			list.get(i).setOnClickListener(listener);
	}
}
