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

public class Analysis {
	public static NovelInfo analysisUrl(int domainID, String tid) throws Exception {
		NovelInfo result = new NovelInfo();
		result.domainID = domainID;
		result.tid = tid;
		result.lastPage = 1;
		
		AsyncTask<NovelInfo, Integer, NovelInfo> request;
		request = (domainID == 0) ? new Ck101Analyzer() : new EynyAnalyzer();
		request.execute(result);
		result = request.get();

		return result;
	}
}

class Ck101Analyzer extends AsyncTask<NovelInfo, Integer, NovelInfo> {
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
    		int start, end;
    		while (!(infoFound && pageFound) && (line = reader.readLine()) != null) {
    			if ((start = line.indexOf("<title>")) >= 0) {
    				start += 7;
    				end = line.indexOf("</title>");
    				line = line.substring(start, end);

    				regex = "([\\[【「（《].+[\\]】」）》])?\\s*[【《]?\\s*([\\S&&[^】》]]+).*作者[】:：︰ ]*([\\S&&[^(（《﹝【]]+)";
    				p = Pattern.compile(regex);
    				matcher = p.matcher(line);
    				if (matcher.find()) {
    					result.name = matcher.group(2);
    					result.author = matcher.group(3);
    					infoFound = true;
    				}
    			}
    			if ((start = line.indexOf("<div class=\"pg\">")) >= 0) {
    				end = line.indexOf("class=\"nxt\"", start);
    				if (end <= 0)
    					continue;
    				
    				end = line.lastIndexOf("<a href=\"thread-", end) - 1; // the start position of next page link
    				start = line.lastIndexOf("<a href=\"thread-", end); // the start position of last page link
    				
    				regex = "<a href=\"thread-\\d+-(\\d+)-\\w+.html\"";
    				p = Pattern.compile(regex);
    				matcher = p.matcher(line);
    				if (matcher.find(start)) {
    					result.lastPage = Integer.parseInt(matcher.group(1));
    					pageFound = true;
    				}
    			}
    		}
    		reader.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return result;
    }
}


class EynyAnalyzer extends AsyncTask<NovelInfo, Integer, NovelInfo> {
    @Override
    protected NovelInfo doInBackground(NovelInfo... infos) {
    	NovelInfo result = infos[0];
    	boolean infoFound = false;
		boolean pageFound = false;
		
    	try {
        	URL url = new URL("http://www.eyny.com/thread-"+result.tid+"-1-1.html");
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
    		int start, end;
    		while (!(infoFound && pageFound) && (line = reader.readLine()) != null) {
    			if ((start = line.indexOf("<title>")) >= 0) {
    				start += 7;
    				end = line.indexOf("</title>", start);
    				line = line.substring(start, end);
    				
    				regex = "(\\S+)\\s*-\\s*【(\\S+)】";
    				p = Pattern.compile(regex);
    				matcher = p.matcher(line);
    				if (matcher.find()) {
    					result.name = matcher.group(2);
    					result.author = matcher.group(1);
    					infoFound = true;
    				}
    			}
    			if ((start = line.indexOf("<div class=\"pg\">")) >= 0) {
    				end = line.indexOf("class=\"nxt\"", start);
    				if (end <= 0)
    					continue;
    				
    				end = line.lastIndexOf("<a href=\"thread-", end) - 1; // the start position of next page link
    				start = line.lastIndexOf("<a href=\"thread-", end); // the start position of last page link
    				
    				regex = "<a href=\"thread-\\d+-(\\d+)-\\w+.html\"";
    				p = Pattern.compile(regex);
    				matcher = p.matcher(line);
    				if (matcher.find(start)) {
    					result.lastPage = Integer.parseInt(matcher.group(1));
    					pageFound = true;
    				}
    			}
    		}
    		reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}