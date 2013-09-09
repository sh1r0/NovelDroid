package com.sh1r0.noveldroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class GingerbreadPrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String KEY_ENCODING = "encoding";
	private static final String KEY_NAMING_RULE = "naming_rule";
	
	private String[] encodingList;
	private String[] namingRuleList;
	private Preference encoding;
	private Preference namingRule;
	private int encodingPref;
	private int namingRulePref;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		encoding = findPreference(KEY_ENCODING);
		namingRule = findPreference(KEY_NAMING_RULE);
		encodingList = this.getResources().getStringArray(R.array.encoding);
		namingRuleList = this.getResources().getStringArray(R.array.naming_rule);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_ENCODING)) {
			encodingPref = Integer.parseInt(sharedPreferences.getString(KEY_ENCODING, ""));
			encoding.setSummary(encodingList[encodingPref]);
		} else if (key.equals(KEY_NAMING_RULE)) {
			namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, ""));
			namingRule.setSummary(namingRuleList[namingRulePref]);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
	    super.onResume();
	    SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
	    encodingPref = Integer.parseInt(sharedPreferences.getString(KEY_ENCODING, ""));
		encoding.setSummary(encodingList[encodingPref]);
		namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, ""));
		namingRule.setSummary(namingRuleList[namingRulePref]);
	    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPause() {
	    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	    super.onPause();
	}
}