package com.darmasoft.xymon;

public class XymonVersionFactory {

	public static XymonVersion for_version(String version) {
		if (XymonVersion423.sufficient_for_version(version)) {
			return(new XymonVersion423(version));
		} else {
			return(new XymonVersion423(version));
		}
	}
}
