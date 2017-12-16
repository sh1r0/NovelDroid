package com.sh1r0.noveldroid;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NovelUtils {
	public static final String DEFAULT_NAMING_RULE = "/n";
	public static final int MAX_THREAD_NUM = 2;
	public static final String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NovelDroid/";
	public static final String TEMP_DIR = APP_DIR + "temp/";
	private static NovelUtils novelUtils;

	private final Map<Character, Character> S2T_MAP;

	private NovelUtils() {
		S2T_MAP = new HashMap<Character, Character>();
		try {
			InputStream input = ApplicationContextProvider.getContext().getAssets().open("table_s2t.txt");
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			input.close();
			char[] S2T_TABLE = (new String(buffer)).toCharArray();
			for (int i = 0; i < S2T_TABLE.length; i += 2) {
				final Character cS = Character.valueOf(S2T_TABLE[i]);
				final Character cT = Character.valueOf(S2T_TABLE[i + 1]);
				S2T_MAP.put(cS, cT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String s2t(String input) {
		final char[] sChars = input.toCharArray();
		for (int i = 0, n = sChars.length; i < n; i++) {
			final Character tChar = S2T_MAP.get(sChars[i]);
			if (tChar != null)
				sChars[i] = tChar;
		}
		return String.valueOf(sChars);
	}

	public static NovelUtils getInstance() {
		if (novelUtils == null) {
			novelUtils = new NovelUtils();
		}
		return novelUtils;
	}

	public static String genTxtName(String novelName, String authorName, String namingRule) {
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String strDate = sdFormat.format(date);

		SimpleDateFormat yyyyFormat = new SimpleDateFormat("yyyy");
		String yyyyDate = yyyyFormat.format(date);

		SimpleDateFormat mmFormat = new SimpleDateFormat("MM");
		String mmDate = mmFormat.format(date);

		SimpleDateFormat ddFormat = new SimpleDateFormat("dd");
		String ddDate = ddFormat.format(date);

		// /n = bookname, /a = author, /t = time, /y = year, /m = month and /d = day
		String filename = namingRule.replace("/n", novelName);
		filename = filename.replace("/a", authorName);
		filename = filename.replace("/t", strDate);
		filename = filename.replace("/y", yyyyDate);
		filename = filename.replace("/m", mmDate);
		filename = filename.replace("/d", ddDate);

		filename = filename + ".txt";

		return filename;
	}

	public static OutputStreamWriter newNovelWriter(String filepath, String encoding) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filepath), encoding);
		if (encoding.equals("UTF-16LE")) { // inject BOM
			writer.write("\uFEFF");
		}

		return writer;
	}

	public static void unZip(String zipName, String target) throws IOException {
		ZipFile zf = new ZipFile(NovelUtils.TEMP_DIR + zipName);
		ZipEntry ze = zf.getEntry(target);

		InputStream inputStream = zf.getInputStream(ze);
		FileOutputStream fout = new FileOutputStream(NovelUtils.TEMP_DIR + ze.getName());

		int length = 0;
		byte[] buffer = new byte[2048];
		while ((length = inputStream.read(buffer)) > 0)
			fout.write(buffer, 0, length);

		inputStream.close();
		fout.close();
		zf.close();
	}

	public static String replace(String str, String target, String replacement) {
		StringBuilder sb = new StringBuilder(str);

		int index = sb.length();
		int lenTarget = target.length();
		while ((index = sb.lastIndexOf(target, index)) != -1) {
			sb.replace(index, index + lenTarget, replacement);
			index -= lenTarget;
		}

		return sb.toString();
	}

	public static String replace(String str, String[] targets, String[] replacements) {
		StringBuilder sb = new StringBuilder(str);

		int index, lenTarget;
		for (int i = 0; i < targets.length; i++) {
			index = sb.length();
			lenTarget = targets[i].length();
			while ((index = sb.lastIndexOf(targets[i], index)) != -1) {
				sb.replace(index, index + lenTarget, replacements[i]);
				index -= lenTarget;
			}
		}

		return sb.toString();
	}

	public static void deleteTempFiles(File tempDir) {
		File[] files = tempDir.listFiles();
		if (files != null) {
			for (File f : files) {
				f.delete();
			}
		}
	}

	/**
	 * Compares two version strings.
	 *
	 * Use this instead of String.compareTo() for a non-lexicographical
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 *
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 *
	 * @param ver1 a string of ordinal numbers separated by decimal points.
	 * @param ver2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if ver1 is _numerically_ less than ver2.
	 *         The result is a positive integer if ver1 is _numerically_ greater than ver2.
	 *         The result is zero if the strings are _numerically_ equal.
	 */
	public static Integer versionCompare(String ver1, String ver2) {
		String[] vals1 = ver1.split("\\.");
		String[] vals2 = ver2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version string
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		} else {
			// the strings are equal or one string is a substring of the other
			// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
			return Integer.signum(vals1.length - vals2.length);
		}
	}
}
