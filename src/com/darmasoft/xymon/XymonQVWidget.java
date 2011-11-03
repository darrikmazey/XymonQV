package com.darmasoft.xymon;

import java.util.Date;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

public class XymonQVWidget extends AppWidgetProvider {

	private static final String TAG = "XymonQVWidget";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, String.format("onUpdate() : %tT", new Date()));
		XymonQVApplication app = ((XymonQVApplication) context.getApplicationContext());

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			app.cancelIntent();
			return;
		}

		if (appWidgetIds.length > 0 && !(app.showing_widgets())) {
			app.set_showing_widgets(true);
			app.setIntentForCurrentInterval();
		}
		
		for (int awid : appWidgetIds) {
			Log.d(TAG, String.format("updating widget: %d", awid));
			
			String color;
			int count = 0;
			try {
				app.xymon_server().load_last_data();
				color = app.xymon_server().color();
				count = app.xymon_server().service_count();
			} catch (UnsupportedVersionException e) {
				color = "black";
			}
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			views.setInt(R.id.widget_color_indicator, "setBackgroundColor", ColorHelper.colorForString(color));
			if (count > 0) {
				views.setTextViewText(R.id.widget_color_indicator, String.format("%d", count));
			} else {
				views.setTextViewText(R.id.widget_color_indicator, "");
			}
			appWidgetManager.updateAppWidget(awid, views);
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d(TAG, "onReceive()");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		this.onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(new ComponentName(context, XymonQVWidget.class)));
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.d(TAG, "onDeleted()");
	}
	
	@Override
	public void onDisabled(Context context) {
		Log.d(TAG, "onDisabled()");
		XymonQVApplication app = ((XymonQVApplication) context.getApplicationContext());
		app.set_showing_widgets(false);
		app.cancelIntent();
	}
	
	@Override
	public void onEnabled(Context context) {
		Log.d(TAG, "onEnabled()");
		XymonQVApplication app = ((XymonQVApplication) context.getApplicationContext());
		app.set_showing_widgets(true);
		app.setIntentForCurrentInterval();
	}
	
	
}
