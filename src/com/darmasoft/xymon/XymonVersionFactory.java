package com.darmasoft.xymon;

public class XymonVersionFactory {

	private static final String TAG = "XymonVersionFactory";
	
	public static XymonVersion initial() {
		return new XymonVersion423("4.2.3");
	}
	
	public static XymonVersion for_version(String version) throws UnsupportedVersionException {
		Log.d(TAG, String.format("for_version(%s)", version));
		
		if (XymonVersion423.sufficient_for_version(version)) {
			return(new XymonVersion423(version));
		} else if (XymonVersion434.sufficient_for_version(version)) {
			return(new XymonVersion434(version));
		} else {
			throw new UnsupportedVersionException(version, String.format("Unsupported version: %s", version));
		}
	}
}
