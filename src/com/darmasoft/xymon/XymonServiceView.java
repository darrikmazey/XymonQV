package com.darmasoft.xymon;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XymonServiceView extends LinearLayout {

	public XymonServiceView(XymonService service, Context context) {
		super(context);
		
		int color = ColorHelper.colorForString(service.color());
		
		this.setOrientation(HORIZONTAL);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 80));
		this.setPadding(5,5,5,5);
		Drawable d = getResources().getDrawable(R.drawable.text_view_border);
		d.setColorFilter(color, PorterDuff.Mode.DARKEN);
//		d.setColorFilter(Color.argb(128,0,0,0), PorterDuff.Mode.DARKEN);
		this.setBackgroundDrawable(d);
		
		TextView tv_hostname = new TextView(context);
		tv_hostname.setText(service.host().hostname());
		tv_hostname.setGravity(Gravity.LEFT);
		tv_hostname.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
		tv_hostname.setPadding(5,5,5,5);
		this.addView(tv_hostname);
		
		TextView tv_service_name = new TextView(context);
		tv_service_name.setText(service.name());
		tv_service_name.setGravity(Gravity.RIGHT);
		tv_service_name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
		tv_service_name.setPadding(5, 5, 25, 5);
		this.addView(tv_service_name);
		
		TextView tv_color = new TextView(context);
		tv_color.setGravity(Gravity.RIGHT);
		tv_color.setLayoutParams(new LayoutParams(50, LayoutParams.MATCH_PARENT));
		tv_color.setBackgroundColor(color);
		tv_color.setPadding(25,5,5,5);
		this.addView(tv_color);

	}

	public XymonServiceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

}
