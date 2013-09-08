package com.sh1r0.noveldroid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class PrefsActivity extends PreferenceActivity {
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle aSavedState) {
		super.onCreate(aSavedState);
		if (Build.VERSION.SDK_INT < 11) {
			addPreferencesFromResource(R.xml.settings);
		} else {
			getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
		}
	}
/*
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
	}
*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static public class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
		}
	}
}