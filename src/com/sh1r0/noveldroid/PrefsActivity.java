package com.sh1r0.noveldroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
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
		private static final String KEY_DOWN_DIR = "down_dir";

		private String[] namingRuleList;
		private Preference encoding;
		private Preference namingRule;
		private Preference downDir;
		private Preference about;
		private int namingRulePref;
		private SharedPreferences prefs;
		private String version;
		private String message;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);

			try {
				version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			message = getString(R.string.version_tag) + version + "\n" + getString(R.string.author_tag)
					+ getString(R.string.author_name);

			namingRuleList = this.getResources().getStringArray(R.array.naming_rule);

			encoding = findPreference(KEY_ENCODING);
			namingRule = findPreference(KEY_NAMING_RULE);

			downDir = findPreference(KEY_DOWN_DIR);
			downDir.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(getActivity(),
							new DirectoryChooserDialog.ChosenDirectoryListener() {
								@Override
								public void onChosenDir(String chosenDir) {
									downDir.getEditor().putString(KEY_DOWN_DIR, chosenDir + "/").commit();
								}
							});
					directoryChooserDialog.setNewFolderEnabled(true);
					directoryChooserDialog.chooseDirectory(prefs.getString(KEY_DOWN_DIR, Config.appDir));
					return true;
				}
			});

			about = findPreference(KEY_ABOUT);
			about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
					dialog.setIcon(android.R.drawable.ic_dialog_info);
					dialog.setTitle(R.string.about);
					dialog.setMessage(message);
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

			prefs = getPreferenceManager().getSharedPreferences();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(KEY_ENCODING)) {
				encoding.setSummary(sharedPreferences.getString(KEY_ENCODING, "UTF-8"));
			} else if (key.equals(KEY_NAMING_RULE)) {
				namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, "0"));
				namingRule.setSummary(namingRuleList[namingRulePref]);
			} else if (key.equals(KEY_DOWN_DIR)) {
				downDir.setSummary(sharedPreferences.getString(KEY_DOWN_DIR, Config.appDir));
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
			namingRulePref = Integer.parseInt(prefs.getString(KEY_NAMING_RULE, "0"));
			namingRule.setSummary(namingRuleList[namingRulePref]);
			downDir.setSummary(prefs.getString(KEY_DOWN_DIR, Config.appDir));
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}
	}
}