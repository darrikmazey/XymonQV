package com.darmasoft.xymon;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class XymonHostView extends LinearLayout  implements OnClickListener {

	private static final String TAG = "XymonHostView";
	public XymonHost host;
	public Context ctx;

	public XymonHostView(XymonHost h, Context context) {
		super(context);
		host = h;
		ctx = context;
		
		int color = ColorHelper.colorForString(host.worst_color());
		
		this.setOrientation(HORIZONTAL);
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 80);
		p.setMargins(0,0,0,5);
		this.setLayoutParams(p);
		this.setPadding(5,5,5,5);
		Drawable d = getResources().getDrawable(R.drawable.text_view_border);
		d.setColorFilter(color, PorterDuff.Mode.DARKEN);
		this.setBackgroundDrawable(d);

		TextView tv_color = new TextView(ctx);
		tv_color.setLayoutParams(new LayoutParams(50,LayoutParams.MATCH_PARENT));
		tv_color.setBackgroundColor(color);
		addView(tv_color);
		
		LinearLayout layout = new LinearLayout(ctx);
		layout.setOrientation(VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setPadding(5,5,5,5);
		
		LinearLayout line1 = new LinearLayout(ctx);
		line1.setOrientation(HORIZONTAL);
		line1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		
		TextView tv_hostname = new TextView(ctx);
		tv_hostname.setText(host.hostname());
		tv_hostname.setTextSize(14);
		
		line1.addView(tv_hostname);
		
		layout.addView(line1);
		
		LinearLayout line2 = new LinearLayout(ctx);
		line2.setOrientation(HORIZONTAL);
		line2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		
		String[] colors = { "red", "yellow", "purple" };
		for (String c : colors) {
			int count = host.service_count_by_color(c);
			if (count > 0) {
				TextView tv_count = new TextView(ctx);
				tv_count.setText(String.format("%d %s", count, c));
				tv_count.setTextSize(12);
				line2.addView(tv_count);
			}
		}
		
		layout.addView(line2);
		
		addView(layout);
		this.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, String.format("CLICK: %s", host.hostname()));
		Intent intent = new Intent(ctx, XymonQVHostActivity.class);
		intent.putExtra("hostname", host.hostname());
   		ctx.startActivity(intent);
	}

}
