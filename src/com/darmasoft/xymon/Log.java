package com.darmasoft.xymon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

import android.os.Environment;

public class Log {

	public static boolean m_debug_mode_on = false;
	
	public static void set_debug_mode(boolean on) {
		m_debug_mode_on = on;
	}
	
	public static int d(String tag, String message) {
		if (m_debug_mode_on) {
			write_message_to_debug_log("D", tag, message);
		}
		return(android.util.Log.d(tag, message));
	}
	
	public static int e(String tag, String message) {
		if (m_debug_mode_on) {
			write_message_to_debug_log("E", tag, message);
		}
		return(android.util.Log.e(tag, message));
	}
	
	public static int i(String tag, String message) {
		if (m_debug_mode_on) {
			write_message_to_debug_log("I", tag, message);
		}
		return(android.util.Log.i(tag, message));
	}
	
	public static int w(String tag, String message) {
		if (m_debug_mode_on) {
			write_message_to_debug_log("W", tag, message);
		}
		return(android.util.Log.w(tag, message));
	}
	
	public static int v(String tag, String message) {
		if (m_debug_mode_on) {
			write_message_to_debug_log("V", tag, message);
		}
		return(android.util.Log.v(tag, message));
	}
	
	public static int wtf(String tag, String message) {
		if (m_debug_mode_on) {
			write_message_to_debug_log("WTF", tag, message);
		}
		return(android.util.Log.wtf(tag, message));
	}
	
	private static void write_message_to_debug_log(String mode, String tag, String message) {
		try {
			File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File debug_log = new File(dir, "xymonqv_debug.log");
			if (!debug_log.exists()) {
				debug_log.createNewFile();
			}
			FileWriter fw = new FileWriter(debug_log, true);
			Date d = new Date();
			fw.write(String.format("[%tF %tT] %s/%s : %s\n", d, d, mode, tag, message));
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.printStackTrace(e);
		}
	}
	
	public static void delete_debug_log() {
		android.util.Log.d("Log", "delete_debug_log()");
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File debug_log = new File(dir, "xymonqv_debug.log");
		boolean deleted = debug_log.delete();
		android.util.Log.d("Log", String.format("deleted: %b", deleted));
	}
	
	public static void printStackTrace(Throwable e) {
		if (m_debug_mode_on == true) {
			e.printStackTrace(get_print_stream());
		}
		e.printStackTrace();
	}
	
	private static PrintStream get_print_stream() {
		try {
			File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File debug_log = new File(dir, "xymonqv_debug.log");
			if (!debug_log.exists()) {
				debug_log.createNewFile();
			}
			FileOutputStream fs = new FileOutputStream(debug_log, true);
			return(new PrintStream(fs));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return(null);
	}

}
