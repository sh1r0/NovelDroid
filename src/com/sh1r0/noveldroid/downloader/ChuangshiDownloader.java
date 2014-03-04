package com.sh1r0.noveldroid.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.sh1r0.noveldroid.Novel;
import com.sh1r0.noveldroid.NovelUtils;

public class ChuangshiDownloader extends AbstractDownloader {
	private static ChuangshiDownloader downloader;

	private Novel novel;
	private NovelUtils novelUtils;

	private ChuangshiDownloader() {
		novel = new Novel();
		novelUtils = NovelUtils.getInstance();
	}

	public static ChuangshiDownloader getInstance() {
		if (downloader == null) {
			downloader = new ChuangshiDownloader();
		}
		return downloader;
	}

	@Override
	public Novel analyze(String id) throws Exception {
		novel.name = "";
		novel.author = "";
		novel.id = id;
		novel.fromPage = 1;
		novel.toPage = 1;

		AsyncTask<Novel, Integer, Novel> request = new Analyzer();
		request.execute(novel);
		novel = request.get();

		return novel;
	}

	@Override
	public void download(Handler progressHandler) throws Exception {
		Downloader downloader = new Downloader(progressHandler);
		downloader.execute(novel.id + ".txt");
		if (!downloader.get())
			throw new Exception();
	}

	@Override
	public String process(String downDirPath, int namingRule, String encoding) {
		File downDir = new File(downDirPath);
		if (!downDir.exists()) {
			downDir.mkdirs();
		}

		String outputFileName = NovelUtils.genTxtName(novel.name, novel.author, namingRule);

		BufferedReader reader = null;
		OutputStreamWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					NovelUtils.TEMP_DIR + novel.id + ".txt"), "UTF-8"));
			writer = NovelUtils.newNovelWriter(downDirPath + outputFileName, encoding);

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("    <a href=http://")) {
					continue;
				}
				line = novelUtils.s2t(line);
				line += "\r\n";
				writer.write(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				reader.close();
				writer.flush();
				writer.close();
			} catch (Exception e) {
			}

			NovelUtils.deleteTempFiles(new File(NovelUtils.TEMP_DIR));
		}

		return outputFileName;
	}

	private class Analyzer extends AsyncTask<Novel, Integer, Novel> {
		@Override
		protected Novel doInBackground(Novel... novels) {
			Novel novel = novels[0];

			try {
				URL url = new URL("http://chuangshi.qq.com/www/txt/txt1/"
						+ novel.id.substring(4, 5) + "/" + novel.id.substring(3, 5) + "/"
						+ novel.id.substring(0, 5) + "/" + novel.id + ".txt");

				String line;
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
						"utf8"), 1024);
				if ((line = reader.readLine()) != null) {
					novel.name = novelUtils.s2t(line);
				}
				if ((line = reader.readLine()) != null) {
					novel.author = novelUtils.s2t(line.substring(7));
				}
				reader.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return novel;
		}
	}

	private class Downloader extends AsyncTask<String, Integer, Boolean> {
		private Handler progressHandler;

		public Downloader(Handler progressHandler) {
			this.progressHandler = progressHandler;
		}

		@Override
		protected void onProgressUpdate(Integer... progresses) {
			progressHandler.sendEmptyMessage(progresses[0]);
		}

		@Override
		protected Boolean doInBackground(String... filenames) {
			try {
				URL url = new URL("http://chuangshi.qq.com/www/txt/txt1/"
						+ filenames[0].substring(4, 5) + "/" + filenames[0].substring(3, 5) + "/"
						+ filenames[0].substring(0, 5) + "/" + filenames[0]);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				int fileLength = connection.getContentLength();
				Message msg = new Message();
				msg.what = -1;
				msg.arg1 = fileLength;
				progressHandler.sendMessage(msg);

				InputStream input = connection.getInputStream();
				FileOutputStream output = new FileOutputStream(NovelUtils.TEMP_DIR + filenames[0]);
				byte data[] = new byte[4096];
				int count;
				while ((count = input.read(data)) != -1) {
					output.write(data, 0, count);
					publishProgress(count);
				}
				output.close();
			} catch (Exception e) {
				return false;
			}

			return true;
		}
	}
}
