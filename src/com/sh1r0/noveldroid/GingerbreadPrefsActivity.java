package com.sh1r0.noveldroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class GingerbreadPrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String KEY_ENCODING = "encoding";
	private static final String KEY_NAMING_RULE = "naming_rule";
	private static final String KEY_ABOUT = "about";
	
	private String[] namingRuleList;
	private Preference encoding;
	private Preference namingRule;
	private Preference about;
	private int namingRulePref;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		namingRuleList = this.getResources().getStringArray(R.array.naming_rule);
		
		encoding = findPreference(KEY_ENCODING);
		namingRule = findPreference(KEY_NAMING_RULE);
		
		about = findPreference(KEY_ABOUT);
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(GingerbreadPrefsActivity.this);
				dialog.setIcon(android.R.drawable.ic_dialog_info);
				dialog.setTitle(R.string.about);
				dialog.setMessage(R.string.author_info);
				dialog.setCancelable(false);
				dialog.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				dialog.show();
				return true;
			}
		});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_ENCODING)) {
			encoding.setSummary(sharedPreferences.getString(KEY_ENCODING, "UTF-8"));
		} else if (key.equals(KEY_NAMING_RULE)) {
			namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, "0"));
			namingRule.setSummary(namingRuleList[namingRulePref]);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
	    super.onResume();
	    SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
	    encoding.setSummary(sharedPreferences.getString(KEY_ENCODING, "UTF-8"));
		namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, "0"));
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