package com.sh1r0.noveldroid;

import android.os.Environment;

public final class Settings {
	public static final int threadNum = 4;
	public static final String appDir = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/NovelDroid/";
	public static final String tempDir = appDir + "temp/";
	
}
