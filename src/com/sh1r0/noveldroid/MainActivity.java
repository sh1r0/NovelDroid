package com.sh1r0.noveldroid;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.slidingmenu.lib.SlidingMenu;

public class MainActivity extends Activity implements OnItemClickListener {
	private static final int SUCCESS = 0x10000;
	private static final int PREPARING = 0x10001;
	private static final int FAIL = 0x10002;
	
	private final int API_VERSION = Build.VERSION.SDK_INT;
	private int width;
	private EditText etID;
	private EditText etNovelName;
	private EditText etAuthor;
	private EditText etFromPage;
	private EditText etToPage;
	private Button btnAnalyze;
	private Button btnDownload;
	private TextView tvStatus;
	private TextView tvDownloadStatus;
	private Spinner spnDomain;
	private ProgressBar pbDownload;
	private NovelInfo novelInfo;
	private ProgressDialog progressDialog;
	private SharedPreferences prefs;
	private String filename;
	private String downDirPath;
	private String encoding;
	private NotificationManager mNotificationManager;
	private ListView slidingMenuItemList;
	private SlidingMenu slidingMenu;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		width = getScreenWidth();
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		
		etID = (EditText) findViewById(R.id.et_id);
		etNovelName = (EditText) findViewById(R.id.et_novel_name);
		etAuthor = (EditText) findViewById(R.id.et_author);
		etFromPage = (EditText) findViewById(R.id.et_from_page);
		etToPage = (EditText) findViewById(R.id.et_to_page);
		btnAnalyze = (Button) findViewById(R.id.btn_analyze);
		btnDownload = (Button) findViewById(R.id.btn_download);
		tvStatus = (TextView) findViewById(R.id.tv_status);
		tvDownloadStatus = (TextView) findViewById(R.id.tv_dl_status);
		spnDomain = (Spinner) findViewById(R.id.spn_doamin);
		pbDownload = (ProgressBar) findViewById(R.id.progressbar);

		ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this
				.getResources().getStringArray(R.array.domain));
		spnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDomain.setAdapter(spnAdapter);
		spnDomain.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		btnAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tvStatus.setText("");
				btnDownload.setEnabled(false);
				closeKeypad();

				if (!isNetworkConnected()) {
					Toast.makeText(getApplicationContext(), R.string.no_connection_tooltip, Toast.LENGTH_SHORT).show();
					return;
				}

				String tid = etID.getText().toString();
				if (tid.isEmpty()) {
					Toast.makeText(getApplicationContext(), R.string.novel_id_tooltip, Toast.LENGTH_SHORT).show();
					return;
				}

				try {
					novelInfo = Analysis.analysisUrl(spnDomain.getSelectedItemPosition(), tid);
				} catch (Exception e) {
					String err = (e.getMessage() == null) ? "analysis fail" : e.getMessage();
					Log.e("Error", err);
					return;
				}

				if (novelInfo == null || novelInfo.wrongUrl) {
					Log.e("Error", "Wrong URL");
					return;
				}

				etAuthor.setText(novelInfo.author);
				etNovelName.setText(novelInfo.name);
				etFromPage.setText("1");
				etToPage.setText(String.valueOf(novelInfo.lastPage));

				Toast.makeText(getApplicationContext(), R.string.analysis_done_tooltip, Toast.LENGTH_SHORT).show();
				btnDownload.setEnabled(true);
			}
		});

		btnDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeKeypad();
				novelInfo.name = etNovelName.getText().toString();
				novelInfo.author = etAuthor.getText().toString();
				novelInfo.fromPage = Integer.parseInt(etFromPage.getText().toString());
				novelInfo.toPage = Integer.parseInt(etToPage.getText().toString());

				if (novelInfo.name.isEmpty()) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.setTitle(R.string.error_dialog_title);
					dialog.setMessage(R.string.empty_name_dialog_msg);
					dialog.setCancelable(false);
					dialog.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					dialog.show();
					return;
				}

				if (novelInfo.fromPage < 1 || novelInfo.fromPage > novelInfo.toPage
						|| novelInfo.toPage > novelInfo.lastPage) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.setTitle(R.string.error_dialog_title);
					dialog.setMessage(R.string.wrong_page_dialog_msg);
					dialog.setCancelable(false);
					dialog.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					dialog.show();
					return;
				}

				File tempDir = new File(Config.tempDir);
				if (!tempDir.exists()) {
					tempDir.mkdirs();
				}

				pbDownload.setProgress(0);
				pbDownload.setVisibility(View.VISIBLE);
				tvDownloadStatus.setVisibility(View.VISIBLE);
				tvStatus.setText(R.string.downloading_tooltip);

				new Thread(new Runnable() {
					@Override
					public void run() {
						Downloader downloader = new Downloader(novelInfo);
						BookWriter bookWriter = new BookWriter(novelInfo);
						try {
							if (!downloader.startDownload(bookWriter, progressHandler)) {
								throw new IOException();
							} else {
								mHandler.sendEmptyMessage(PREPARING);
								encoding = prefs.getString("encoding", "UTF-8");
								downDirPath = prefs.getString("down_dir", Config.appDir);
								filename = bookWriter.makeBook(downDirPath,
										Integer.parseInt(prefs.getString("naming_rule", "0")), encoding);
								if (filename == null) {
									throw new IOException();
								} else {
									mHandler.sendEmptyMessage(SUCCESS);
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							mHandler.sendEmptyMessage(FAIL);
						}
					}
				}).start();
			}
		});
		
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.RIGHT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		slidingMenu.setMenu(R.layout.sliding_menu);
		slidingMenuItemList = (ListView) findViewById(R.id.sliding_menu);
		slidingMenuItemList.setAdapter(new ArrayAdapter<String>(this, R.layout.sliding_menu_item,
				R.id.tv_item, this.getResources().getStringArray(R.array.menu_item)));
		slidingMenuItemList.setOnItemClickListener(this);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SUCCESS:
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), R.string.download_success_tooltip, Toast.LENGTH_LONG)
							.show();
					tvStatus.setText(filename + " " + getResources().getString(R.string.novel_saved_tooltip));

					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri uri = Uri.fromFile(new File(downDirPath + filename));
					intent.setDataAndType(uri, "text/plain");
					String ticker = filename + " " + getString(R.string.novel_saved_tooltip);

					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
							.setContentTitle(getString(R.string.app_name)).setContentText(downDirPath + filename)
							.setTicker(ticker).setSmallIcon(android.R.drawable.stat_sys_download_done)
							.setAutoCancel(true);
					PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
					mBuilder.setContentIntent(contentIntent);
					mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(0, mBuilder.build());

					break;
				case PREPARING:
					pbDownload.setVisibility(View.GONE);
					tvDownloadStatus.setVisibility(View.GONE);
					progressDialog = ProgressDialog.show(MainActivity.this,
							getResources().getString(R.string.progress_dialog_title),
							getResources().getString(R.string.progress_dialog_msg));
					break;
				case FAIL:
					if (progressDialog.isShowing())
						progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), R.string.download_fail_tooltip, Toast.LENGTH_SHORT).show();
					tvStatus.setText(R.string.download_fail_msg);
					break;
			}
		}
	};

	@SuppressLint("HandlerLeak")
	public Handler progressHandler = new Handler() {
		int completeTaskNum;
		int totalTaskNum;

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x10000) {
				completeTaskNum = 0;
				totalTaskNum = msg.arg1;
				tvDownloadStatus.setText(0 + "/" + totalTaskNum);
				return;
			}

			completeTaskNum++;
			tvDownloadStatus.setText(completeTaskNum + "/" + totalTaskNum);
			pbDownload.setProgress((int) completeTaskNum * 100 / totalTaskNum);
		}
	};

	@Override
	protected void onResume() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		super.onResume();
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		slidingMenu.toggle(false);
		switch (position) {
			case 0: // search
				LayoutInflater factory = LayoutInflater.from(this);
				final View searchDialogView = factory.inflate(R.layout.search_dialog, null);
				final AlertDialog searchDialog = new AlertDialog.Builder(this).setTitle(R.string.search)
						.setNegativeButton(R.string.close_btn, null).setCancelable(false).create();
				searchDialog.setView(searchDialogView);

				final WebView wv = (WebView) searchDialogView.findViewById(R.id.wv_something);
				wv.getSettings().setJavaScriptEnabled(true);
				wv.loadUrl("https://googledrive.com/host/0By9mvBCbgqrycV9naFJSYm5mbjQ");
				wv.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						view.loadUrl(url);
						return true;
					}
				});
				wv.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if ((keyCode == KeyEvent.KEYCODE_BACK)) {
							if (wv.canGoBack()) {
								wv.goBack();
							} else {
								searchDialog.dismiss();
							}
							return true;
						}
						return false;
					}
				});

				searchDialog.show();
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(searchDialog.getWindow().getAttributes());
				lp.width = width;
				searchDialog.getWindow().setAttributes(lp);
				break;
			case 1: // settings
				if (API_VERSION >= 11) {
					startActivity(new Intent(this, PrefsActivity.class));
				} else {
					startActivity(new Intent(this, GingerbreadPrefsActivity.class));
				}
				break;
			case 2: // exit
				finish();
				break;
			default:
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        slidingMenu.toggle();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	private void closeKeypad() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private int getScreenWidth() {
		Display display = getWindowManager().getDefaultDisplay();
		if (API_VERSION >= 13) {
		    Point size = new Point();
		    display.getSize(size);
		    width = size.x;
		} else {
		    width = display.getWidth();
		}
		return width;
	}
}
