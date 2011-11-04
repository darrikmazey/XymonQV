package com.darmasoft.xymon;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XymonServiceView extends LinearLayout implements OnClickListener {

	private final static String TAG = "XymonServiceView";
	public XymonService service;
	public Context ctx;
	
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	public XymonServiceView(XymonService svc, Context context) {
		super(context);
		service = svc;
		ctx = context;
		
		Log.d(TAG, "constructor: " + service.name());
		
		int color = ColorHelper.colorForString(service.color());
		
		this.setOrientation(HORIZONTAL);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 80));
		this.setPadding(5,5,5,5);
		Drawable d = getResources().getDrawable(R.drawable.text_view_border);
		d.setColorFilter(color, PorterDuff.Mode.DARKEN);
		this.setBackgroundDrawable(d);
		
		TextView tv_hostname = new TextView(context);
		tv_hostname.setText(service.host().hostname());
		tv_hostname.setGravity(Gravity.LEFT);
		tv_hostname.setTextSize(12);
		tv_hostname.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
		tv_hostname.setPadding(5,5,5,5);
		this.addView(tv_hostname);
		
		TextView tv_service_name = new TextView(context);
		tv_service_name.setText(service.name());
		tv_service_name.setTextSize(12);
		tv_service_name.setGravity(Gravity.RIGHT);
		tv_service_name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
		tv_service_name.setPadding(5, 5, 5, 5);
		this.addView(tv_service_name);
		
		ImageView iv_checkmark = new ImageView(context);
		if (svc.acked()) {
			iv_checkmark.setImageDrawable(getResources().getDrawable(R.drawable.ic_checkmark));
			iv_checkmark.setColorFilter(color);
		}
		iv_checkmark.setLayoutParams(new LayoutParams(50,50));
		iv_checkmark.setPadding(5,5,5,5);
		this.addView(iv_checkmark);
		
		TextView tv_color = new TextView(context);
		tv_color.setGravity(Gravity.RIGHT);
		tv_color.setLayoutParams(new LayoutParams(50, LayoutParams.MATCH_PARENT));
		tv_color.setBackgroundColor(color);
		tv_color.setPadding(5,5,5,5);
		this.addView(tv_color);

    	gestureDetector = new GestureDetector(new MyGestureDetector());
    	
    	gestureListener = new View.OnTouchListener() {
    		public boolean onTouch(View v, MotionEvent event) {
    			if (gestureDetector.onTouchEvent(event)) {
    				return(true);
    			}
    			return(false);
    		}
		};

		this.setOnClickListener(this);
		this.setOnTouchListener(gestureListener);
	}

	public XymonServiceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, String.format("CLICK: Host [%s] Service [%s]", this.service.host().hostname(), this.service.name()));
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			float dx = e2.getX() - e1.getX();
			Log.d(TAG, String.format("FLING: dx [%02f]", dx));
			if (dx > 0) {
				return(onFlingRight());
			}
			if (dx < 0) {
				return(onFlingLeft());
			}
			return(false);
		}
		
		public boolean onFlingLeft() {
			Log.d(TAG, "FLING LEFT");
			Log.d(TAG, String.format("Service URL: %s", service.host().server().service_url(service)));
			return(true);
		}
		
		public boolean onFlingRight() {
			Log.d(TAG, "FLING RIGHT");
			XymonHost host = service.host();
			XymonServer server = host.server();
			String version = server.version();
			Log.d(TAG, String.format("Server version: %s", version));
			String url = service.host().server().service_url(service);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			ctx.startActivity(i);
			return(true);
		}
	}

}
