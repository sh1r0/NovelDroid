package com.sh1r0.noveldroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

@SuppressLint("NewApi")
public class PrefsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
	}

	static public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		private static final String KEY_ENCODING = "encoding";
		private static final String KEY_NAMING_RULE = "naming_rule";
		private static final String KEY_ABOUT = "about";
		
		private String[] namingRuleList;
		private Preference encoding;
		private Preference namingRule;
		private Preference about;
		private int namingRulePref;
		
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
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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

		@Override
		public void onResume() {
		    super.onResume();
		    SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		    encoding.setSummary(sharedPreferences.getString(KEY_ENCODING, "UTF-8"));
			namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, "0"));
			namingRule.setSummary(namingRuleList[namingRulePref]);
		    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
		    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		    super.onPause();
		}
	}
}