package com.sh1r0.noveldroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.os.AsyncTask;
import android.util.Log;

public class BookWriter {
	private String[][] fileName;
	private int domainID;
	public String bookName;
	public String author;
	private OutputStreamWriter writer;

	public BookWriter(NovelInfo novelInfo) {
		fileName = new String[Config.threadNum][];
		this.domainID = novelInfo.domainID;
		this.bookName = novelInfo.name;
		this.author = novelInfo.author;
	}

	public String makeBook(String encoding, int namingRule) throws IOException {
		String filename = "";
		switch (namingRule) {
			case 0:
				filename = bookName + ".txt";
				break;
			case 1:
				filename = bookName + "_" + author + ".txt";
				break;
			case 2:
				filename = author + "_" + bookName + ".txt";
				break;
			default:
				filename = bookName + ".txt";
				break;
		}
		
		writer = new OutputStreamWriter(new FileOutputStream(Config.appDir + filename), encoding);

		if (domainID == Site.CK101) {
			AsyncTask<String, Integer, String>[] contentParsers = new Ck101Parser[Config.threadNum];
			for (int i = 0; i < Config.threadNum; i++) {
				contentParsers[i] = new Ck101Parser();
				contentParsers[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileName[i]);
			}
			try {
				for (int i = 0; i < Config.threadNum; i++) {
					writer.write(contentParsers[i].get());
				}
			} catch (Exception e) {
				return null;
			}
		} else if (domainID == Site.EYNY) {
			AsyncTask<String, Integer, String>[] contentParsers = new EynyParser[Config.threadNum];
			for (int i = 0; i < Config.threadNum; i++) {
				contentParsers[i] = new EynyParser();
				contentParsers[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileName[i]);
			}
			try {
				for (int i = 0; i < Config.threadNum; i++) {
					writer.write(contentParsers[i].get());
				}
			} catch (Exception e) {
				return null;
			}
		}

		writer.flush();
		writer.close();
		
		Log.d("Debug", "小說製作完成");
		delTempFile();
		
		return filename;
	}

	public void addFileName(int threadID, String[] temp) {
		fileName[threadID] = temp;
	}

	public void setBookName(String data) {
		bookName = data;
	}

	public void delTempFile() {
		Log.d("Debug", "刪除暫存檔中..");
		File temp;
		for (int n = 0; n < Config.threadNum; n++) {
			for (int m = 0; m < fileName[n].length; m++) {
				temp = new File(fileName[n][m]);
				if (temp.exists())
					temp.delete();
			}
		}
		Log.d("Debug", "刪除完畢");
	}
}
