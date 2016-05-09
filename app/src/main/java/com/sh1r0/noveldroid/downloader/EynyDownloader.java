package com.sh1r0.noveldroid.downloader;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.sh1r0.noveldroid.Novel;
import com.sh1r0.noveldroid.NovelUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EynyDownloader extends AbstractDownloader {
	private static final int SITE_ID = 1;
	private static final String ARCHIVER_PREFIX = "http://www.eyny.com/archiver/tid-";
	private static final String THREAD_PREFIX = "http://www.eyny.com/thread-";
	private static EynyDownloader downloader;

	private Novel novel;
	private int totalTaskNum;
	private int threadNum;
	private String[][] assignedFiles;

	private EynyDownloader() {
	}

	public static EynyDownloader getInstance() {
		if (downloader == null) {
			downloader = new EynyDownloader();
		}
		return downloader;
	}

	@Override
	public Novel analyze(String bookID) throws Exception {
		novel = new Novel();
		novel.siteID = SITE_ID;
		novel.bookID = bookID;
		AsyncTask<Novel, Integer, Novel> request = new Analyzer();
		request.execute(novel);
		novel = request.get();
		novel.toPage = novel.lastPage;

		return novel;
	}

	private void prepare() {
		this.totalTaskNum = novel.toPage - novel.fromPage + 1;
		int minTaskNum = totalTaskNum / NovelUtils.MAX_THREAD_NUM; // min #tasks of each thread
		int leftTaskNum = totalTaskNum % NovelUtils.MAX_THREAD_NUM;
		this.threadNum = Math.min(totalTaskNum, NovelUtils.MAX_THREAD_NUM);
		String[] filenames = new String[totalTaskNum];

		// http://www.eyny.com/archiver/tid-<tid>-<page>.html
		for (int i = novel.fromPage, n = 0; i <= novel.toPage; i++) {
			filenames[n] = novel.bookID + "-" + i + ".html";
			n++;
		}

		assignedFiles = new String[threadNum][];
		for (int i = 0, s = 0, taskNum; i < threadNum; i++) {
			taskNum = minTaskNum;
			if (leftTaskNum > 0) {
				taskNum++;
				leftTaskNum--;
			}
			assignedFiles[i] = Arrays.copyOfRange(filenames, s, s + taskNum);
			s += taskNum;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void download(Handler progressHandler) throws Exception {
		prepare();

		Downloader[] downloaders = new Downloader[threadNum];
		Message msg = new Message();
		msg.what = -1;
		msg.arg1 = totalTaskNum;
		progressHandler.sendMessage(msg);

		if (Build.VERSION.SDK_INT >= 11) {
			for (int i = 0; i < threadNum; i++) {
				downloaders[i] = new Downloader(i, progressHandler);
				downloaders[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, assignedFiles[i]);
			}
		} else {
			for (int i = 0; i < threadNum; i++) {
				downloaders[i] = new Downloader(i, progressHandler);
				downloaders[i].execute(assignedFiles[i]);
			}
		}

		for (int i = 0; i < threadNum; i++) {
			if (!downloaders[i].get()) {
				throw new Exception();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public String process(String downDirPath, int namingRule, String encoding) {
		File downDir = new File(downDirPath);
		if (!downDir.exists()) {
			downDir.mkdirs();
		}

		String filename = NovelUtils.genTxtName(novel.name, novel.author, namingRule);

		OutputStreamWriter writer = null;
		try {
			writer = NovelUtils.newNovelWriter(downDirPath + filename, encoding);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Parser[] parsers = new Parser[threadNum];

		if (Build.VERSION.SDK_INT >= 11) {
			for (int i = 0; i < threadNum; i++) {
				try {
					parsers[i] = new Parser();
				} catch (Exception e) {
					e.printStackTrace();
				}
				parsers[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, assignedFiles[i]);
			}
		} else {
			for (int i = 0; i < threadNum; i++) {
				try {
					parsers[i] = new Parser();
				} catch (Exception e) {
					e.printStackTrace();
				}
				parsers[i].execute(assignedFiles[i]);
			}
		}

		try {
			for (int i = 0; i < threadNum; i++) {
				writer.write(parsers[i].get());
			}
		} catch (Exception e) {
			return null;
		} finally {
			try {
				writer.flush();
				writer.close();
			} catch (Exception e) {
			}

			NovelUtils.deleteTempFiles(new File(NovelUtils.TEMP_DIR));
		}

		return filename;
	}

	private class Analyzer extends AsyncTask<Novel, Integer, Novel> {
		@Override
		protected Novel doInBackground(Novel... novels) {
			Novel novel = novels[0];

			try {
				Document doc = Jsoup.connect(THREAD_PREFIX + novel.bookID + "-1-1.html").get();

				String title = doc.title();
				String regex = "(\\S+)\\s*-\\s*【(\\S+)】";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(title);
				if (matcher.find()) {
					novel.name = matcher.group(2);
					novel.author = matcher.group(1);
				}

				Element pg = doc.select("div.pg").first(); // if the novel has only 1 page, pg is null
				if (pg != null) {
					Elements pageLinks = pg.select("a[href]");
					Element nextPage = pageLinks.select(".nxt").first();
					Element lastPage = nextPage.previousElementSibling();
					if (lastPage.hasClass("last")) {
						regex = "([1-9][0-9]*)$";
						pattern = Pattern.compile(regex);
						matcher = pattern.matcher(lastPage.text());
						if (matcher.find()) {
							novel.lastPage = Integer.parseInt(matcher.group(1));
						}
					} else {
						novel.lastPage = Integer.parseInt(lastPage.text());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return novel;
		}
	}

	private class Downloader extends AsyncTask<String, Integer, Boolean> {
		private int tid;
		private Handler progressHandler;

		public Downloader(int tid, Handler progressHandler) {
			this.tid = tid;
			this.progressHandler = progressHandler;
		}

		@Override
		protected void onProgressUpdate(Integer... progresses) {
			progressHandler.sendEmptyMessage(progresses[0]);
		}

		@Override
		protected Boolean doInBackground(String... filenames) {
			for (int i = 0; i < filenames.length; i++) {
				StringBuffer html = new StringBuffer();
				String urlString = ARCHIVER_PREFIX + filenames[i];
				String tempFilePath = NovelUtils.TEMP_DIR + filenames[i];

				try {
					URL url = new URL(urlString);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();

					connection.setDoOutput(true);
					connection.setRequestProperty("User-Agent", MOBILE_USER_AGENTS[tid
							% MOBILE_USER_AGENTS.length]);
					connection.connect();

					InputStream inStream = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inStream,
							"utf8"));
					String line = "";
					while ((line = reader.readLine()) != null) {
						html.append(line);
						html.append("\n");
					}
				} catch (Exception e) {
					return false;
				}
				try {
					OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(
							tempFilePath), "UTF-8");
					writer.write(html.toString());
					writer.flush();
					writer.close();
				} catch (Exception e) {
					return false;
				}
				publishProgress(1);
			}

			return true;
		}
	}

	/**
	 * Since archiver html is not in a good strcture,
	 * stick on regarding html as text file to parse
	 */
	private class Parser extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... filenames) {
			StringBuilder bookData = new StringBuilder();
			BufferedReader reader = null;
			String line;
			Pattern pHtmlTag = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
			Pattern pTitle = Pattern.compile("<h3>(.+)?</h3>");
			Pattern pModStamp = Pattern
					.compile(" 本帖最後由 \\S+ 於 \\d{4}-\\d{1,2}-\\d{1,2} \\d{2}:\\d{2} (\\S{2} )?編輯 ");
			Matcher matcher;
			String[] targets = {"&nbsp;", "<br/>", "<br />"};
			String[] replacements = {"", "\r\n", "\r\n"};

			/**
			 * 0: not in article
			 * 1: in author section
			 * 2: in title section
			 * 3: in article section
			 */
			int stage;
			int end;
			for (int i = 0; i < filenames.length; i++) {
				String tempFilePath = NovelUtils.TEMP_DIR + filenames[i];
				stage = 0;
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(
							tempFilePath), "UTF-8"));
					while ((line = reader.readLine()) != null) {
						switch (stage) {
							case 0:
								if (line.contains("<p class=\"author\">")) {
									stage = 1;
								}
								break;
							case 1:
								if (line.contains("</p>")) {
									stage = 2;
								}
								break;
							case 2:
								matcher = pTitle.matcher(line);
								if (matcher.find()) {
									if ((line = matcher.group(1)) != null) {
										bookData.append(line);
										bookData.append("\r\n");
									}
									stage = 3;
								}
								break;
							case 3:
								matcher = pModStamp.matcher(line);
								if (matcher.find())
									break;
								if (line.contains("<p class=\"author\">")) {
									stage = 1;
									break;
								}
								if ((end = line.indexOf("...&lt;div class='locked'")) == 0) {
									stage = 0;
									break;
								} else if (end > 0) {
									line = line.substring(0, end - 1);
									stage = 0;
								}
								line = NovelUtils.replace(line, targets, replacements);
								matcher = pHtmlTag.matcher(line);
								line = matcher.replaceAll("");
								line = line.replaceAll("^[ \t　]+", "");
								if (!line.startsWith("\r\n")) {
									bookData.append("　　");
								}
								bookData.append(line);
								break;
							default:
								break;
						}
					}

					bookData.append("\r\n");
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}

			return bookData.toString();
		}
	}
}
