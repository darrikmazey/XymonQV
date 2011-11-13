package com.darmasoft.xymon;

import java.util.Date;

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
import android.widget.LinearLayout.LayoutParams;

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
		
//		Log.d(TAG, "constructor: " + service.name());
		
		int color = ColorHelper.colorForString(service.color());
		
		this.setOrientation(HORIZONTAL);
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 80);
		p.setMargins(0,0,0,5);
		this.setLayoutParams(p);
		this.setPadding(5,5,5,5);
		Drawable d = getResources().getDrawable(R.drawable.text_view_border);
		d.setColorFilter(color, PorterDuff.Mode.DARKEN);
		this.setBackgroundDrawable(d);

		TextView tv_color = new TextView(context);
		tv_color.setGravity(Gravity.LEFT);
		tv_color.setLayoutParams(new LayoutParams(50, LayoutParams.MATCH_PARENT));
		tv_color.setBackgroundColor(color);
		tv_color.setPadding(5,5,5,5);
		this.addView(tv_color);
		
		LinearLayout layout = new LinearLayout(ctx);
		layout.setOrientation(VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setPadding(5,5,5,5);
		
		LinearLayout line1 = new LinearLayout(ctx);
		line1.setOrientation(HORIZONTAL);
		line1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		
		TextView tv_svcname = new TextView(ctx);
		tv_svcname.setText(service.name());
		tv_svcname.setTextSize(14);
		
		line1.addView(tv_svcname);
		
		layout.addView(line1);
		
		LinearLayout line2 = new LinearLayout(ctx);
		line2.setOrientation(HORIZONTAL);
		line2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		
		TextView tv_duration = new TextView(ctx);
		long du = (long) service.duration();
		
		String fts = DateHelper.formatted_elapsed_time(du * 1000);
		
		tv_duration.setText(String.format("Down %s", fts));
		tv_duration.setTextSize(12);
		
		line2.addView(tv_duration);
		
		layout.addView(line2);
		
		addView(layout);
		

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
			String url = service.url();
			Log.d(TAG, String.format("found url: %s", url));
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			ctx.startActivity(i);
			return(true);
		}
	}

}
