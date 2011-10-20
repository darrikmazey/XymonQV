package com.darmasoft.xymon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.util.Log;

public class HttpHelper {

	public static final String TAG = "HttpHelper";
	
	public static String readBody(HttpResponse res) throws IllegalStateException, IOException {
		Log.d(TAG, res.getStatusLine().toString());
		if (res.getStatusLine().getStatusCode() == 200) {
			Log.d(TAG, "200 code");
			HttpEntity entity = res.getEntity();
			InputStreamReader isr = new InputStreamReader(entity.getContent());
			StringBuffer buffer = new StringBuffer();
			Reader in = new BufferedReader(isr);
			int ch;
			char[] tmp_buffer = new char[1025];
			while ((ch = in.read(tmp_buffer, 0, 1024)) > -1) {
				buffer.append(tmp_buffer);
				tmp_buffer = new char[1025];
			}
			return(buffer.toString());
		}
		return(null);
	}
}
