package com.sh1r0.noveldroid;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int SUCCESS = 0x10000;
	private static final int FAIL = 0x10001;

	private EditText etID;
	private EditText etNovelName;
	private EditText etAuthor;
	private EditText etFromPage;
	private EditText etToPage;
	private Button btnAnalyze;
	private Button btnDownload;
	private TextView tvDebug;
	private Spinner spnDomain;
	private NovelInfo novelInfo;
	private String[] domainList = { "ck101" };

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

		etID.setText("2807952"); // debug use only

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, domainList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDomain.setAdapter(adapter);
		spnDomain.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				Toast.makeText(getApplicationContext(), "Domain: " + domainList[position], Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(getApplicationContext(), "Nothing", Toast.LENGTH_SHORT).show();
			}
		});

		btnAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), R.string.tooltip_analyze, Toast.LENGTH_SHORT).show();
				String tid = etID.getText().toString();
				tvDebug.setText(tid);

				try {
					novelInfo = Analysis.analysisUrl(tid);
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

				Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
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

				new Thread(new Runnable() {
					@Override
					public void run() {
						Downloader downloader = new Downloader(novelInfo);
						BookWriter bookWriter = new BookWriter(novelInfo);
						Message msg = new Message();
						try {
							if (!downloader.startDownload(bookWriter)) { // 開始下載
								msg.what = FAIL;
								mHandler.sendMessage(msg);
							} else {
								bookWriter.makeBook();
								msg.what = SUCCESS;
								mHandler.sendMessage(msg);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FAIL:
				Toast.makeText(getApplicationContext(), "Novel Download Fail :(", Toast.LENGTH_SHORT).show();
				break;
			case SUCCESS:
				Toast.makeText(getApplicationContext(), "Novel Download Success!!", Toast.LENGTH_SHORT).show();
				tvDebug.setText("OK");
				break;
			}
		}
	};

/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
*/
}
