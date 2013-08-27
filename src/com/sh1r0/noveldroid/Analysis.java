package com.sh1r0.noveldroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.util.Log;

public class Analysis {
	public static NovelInfo analysisUrl(String tid) throws Exception {
		NovelInfo result = new NovelInfo();
		Log.d("Debug", tid);
		result.domain = "ck101.com";
		result.tid = tid;
		result.lastPage = 1;
/*
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
*/		
		AsyncTask<NovelInfo, String, NovelInfo> request = new RequestTask();
		request.execute(result);
		result = request.get();

		return result;
	}
}

class RequestTask extends AsyncTask<NovelInfo, String, NovelInfo> {
    @Override
    protected NovelInfo doInBackground(NovelInfo... infos) {
    	NovelInfo result = infos[0];
    	boolean infoFound = false;
		boolean pageFound = false;
		
    	try {
        	URL url = new URL("http://ck101.com/thread-"+result.tid+"-1-1.html");
    		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    		connection.setDoOutput(true);
    		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");
    		connection.connect();
    		InputStream inStream = (InputStream) connection.getInputStream();
    		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf8"));
    		
    		String line = "";
    		String regex = "";
    		Matcher matcher;
    		Pattern p;
    		while (!(infoFound && pageFound) && (line = reader.readLine()) != null) {
    			int start;
    			if ((start = line.indexOf("<title>")) >= 0) {
    				start += 7;
    				int end = line.indexOf("</title>");
    				line = line.substring(start, end);

    				regex = "([\\[【「].+[\\]】」])?\\s*[【《]?([\\S&&[^】》]]+).*作者[】:：︰ ]*([\\S&&[^(（《﹝【]]+)";
    				p = Pattern.compile(regex);
    				matcher = p.matcher(line);
    				if (matcher.find()) {
    					result.name = matcher.group(2);
    					result.author = matcher.group(3);
    					infoFound = true;
    				}
    			}
    			if (line.indexOf("<div class=\"pg\">") >= 0) {
    				// "<a href=\"thread-<tid>-<page>-1.html\" class=\"last\">";
    				regex = "<a href=\"thread-\\d+-(\\d+)-\\w+.html\"";
    				p = Pattern.compile(regex);
    				matcher = p.matcher(line);
    				int tempPage = 1; // get the second last page link
    				while (matcher.find()) {
    					result.lastPage = tempPage;
    					tempPage = Integer.parseInt(matcher.group(1));
    				}
    				pageFound = true;
    			}
    		}
    		reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
/*
    @Override
    protected void onPostExecute(NovelInfo result) {
        super.onPostExecute(result);
    }
*/
}
