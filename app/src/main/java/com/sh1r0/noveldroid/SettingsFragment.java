package com.sh1r0.noveldroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class SettingsFragment extends PreferenceFragmentCompat implements
	SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String KEY_ENCODING = "encoding";
	private static final String KEY_NAMING_RULE = "naming_rule";
	private static final String KEY_DOWN_DIR = "down_dir";
	private static final String KEY_CHECK_UPDATE = "check_update";
	private static final String KEY_ABOUT = "about";
	private static final int FILE_CODE = 0;

	private String[] namingRuleList;
	private Preference encoding;
	private Preference namingRule;
	private Preference downDir;
	private Preference checkUpdate;
	private Preference about;
	private int namingRulePref;
	private SharedPreferences prefs;
	private String versionName;
	private String aboutMessage;

	private DownloadManager manager;
	private BroadcastReceiver receiver;
	private DownloadManager.Request request;
	private long downloadId;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		addPreferencesFromResource(R.xml.settings);
		prefs = getPreferenceManager().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);

		try {
			versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
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
				Intent i = new Intent(getActivity(), FilePickerActivity.class);
				i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
				i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
				i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
				i.putExtra(FilePickerActivity.EXTRA_START_PATH, prefs.getString(KEY_DOWN_DIR, NovelUtils.APP_DIR));

				startActivityForResult(i, FILE_CODE);
				return true;
			}
		});

		checkUpdate = findPreference(KEY_CHECK_UPDATE);
		checkUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				UpdateChecker updateChecker = new UpdateChecker(versionName);
				updateChecker.execute("sh1r0/NovelDroid");
				return true;
			}
		});

		about = findPreference(KEY_ABOUT);
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(android.R.id.content, AboutFragment.newInstance(versionName));
				ft.addToBackStack(null);
				ft.commit();
				return true;
			}
		});

		manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)){
					DownloadManager.Query query = new DownloadManager.Query();
					query.setFilterById(downloadId);
					Cursor c = manager.query(query);
					if (c.moveToFirst()) {
						int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
						if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
							String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setDataAndType(Uri.parse(uriString), "application/vnd.android.package-archive");
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(i);
						}
					}
				}
			}
		};
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {

	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.settings);
		encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
		namingRulePref = Integer.parseInt(prefs.getString(KEY_NAMING_RULE, "0"));
		namingRule.setSummary(namingRuleList[namingRulePref]);
		downDir.setSummary(prefs.getString(KEY_DOWN_DIR, NovelUtils.APP_DIR));
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		getActivity().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	@Override
	public void onPause() {
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		getActivity().unregisterReceiver(this.receiver);
		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			downDir.getSharedPreferences().edit().putString(KEY_DOWN_DIR, uri.getPath() + "/").commit();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch (key) {
			case KEY_ENCODING:
				encoding.setSummary(sharedPreferences.getString(KEY_ENCODING, "UTF-8"));
				break;
			case KEY_NAMING_RULE:
				namingRulePref = Integer.parseInt(sharedPreferences.getString(KEY_NAMING_RULE, "0"));
				namingRule.setSummary(namingRuleList[namingRulePref]);
				break;
			case KEY_DOWN_DIR:
				downDir.setSummary(sharedPreferences.getString(KEY_DOWN_DIR, NovelUtils.APP_DIR));
				break;
		}
	}

	private class UpdateChecker extends AsyncTask<String, Integer, Pair<String, URL>> {
		private ProgressDialog progressDialog;
		private String currentVersion;
		private File apkPath;

		public UpdateChecker(String versionName) {
			super();
			this.currentVersion = versionName;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(),
					getResources().getString(R.string.check_update), getResources().getString(R.string.checking));
		}

		@Override
		protected Pair<String, URL> doInBackground(String... repos) {
			ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
					Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
			if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
				return null;
			}

			String repo = repos[0];
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(String.format("https://api.github.com/repos/%s/releases", repo));
			HttpResponse response;
			try {
				response = client.execute(get);
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 4096);

				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				is.close();

				JSONArray releases = new JSONArray(sb.toString());
				JSONObject latestRelease = releases.getJSONObject(0);
				String latestVersion = latestRelease.getString("tag_name");
				URL downloadURL = new URL(latestRelease.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"));

				File tempDir = new File(NovelUtils.TEMP_DIR);
				if (!tempDir.exists()) {
					tempDir.mkdirs();
				}
				this.apkPath = new File(tempDir, "noveldroid.apk");

				return new Pair<>(latestVersion, downloadURL);
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(final Pair<String, URL> versionPair) {
			progressDialog.dismiss();

			if (versionPair == null) {
				Toast.makeText(getActivity(), R.string.check_update_fail_tooltip, Toast.LENGTH_LONG).show();
				return;
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setCancelable(false);

			if (NovelUtils.versionCompare(currentVersion, versionPair.first) == 0) {
				dialog.setMessage(R.string.current_is_latest);
				dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
			} else {
				String msg = getString(R.string.latest_version) + versionPair.first + "\n"
						+ getString(R.string.current_version) + currentVersion;
				dialog.setTitle(R.string.found_update).setMessage(msg);
				dialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request = new DownloadManager.Request(Uri.parse(versionPair.second.toString()));
						request.setDestinationUri(Uri.fromFile(apkPath));
						request.setMimeType("application/vnd.android.package-archive");
						downloadId = manager.enqueue(request);
					}
				});
			}
			dialog.show();
		}
	}
}
