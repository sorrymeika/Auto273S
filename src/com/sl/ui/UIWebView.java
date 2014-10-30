package com.sl.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

public class UIWebView extends WebView {

	public UIWebView(Context context) {
		super(context);
		this.setUp(context);
	}

	public UIWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setUp(context);
	}

	public UIWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setUp(context);
	}

	private void setUp(Context context) {

		this.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
		this.setLongClickable(false);
	}

	@Override
	public boolean performLongClick() {
		return true;
	}

	public void drawContent(Canvas paramCanvas) {
		onDraw(paramCanvas);
	}

	private boolean mBackgroundRemoved = false;

	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		if ((!this.mBackgroundRemoved)
				&& (getRootView().getBackground() != null)) {
			this.mBackgroundRemoved = true;
			post(new Runnable() {
				public void run() {
					UIWebView.this.getRootView().setBackgroundDrawable(null);
				}
			});
		}
	}
}