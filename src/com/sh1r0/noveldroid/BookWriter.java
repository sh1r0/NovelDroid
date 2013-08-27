package com.sh1r0.noveldroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.util.Log;

public class BookWriter {
	private String[][] fileName;
	public String bookName;
	public String author;
	private OutputStreamWriter writer;

	public BookWriter(NovelInfo novelInfo) {
		fileName = new String[Settings.threadNum][];
		this.bookName = novelInfo.name;
		this.author = novelInfo.author;
	}

	public void makeBook() throws IOException {
		writer = new OutputStreamWriter(new FileOutputStream(Settings.appDir + bookName + ".txt"), "UTF-8");
/*
		writer.write("書名：" + bookName + "\r\n");
		if (!author.isEmpty())
			writer.write("作者：" + author + "\r\n");
*/
		ContentParserThread[] parserThreads = new ContentParserThread[Settings.threadNum];
		for (int n = 0; n < Settings.threadNum; n++) {
			parserThreads[n] = new ContentParserThread(fileName[n]);
			parserThreads[n].start();
		}
		
		// wait for termination of all thread
		try {
			for (int n = 0; n < Settings.threadNum; n++) {
				parserThreads[n].join();
			}
		} catch (InterruptedException e) {
		}
		
		for (int n = 0; n < Settings.threadNum; n++) {
			writer.write(parserThreads[n].getResult());
		}
		
		writer.flush();
		writer.close();
		
		Log.d("Debug", "小說製作完成");
		delTempFile();
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
		for (int n = 0; n < Settings.threadNum; n++) {
			for (int m = 0; m < fileName[n].length; m++) {
				temp = new File(fileName[n][m]);
				if (temp.exists())
					temp.delete();
			}
		}
		Log.d("Debug", "刪除完畢");
	}
}
