package com.sh1r0.noveldroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Analysis {
	public static NovelInfo analysisUrl(String urlString) throws Exception {
		NovelInfo result = new NovelInfo();
		Log.d("Debug", urlString);
		result.urlString = urlString;
		
		// http://ck101.com/thread-<tid>-<page>-1.html
		String regex = "^http://\\w*.*ck101.com/thread-(\\d+)-(\\d+)-\\w+.html";
		Pattern p = Pattern.compile(regex);
		Matcher macher = p.matcher(urlString);
		if (macher.find()) {
			result.domain = "ck101.com";
			result.tid = macher.group(1);
			result.currentPage = Integer.parseInt(macher.group(2));
		} else {
			Log.e("Error", "Wrong URL!!");
			result.wrongUrl = true;
			return result;
		}
		
		URL url = new URL(urlString);
		boolean infoFound = false;
		boolean pageFound = false;

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");
		connection.connect();
		InputStream inStream = (InputStream) connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf8"));
		String line = "";
		result.lastPage = "1";
		
		while (!(infoFound && pageFound) && (line = reader.readLine()) != null) {
			int start;
			if ((start = line.indexOf("<title>")) >= 0) {
				start += 7;
				int end = line.indexOf("</title>");
				line = line.substring(start, end);
				
				regex = "([\\[【「].+[\\]】」])?\\s*(\\S+).*作者[】:： ]*([\\S&&[^(（《﹝]]+)";
				p = Pattern.compile(regex);
				macher = p.matcher(line);
				if (macher.find()) {
					result.name = macher.group(2);
					result.author = macher.group(3);
					infoFound = true;
				}
			}
			if (line.indexOf("<div class=\"pg\">") >= 0) {
				// "<a href=\"thread-<tid>-<page>-1.html\" class=\"last\">";
				regex = "<a href=\"thread-\\d+-(\\d+)-\\w+.html\"";
				p = Pattern.compile(regex);
				macher = p.matcher(line);
				String tempPage = "1";
				// get the second last page link
				while (macher.find()) {
					result.lastPage = tempPage;
					tempPage = macher.group(1);
				}
				pageFound = true;
			}
		}

		reader.close();

		return result;
	}
}
