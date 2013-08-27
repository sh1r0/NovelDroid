package com.sh1r0.noveldroid;

import java.io.IOException;

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
		String prefix = "http://" + novelInfo.domain + "/thread-" + novelInfo.tid + "-";
		int n = 0;
		for (int i = novelInfo.fromPage; i <= novelInfo.toPage; i++) {
			urlStrings[n] = prefix + i + "-1.html";
			tempFilePaths[n] = Settings.tempDir + novelInfo.tid + "-" + i + ".html";
			n++;
		}
	}

	public boolean startDownload(BookWriter bookWriter) throws IOException {
		generateUrlList();
		int minTaskNum = urlStrings.length / Settings.threadNum; // min #tasks of each thread
		int leftTaskNum = urlStrings.length % Settings.threadNum; // totalTaskNum = minTaskNum * threadNum + leftTaskNum
		DownloadThread[] downloadThread = new DownloadThread[Settings.threadNum];
		String[] src;
		String[] dst;
		
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
			downloadThread[x] = new DownloadThread(src, dst, x);
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