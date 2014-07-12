package com.sh1r0.noveldroid.downloader;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.sh1r0.noveldroid.Novel;
import com.sh1r0.noveldroid.NovelUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

public class QidianDownloader extends AbstractDownloader {
	private static QidianDownloader downloader;

	private Novel novel;

	private QidianDownloader() {
		novel = new Novel();
	}

	public static QidianDownloader getInstance() {
		if (downloader == null) {
			downloader = new QidianDownloader();
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
				NovelUtils.TEMP_DIR + novel.id + ".txt"), "GBK"));
			writer = NovelUtils.newNovelWriter(downDirPath + outputFileName, encoding);

			NovelUtils novelUtils = NovelUtils.getInstance();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("　　<a href=http://")) {
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
				Document doc = Jsoup.connect("http://m.qidian.com.tw/books/" + novel.id).get();
				Element description = doc.select("meta[name=description]").first();

				String regex = "^(\\S+)\\((\\S+)\\):";
				Pattern p = Pattern.compile(regex);
				Matcher matcher = p.matcher(description.attr("content"));
				if (matcher.find()) {
					novel.name = matcher.group(1);
					novel.author = matcher.group(2);
				}
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
				URL url = new URL("http://download.qidian.com/pda/" + filenames[0]);
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
