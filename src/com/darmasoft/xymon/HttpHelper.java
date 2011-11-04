package com.darmasoft.xymon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class HttpHelper {

	public static final String TAG = "HttpHelper";
	
	public static String readBody(HttpResponse res) throws XymonQVException {
		Log.d(TAG, res.getStatusLine().toString());
		int sc = res.getStatusLine().getStatusCode();
		if (sc == 200) {
			try {
			Log.d(TAG, "200 code");
			HttpEntity entity = res.getEntity();
			InputStreamReader isr = new InputStreamReader(entity.getContent());
			StringBuffer buffer = new StringBuffer();
			Reader in = new BufferedReader(isr);
			char[] tmp_buffer = new char[1025];
			while ((in.read(tmp_buffer, 0, 1024)) > -1) {
				buffer.append(tmp_buffer);
				tmp_buffer = new char[1025];
			}
			return(buffer.toString());
			} catch(IOException e) {
				Log.printStackTrace(e);
			}
		} else if (sc == 401) {
			InvalidUsernameOrPasswordException e = new InvalidUsernameOrPasswordException();
			Log.printStackTrace(e);
			throw e;
			// status not 200
		}
		return(null);
	}
}


