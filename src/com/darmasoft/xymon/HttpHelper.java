package com.darmasoft.xymon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class HttpHelper {

	public static final String TAG = "HttpHelper";
	
	public static String readBody(HttpResponse res) throws XymonQVException {
		Log.d(TAG, res.getStatusLine().toString());
		int sc = res.getStatusLine().getStatusCode();
		StringBuffer buffer = new StringBuffer();
		try {
			HttpEntity entity = res.getEntity();
			InputStreamReader isr = new InputStreamReader(entity.getContent(), Charset.forName("ISO-8859-1"));
			Reader in = new BufferedReader(isr);
			char[] tmp_buffer = new char[1024];
			int bytes_read = 0;
			while ((bytes_read = in.read(tmp_buffer, 0, 1024)) > -1) {
				buffer.append(tmp_buffer, 0, bytes_read);
				tmp_buffer = new char[1024];
			}
			} catch(IOException e) {
				Log.printStackTrace(e);
			}
		if (sc == 200) {
			Log.d(TAG, "200 code");
			return(buffer.toString());
		} else if (sc == 404) {
			// url not found
			NotFoundException e = new NotFoundException();
			Log.printStackTrace(e);
			throw e;
		} else if (sc == 401) {
			// auth error
			InvalidUsernameOrPasswordException e = new InvalidUsernameOrPasswordException();
			Log.printStackTrace(e);
			throw e;
		} else {
			throw new XymonQVException(String.format("Bad Status: %d", sc));
		}
	}
}


