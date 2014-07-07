package com.sh1r0.noveldroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerActivity;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;

public class SettingsFragment extends PreferenceCompatFragment implements
	SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String KEY_ENCODING = "encoding";
	private static final String KEY_NAMING_RULE = "naming_rule";
	private static final String KEY_DOWN_DIR = "down_dir";
	private static final String KEY_CHECK_UPDATE = "check_update";
	private static final String KEY_ABOUT = "about";
	private static final int EX_FILE_PICKER_RESULT = 0;

	private String[] namingRuleList;
	private Preference encoding;
	private Preference namingRule;
	private Preference downDir;
	private Preference checkUpdate;
	private Preference about;
	private int namingRulePref;
	private SharedPreferences prefs;
	private String versionName;
	private int versionCode;
	private String aboutMessage;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		addPreferencesFromResource(R.xml.settings);
		prefs = getPreferenceManager().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);

		Parse.initialize(getActivity(), ParseAppKey.APP_ID, ParseAppKey.CLIENT_KEY);

		try {
			versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			versionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		aboutMessage = getString(R.string.version_tag) + versionName + "\n" + getString(R.string.author_tag) + getString(R.string.author_name);

		namingRuleList = this.getResources().getStringArray(R.array.naming_rule);

		encoding = findPreference(KEY_ENCODING);
		namingRule = findPreference(KEY_NAMING_RULE);

		downDir = findPreference(KEY_DOWN_DIR);
		downDir.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(ApplicationContextProvider.getContext(), ExFilePickerActivity.class);
				intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
				intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_DIRECTORIES);
				intent.putExtra(ExFilePicker.SET_START_DIRECTORY, prefs.getString(KEY_DOWN_DIR, NovelUtils.APP_DIR));
				intent.putExtra(ExFilePicker.DISABLE_SORT_BUTTON, true);
				startActivityForResult(intent, EX_FILE_PICKER_RESULT);
				return true;
			}
		});

		checkUpdate = findPreference(KEY_CHECK_UPDATE);
		checkUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
					Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
				if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
					Toast.makeText(getActivity(), R.string.no_connection_tooltip, Toast.LENGTH_SHORT).show();
					return false;
				}

				final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),
					getResources().getString(R.string.check_update), getResources().getString(R.string.checking));

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("versionCode", versionCode);
				ParseCloud.callFunctionInBackground("checkLatestVersion", params, new FunctionCallback<String>() {
					public void done(String latestVer, ParseException e) {
						progressDialog.dismiss();

						if (e == null) {
							AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
							dialog.setCancelable(false);

							if (latestVer.equals("latest")) {
								dialog.setMessage(R.string.current_is_latest);
								dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								});
							} else {
								String msg = getString(R.string.latest_version) + latestVer + "\n"
									+ getString(R.string.current_version) + versionName;
								dialog.setTitle(R.string.found_update).setMessage(msg);
								dialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								}).setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										final DownloadTask downloadTask = new DownloadTask(getActivity());
										ParseCloud.callFunctionInBackground("getLatestDownloadLink",
											new HashMap<String, Object>(), new FunctionCallback<String>() {
												public void done(String link, ParseException e) {
													if (e == null) {
														downloadTask.execute(link);
													}
												}
											}
										);
									}
								});
							}

							dialog.show();
						} else {
							Toast.makeText(getActivity(), R.string.check_update_fail_tooltip, Toast.LENGTH_LONG).show();
						}
					}
				});

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
				dialog.setMessage(aboutMessage);
				dialog.setCancelable(false);
				dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
	public void onResume() {
		super.onResume();
		encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
		namingRulePref = Integer.parseInt(prefs.getString(KEY_NAMING_RULE, "0"));
		namingRule.setSummary(namingRuleList[namingRulePref]);
		downDir.setSummary(prefs.getString(KEY_DOWN_DIR, NovelUtils.APP_DIR));
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EX_FILE_PICKER_RESULT) {
			if (data != null) {
				ExFilePickerParcelObject object = data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
				if (object.count > 0) {
					downDir.getEditor().putString(KEY_DOWN_DIR, object.path + object.names.get(0) + "/").commit();
				}
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_ENCODING)) {
			encoding.setSummary(sharedPreferences.getString(KEY_ENCODING, "UTF-8"));
		} else if (key.equals(KEY_NAMING_RULE)) {
			namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, "0"));
			namingRule.setSummary(namingRuleList[namingRulePref]);
		} else if (key.equals(KEY_DOWN_DIR)) {
			downDir.setSummary(sharedPreferences.getString(KEY_DOWN_DIR, NovelUtils.APP_DIR));
		}
	}

	private class DownloadTask extends AsyncTask<String, Integer, String> {
		private ProgressDialog mDialog;
		private File tempDir;
		private File apkPath;

		public DownloadTask(Context context) {
			this.tempDir = new File(NovelUtils.TEMP_DIR);
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			this.apkPath = new File(tempDir, "noveldroid.apk");

			mDialog = new ProgressDialog(context);
			mDialog.setTitle(R.string.downloading_tooltip);
			mDialog.setCancelable(false);
			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialog.setIndeterminate(true);
			mDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			mDialog.setProgress(progress[0]);
		}

		protected void onPostExecute(String result) {
			mDialog.dismiss();
			if (result == null) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(this.apkPath), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}

		@Override
		protected String doInBackground(String... sUrl) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				int fileLength = connection.getContentLength();
				mDialog.setMax(fileLength);
				mDialog.setIndeterminate(false);
				mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

				input = connection.getInputStream();
				output = new FileOutputStream(apkPath);

				byte data[] = new byte[4096];
				int total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					output.write(data, 0, count);
					publishProgress(total);
				}
				output.close();
			} catch (Exception e) {
				return e.toString();
			}

			return null;
		}
	}
}
