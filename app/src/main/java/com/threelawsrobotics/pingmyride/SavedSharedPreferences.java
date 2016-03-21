package com.threelawsrobotics.pingmyride;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SavedSharedPreferences {
	static final String PREF_USERNAME = "username";
	static final String PREF_PASSWORD = "password";
	static final String PREF_INITIAL = "0";

	static SharedPreferences getSharedPreferences(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	public static void setUser(Context ctx, String username, String password, String initial) {
		SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
		editor.putString(PREF_USERNAME, username);
		editor.putString(PREF_PASSWORD, password);
		editor.putString(PREF_INITIAL, initial);
		editor.commit();
	}

	public static String getUsername(Context ctx) {
		return getSharedPreferences(ctx).getString(PREF_USERNAME, "");
	}

	public static String getPassword(Context ctx) {
		return getSharedPreferences(ctx).getString(PREF_PASSWORD, "");
	}

	public static String getInitial(Context ctx) {
		return getSharedPreferences(ctx).getString(PREF_INITIAL, "");
	}

	public static void clearUser(Context ctx) {
		SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
		editor.clear();
		editor.commit();
	}
}