package com.sh1r0.noveldroid;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.util.Log;

public class DownloadThread extends Thread {

	private String[] srcUrlStrings;
	private String[] dstFilePaths;
	private int threadID;
	private Handler progressHandler;
	public boolean downloadstate;

	public DownloadThread(String[] src, String[] dst, int t, Handler progressHandler) {
		this.srcUrlStrings = src;
		this.dstFilePaths = dst;
		this.threadID = t;
		downloadstate = true;
		this.progressHandler = progressHandler;
	}

	public void run() {
		for (int n = 0; n < this.dstFilePaths.length; n++) {
			StringBuffer total = new StringBuffer();

			try {
				Log.d("Debug", "開始下載: " + srcUrlStrings[n]);
				URL url = new URL(srcUrlStrings[n]);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setDoOutput(true);
				switch (threadID) {
				case 0:
					connection
							.setRequestProperty(
									"User-Agent",
									"Mozilla/5.0 (Linux; U; Android 4.0.3; zh-tw; HTC_Sensation_Z710e Build/IML74K)AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
					break;
				case 1:
					connection
							.setRequestProperty("User-Agent",
									"Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; SAMSUNG; OMNIA7)　");
					break;
				case 2:
					connection
							.setRequestProperty(
									"User-Agent",
									"Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A5313e Safari/7534.48.3");
					break;
				case 3:
					connection
							.setRequestProperty(
									"User-Agent",
									"Mozilla/5.0 (Linux; Android 4.2.2; Nexus 7 Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166  Safari/535.19");
					break;
				default:
					connection
							.setRequestProperty(
									"User-Agent",
									"Mozilla/5.0 (Linux; Android 4.2.2; Nexus 7 Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166  Safari/535.19");
					break;
				}

				connection.connect();
				InputStream inStream = (InputStream) connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf8"));
				String line = "";
				while ((line = reader.readLine()) != null) {
					total.append(line + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Debug", "取得網頁html時發生錯誤");
				downloadstate = false;
				return;
			}
			try {
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dstFilePaths[n]), "UTF-8");
				writer.write(total.toString());
				writer.flush();
				writer.close();
				Log.d("Debug", "下載完成: " + dstFilePaths[n]);
				progressHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				downloadstate = false;
				return;
			}
		}
	}
}
