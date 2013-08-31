package com.sh1r0.noveldroid;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int SUCCESS = 0x10000;
	private static final int PREPARING = 0x10001;
	private static final int FAIL = 0x10002;

	private EditText etID;
	private EditText etNovelName;
	private EditText etAuthor;
	private EditText etFromPage;
	private EditText etToPage;
	private Button btnAnalyze;
	private Button btnDownload;
	private TextView tvDebug;
	private Spinner spnDomain;
	private ProgressBar pbDownload;
	private NovelInfo novelInfo;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		etID = (EditText) findViewById(R.id.et_id);
		etNovelName = (EditText) findViewById(R.id.et_novel_name);
		etAuthor = (EditText) findViewById(R.id.et_author);
		etFromPage = (EditText) findViewById(R.id.et_from_page);
		etToPage = (EditText) findViewById(R.id.et_to_page);
		btnAnalyze = (Button) findViewById(R.id.btn_analyze);
		btnDownload = (Button) findViewById(R.id.btn_download);
		tvDebug = (TextView) findViewById(R.id.tv_debug);
		spnDomain = (Spinner) findViewById(R.id.spn_doamin);
		pbDownload = (ProgressBar) findViewById(R.id.progressbar);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Site.domainList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDomain.setAdapter(adapter);
		spnDomain.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(getApplicationContext(), "Nothing", Toast.LENGTH_SHORT).show();
			}
		});

		btnAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tvDebug.setText("");
				
				if (!isNetworkConnected()) {
					Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_SHORT).show();
					btnDownload.setEnabled(false);
					return;
				}
				
				String tid = etID.getText().toString();
				if (tid.isEmpty()) {
					Toast.makeText(getApplicationContext(), "Novel ID cannot be blank", Toast.LENGTH_SHORT).show();
					btnDownload.setEnabled(false);
					return;
				}
				
				try {
					novelInfo = Analysis.analysisUrl(spnDomain.getSelectedItemPosition(), tid);
				} catch (Exception e) {
					btnDownload.setEnabled(false);
					String err = (e.getMessage() == null) ? "analysis fail" : e.getMessage();
					Log.e("Error", err);
					return;
				}

				if (novelInfo == null || novelInfo.wrongUrl) {
					btnDownload.setEnabled(false);
					Log.e("Error", "Wrong URL");
					return;
				}

				etAuthor.setText(novelInfo.author);
				etNovelName.setText(novelInfo.name);
				etFromPage.setText("1");
				etToPage.setText(String.valueOf(novelInfo.lastPage));

				Toast.makeText(getApplicationContext(), "Analysis Done!", Toast.LENGTH_SHORT).show();
				btnDownload.setEnabled(true);
			}
		});

		btnDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				novelInfo.name = etNovelName.getText().toString();
				novelInfo.author = etAuthor.getText().toString();
				novelInfo.fromPage = Integer.parseInt(etFromPage.getText().toString());
				novelInfo.toPage = Integer.parseInt(etToPage.getText().toString());

				if (novelInfo.name.isEmpty()) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.setTitle("Error");
					dialog.setMessage("Novel name field cannot be blank");
					dialog.setCancelable(false);
					dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
					dialog.setTitle("Error");
					dialog.setMessage("Please check page fields again");
					dialog.setCancelable(false);
					dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					dialog.show();
					return;
				}
				
				File tempDir = new File(Settings.tempDir);
				if (!tempDir.exists()) {
					tempDir.mkdirs();
				}
				
				pbDownload.setProgress(0);
				pbDownload.setVisibility(View.VISIBLE);
				tvDebug.setText("Downloading...");

				new Thread(new Runnable() {
					@Override
					public void run() {
						Downloader downloader = new Downloader(novelInfo);
						BookWriter bookWriter = new BookWriter(novelInfo);
						try {
							if (!downloader.startDownload(bookWriter, progressHandler)) {
								mHandler.sendEmptyMessage(FAIL);
							} else {
								mHandler.sendEmptyMessage(PREPARING);
								bookWriter.makeBook();
								mHandler.sendEmptyMessage(SUCCESS);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
		
		// debug use only
//		etID.setText("7475179");
//		spnDomain.setSelection(Site.EYNY);
		etID.setText("2800598");
		spnDomain.setSelection(Site.CK101);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESS:
				progressDialog.dismiss();
				Toast.makeText(getApplicationContext(), "Novel Download Success!!", Toast.LENGTH_LONG).show();
				tvDebug.setText("Novel is saved");
				break;
			case PREPARING:
				progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Processing...");
				break;
			case FAIL:
				Toast.makeText(getApplicationContext(), "Novel Download Fail :(", Toast.LENGTH_SHORT).show();
				tvDebug.setText("Download failed");
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
				return;
			}
			
			completeTaskNum++;
			pbDownload.setProgress((int) completeTaskNum * 100 / totalTaskNum);
			if (completeTaskNum == totalTaskNum) {
				pbDownload.setVisibility(View.GONE);
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
			dialog.setIcon(android.R.drawable.ic_dialog_info);
			dialog.setTitle(R.string.menu_about);
			dialog.setMessage("Author: sh1r0");
			dialog.setCancelable(false);
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			dialog.show();
			break;
		case R.id.menu_quit:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
}
