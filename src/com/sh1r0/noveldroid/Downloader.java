package com.sh1r0.noveldroid;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;

public class Downloader {
	private String[] urlStrings;
	private String[] tempFilePaths;
	private NovelInfo novelInfo;

	public Downloader(NovelInfo novelInfo) {
		this.novelInfo = novelInfo;
	}

	private void generateUrlList() {
		int totalPageNum = novelInfo.toPage - novelInfo.fromPage + 1;
		urlStrings = new String[totalPageNum];
		tempFilePaths = new String[totalPageNum];
		String prefix = "";
		String postfix = "";
		if (novelInfo.domainID == Site.CK101) {
			// http://ck101.com/thread-<tid>-<page>-1.html
			prefix = "http://ck101.com/thread-" + novelInfo.tid + "-";
			postfix = "-1.html";
		} else if (novelInfo.domainID == Site.EYNY) {
			// http://www.eyny.com/archiver/tid-<tid>-<page>.html
			prefix = "http://www.eyny.com/archiver/tid-" + novelInfo.tid + "-";
			postfix = ".html";
		}
		
		int n = 0;
		for (int i = novelInfo.fromPage; i <= novelInfo.toPage; i++) {
			urlStrings[n] = prefix + i + postfix;
			tempFilePaths[n] = Settings.tempDir + novelInfo.tid + "-" + i + ".html";
			n++;
		}
	}

	public boolean startDownload(BookWriter bookWriter, Handler progressHandler) throws IOException {
		generateUrlList();
		int minTaskNum = urlStrings.length / Settings.threadNum; // min #tasks of each thread
		int leftTaskNum = urlStrings.length % Settings.threadNum; // totalTaskNum = minTaskNum * threadNum + leftTaskNum
		DownloadThread[] downloadThread = new DownloadThread[Settings.threadNum];
		String[] src;
		String[] dst;
		
		Message msg = new Message();
		msg.what = 0x10000;
		msg.arg1 = novelInfo.toPage - novelInfo.fromPage + 1;
		progressHandler.sendMessage(msg);
		
		// assign job to each DownloadThread
		int m = 0;
		int taskNum;
		for (int x = 0; x < Settings.threadNum; x++) {
			if (leftTaskNum > 0) {
				taskNum = minTaskNum + 1;
				leftTaskNum--;
			} else {
				taskNum = minTaskNum;
			}
			src = new String[taskNum];
			dst = new String[taskNum];
			for (int y = 0; y < taskNum; y++) {
				src[y] = urlStrings[m];
				dst[y] = tempFilePaths[m++];
			}
			downloadThread[x] = new DownloadThread(src, dst, x, progressHandler);
			bookWriter.addFileName(x, dst);
			downloadThread[x].start(); // start job
		}
		
		// wait for termination of each DownloadThread
		try {
			for (int x = 0; x < Settings.threadNum; x++) {
				downloadThread[x].join();
			}
		} catch (InterruptedException e) {
		}
		
		for (int x = 0; x < Settings.threadNum; x++) {
			if (!downloadThread[x].downloadstate)
				return false;
		}
		
		return true;
	}
}