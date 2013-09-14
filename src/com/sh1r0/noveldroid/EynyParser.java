package com.sh1r0.noveldroid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class EynyParser extends AbstractParser {
	@Override
	protected String doInBackground(String... htmls) {
		StringBuilder bookData = new StringBuilder();
		BufferedReader reader = null;
		String line;
		Pattern pHtmlTag = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
		Pattern pTitle = Pattern.compile("<h3>(.+)?</h3>");
		Pattern pModStamp = Pattern.compile(" 本帖最後由 \\S+ 於 \\d{4}-\\d{1,2}-\\d{1,2} \\d{2}:\\d{2} (\\S{2} )?編輯 ");
		Matcher matcher;
		
		/**
		 * 0: not in article
		 * 1: in author section
		 * 2: in title section
		 * 3: in article section
		 */
		int stage;
		int end;
		for (int n = 0; n < htmls.length; n++) {
			stage = 0;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(htmls[n]), "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				return null;
			} 
			Log.d("Debug", "處理中: " + htmls[n]);
			try {
				while ((line = reader.readLine()) != null) {
					switch (stage) {
					case 0:
						if (line.indexOf("<p class=\"author\">") >= 0) {
							stage = 1;
						}
						break;
					case 1:
						if (line.indexOf("</p>") >= 0) {
							stage = 2;
						}
						break;
					case 2:
						matcher = pTitle.matcher(line);
						if (matcher.find()) {
							if ((line = matcher.group(1)) != null)
								bookData.append(line + "\r\n");
							stage = 3;
						}
						break;
					case 3:
						matcher = pModStamp.matcher(line);
						if (matcher.find())
							break;
						if (line.indexOf("<p class=\"author\">") >= 0) {
							stage = 1;
							break;
						}
						if ((end = line.indexOf("...&lt;div class='locked'")) == 0) {
							stage = 0;
							break;
						}
						if (end > 0) {
							line = line.substring(0, end-1);
							stage = 0;
						}
						line = Replace.replace(line, "&nbsp;", "");
						line = Replace.replace(line, "<br/>", "\r\n");
						line = Replace.replace(line, "<br />", "\r\n");
						matcher = pHtmlTag.matcher(line);
						line = matcher.replaceAll("");
						line = line.replaceAll("^[ \t　]+", "");
						if (line.length() > 2)
							line = "　　" + line;
						bookData.append(line);
						break;
					default:
						break;
					}
				}
			} catch (IOException e1) {
				return null;
			}
			bookData.append("\r\n");
			try {
				reader.close();
			} catch (IOException e) {
				return null;
			}
		}
		
		return bookData.toString();
	}
}
