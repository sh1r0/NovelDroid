package com.sh1r0.noveldroid;

import com.sh1r0.noveldroid.downloader.*;

public class DownloaderFactory {
	public static AbstractDownloader getDownloader(int siteID) {
		switch (siteID) {
			case 0:
				return Ck101Downloader.getInstance();
			case 1:
				return EynyDownloader.getInstance();
			case 2:
				return QidianDownloader.getInstance();
			case 3:
				return ZonghengDownloader.getInstance();
			case 4:
				return _17kDownloader.getInstance();
			case 5:
				return ChuangshiDownloader.getInstance();
			default:
				return null;
		}
	}
}
