package com.sh1r0.noveldroid;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText etURL;
	private EditText etNovelName;
	private EditText etAuthor;
	private EditText etFromPage;
	private EditText etToPage;
	private Button btnAnalyze;
	private TextView tvDebug;
	private NovelInfo novelInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		etURL = (EditText) findViewById(R.id.et_url);
		etNovelName = (EditText) findViewById(R.id.et_novel_name);
		etAuthor = (EditText) findViewById(R.id.et_author);
		etFromPage = (EditText) findViewById(R.id.et_from_page);
		etToPage = (EditText) findViewById(R.id.et_to_page);
		btnAnalyze = (Button) findViewById(R.id.btn_analyze);
		tvDebug = (TextView) findViewById(R.id.tv_debug);
		
//		etURL.setText("http://ck101.com/thread-1517656-1-1.html");
//		etURL.setText("http://ck101.com/thread-2806042-1-1.html");
		etURL.setText("http://ck101.com/thread-2805147-1-1.html");

		btnAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), R.string.tooltip_analyze, Toast.LENGTH_SHORT).show();
				String url = etURL.getText().toString();
				tvDebug.setText(url);
				
				try {
					novelInfo = Analysis.analysisUrl(url);
				} catch (Exception e) {
					Log.e("Error", e.getMessage());
				}
				
				if (novelInfo == null || novelInfo.wrongUrl) {
					Log.e("Error", "Wrong URL");
					return;
				}
				
				etAuthor.setText(novelInfo.author);
				etNovelName.setText(novelInfo.name);
				etFromPage.setText("1");
				etToPage.setText(novelInfo.lastPage);

//				HttpClient client = new DefaultHttpClient();
//				HttpGet request = new HttpGet(url);
//				String html = "";
//				try {
//					HttpResponse response = client.execute(request);
//					html = EntityUtils.toString(response.getEntity());
//					writeFileSdcardFile("test.html", html);
//					Log.d("Debug", "Create folder success");
//				} catch (Exception e) {
//					Log.e("Error", e.getMessage());
//					Log.e("Error", "Create folder fail");
//				}
				
				Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 */
	public void writeFileSdcardFile(String fileName, String write_str) throws IOException {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName), "UTF8"));
			out.write(write_str);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
