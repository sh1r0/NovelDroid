package com.sh1r0.noveldroid.downloader;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.sh1r0.noveldroid.Novel;
import com.sh1r0.noveldroid.NovelUtils;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class _17kDownloader extends AbstractDownloader {
	private static final int SITE_ID = 4;
	private static _17kDownloader downloader;

	private Novel novel;
	private NovelUtils novelUtils;

	private _17kDownloader() {
		novel = new Novel();
		novelUtils = NovelUtils.getInstance();
	}

	public static _17kDownloader getInstance() {
		if (downloader == null) {
			downloader = new _17kDownloader();
		}
		return downloader;
	}

	@Override
	public Novel analyze(String bookID) throws Exception {
		novel.siteID = SITE_ID;
		novel.name = "";
		novel.author = "";
		novel.bookID = bookID;
		novel.fromPage = 1;
		novel.toPage = 1;
		novel.author = "";

		AsyncTask<Novel, Integer, Novel> request = new Analyzer();
		request.execute(novel);
		novel = request.get();

		return novel;
	}

	@Override
	public void download(Handler progressHandler) throws Exception {
		Downloader downloader = new Downloader(progressHandler);
		downloader.execute(novel.bookID + "_txt.zip");
		if (!downloader.get())
			throw new Exception();
	}

	@Override
	public String process(String downDirPath, String namingRule, String encoding) {
		File downDir = new File(downDirPath);
		if (!downDir.exists()) {
			downDir.mkdirs();
		}

		String outputFileName = NovelUtils.genTxtName(novel.name, novel.author, namingRule);

		BufferedReader reader = null;
		OutputStreamWriter writer = null;
		try {
			NovelUtils.unZip(novel.bookID + "_txt.zip", novel.bookID + ".txt");

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				NovelUtils.TEMP_DIR + novel.bookID + ".txt"), "UTF-8"));
			writer = NovelUtils.newNovelWriter(downDirPath + outputFileName, encoding);

			NovelUtils novelUtils = NovelUtils.getInstance();
			String line;
			reader.readLine();
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("  本书首发17K小说网")) {
					continue;
				}
				if (!line.startsWith("\r\n")) {
					line = novelUtils.s2t(line);
					line += "\r\n";
				}
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
				URL url = new URL("http://www.17k.com/book/" + novel.bookID + ".html");
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
					"utf8"));

				String line = "";
				String regex = "(\\S+)最新章节\\((\\S+)\\),";
				Pattern p = Pattern.compile(regex);
				Matcher matcher;
				int start;
				while ((line = reader.readLine()) != null) {
					if ((start = line.indexOf("<title>")) >= 0) {
						start += 7;
						line = line.substring(start);
						matcher = p.matcher(line);
						if (matcher.find()) {
							novel.name = novelUtils.s2t(matcher.group(1));
							novel.author = novelUtils.s2t(matcher.group(2));
						}
						break;
					}
				}
				reader.close();
			} catch (IOException e) {
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
				URL url = new URL("http://www.17k.com/html/books/0/" + filenames[0].substring(0, 2)
					+ "/" + filenames[0].substring(0, 4) + "/" + filenames[0]);
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
