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
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ck101Downloader extends AbstractDownloader {
	private static final int SITE_ID = 0;
	private static final String THREAD_PREFIX = "http://ck101.com/thread-";
	private static Ck101Downloader downloader;

	private Novel novel;
	private int totalTaskNum;
	private int threadNum;
	private String[][] assignedFiles;

	private Ck101Downloader() {
	}

	public static Ck101Downloader getInstance() {
		if (downloader == null) {
			downloader = new Ck101Downloader();
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

		// http://ck101.com/thread-<tid>-<page>-1.html
		for (int i = novel.fromPage, n = 0; i <= novel.toPage; i++) {
			filenames[n] = novel.bookID + "-" + i + "-1.html";
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
				String regex = "([\\[【「（《［].+[\\]】」）》］])?\\s*[【《\\[]?\\s*([\\S&&[^】》]]+).*作者[】:：︰ ]*([\\S&&[^(（《﹝【]]+)";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(title);
				if (matcher.find()) {
					novel.name = matcher.group(2);
					novel.author = matcher.group(3);
				}

				Element pg = doc.select("div.pg").first(); // if the novel has only 1 page, pg is null
				if (pg != null) {
					Elements pageLinks = pg.select("a[href]");
					Element lastPage = pageLinks.get(pageLinks.size() - 2); // the 2nd last link
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
				String urlString = THREAD_PREFIX + filenames[i];
				String tempFilePath = NovelUtils.TEMP_DIR + filenames[i];

				try {
					URL url = new URL(urlString);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();

					connection.setDoOutput(true);
					connection.setRequestProperty("User-Agent", MOBILE_USER_AGENTS[tid
							% MOBILE_USER_AGENTS.length]);
					connection.connect();

					InputStream inStream = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf8"));
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

	private class Parser extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... filenames) {
			StringBuilder html = new StringBuilder();
			StringBuilder bookContent = new StringBuilder();
			BufferedReader reader;
			String line;

			for (int i = 0; i < filenames.length; i++) {
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(
							new File(NovelUtils.TEMP_DIR, filenames[i])), "UTF-8"));
					html.setLength(0);
					while ((line = reader.readLine()) != null) {
						html.append(line); // discard line endings
					}
					reader.close();

					Document doc = Jsoup.parse(html.toString());
					Elements posts = doc.select("div.postmessage");

					for (Element post : posts) {
						List<Element> elements = post.children();
						for (int j = 0; j < elements.size(); j++) {
							final Element element = elements.get(j);
							if (element.nodeName().equals("img") || element.hasClass("pstatus") || element.hasClass("quote")) {
								element.remove();
							} else if (element.nodeName().equals("br")) {
								element.replaceWith(TextNode.createFromEncoded("\r\n", ""));
							} else {
								unwrapNodeAndDescendentNodes(element);
							}
						}

						for (TextNode textNode : post.textNodes()) {
							bookContent.append(textNode.getWholeText());
						}
					}

					bookContent.append("\r\n"); // end of page
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}

			return bookContent.toString();
		}

		private void unwrapNodeAndDescendentNodes(Node node) {
			if (node.nodeName().equals("#text")) {
				return;
			}

			for (int i = node.childNodeSize() - 1; i >= 0; i--) {
				final Node child = node.childNode(i);
				unwrapNodeAndDescendentNodes(child);
			}

			node.unwrap();
		}
	}
}
