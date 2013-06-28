package com.xuejia;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



public class PreferencesManager {
	public static void setBgColor(Context context,String color){
		try {	
			PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("preference_bg_color", color);
			editor.commit();	
		} catch (Exception e) {

		}
	}
	public static String getBgColor(Context context){
		String defaultColor="";
		try {	

			PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			String res = settings.getString("preference_bg_color", defaultColor);		
			return  res;
		} catch (Exception e) {
			return defaultColor;
		}
	}
	
	public static void setBgImage(Context context,String color){
		try {	
			PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("preference_bg_img", color);
			editor.commit();	
		} catch (Exception e) {

		}
	}
	public static String getBgImage(Context context){
		String defaultURL="";
		try {	

			PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			String res = settings.getString("preference_bg_img", defaultURL);		
			return  res;
		} catch (Exception e) {
			return defaultURL;
		}
	}
}
