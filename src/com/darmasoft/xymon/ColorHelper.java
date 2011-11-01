package com.darmasoft.xymon;

import android.graphics.Color;

public class ColorHelper {

	public static int colorForString(String color) {
		if (color == null) {
			return(Color.BLACK);
		}
		String c = color.toLowerCase();
		if (c.equals("red")) {
			return(Color.argb(150,255,0,0));
		} else if (c.equals("yellow")) {
//			return(Color.YELLOW);
			return(Color.argb(200,255,180,0));
		} else if (c.equals("green")) {
			return(Color.GREEN);
		} else if (c.equals("blue")) {
			return(Color.BLUE);
		} else if (c.equals("purple")) {
			return(Color.argb(255, 255, 0, 255));
		} else if (c.equals("clear")) {
			return(Color.WHITE);
		} else {
			return(Color.BLACK);
		}
	}
}
