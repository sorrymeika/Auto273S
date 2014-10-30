package com.sl.controller;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sl.util.BitmapTools;
import com.sl.util.Regex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Login {

	private static final String TAG = "DarinAccount";
	private boolean isFirst = true;
	private AsyncHttpClient mClient;
	private EditText mPassWd;
	private RequestParams mRequestParams;
	private EditText mUserName;
	private RelativeLayout mVerifyLayout;
	private ImageView mVerifyPic;
	private EditText mVerrify;
	private Button mlogin;
	private CookieStore myCookieStore;
	private Context context;
	private Handler handler;

	public Login(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;

		this.initClient();
	}

	private void initClient() {
		this.myCookieStore = new PersistentCookieStore(context);

		this.mClient = new AsyncHttpClient(true, 80, 443);
		this.myCookieStore.clear();
		this.mClient.setCookieStore(this.myCookieStore);
		this.mClient
				.addHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		this.mClient.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
		this.mClient.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		this.mClient.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
		this.mClient.addHeader("Cache-Control", "max-age=0");
		this.mClient.addHeader("Content-Type",
				"application/x-www-form-urlencoded");
		this.mClient
				.addHeader(
						"User-agent",
						"Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19");
	}

	private void error(String callback, String text) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("callback", callback);
		bundle.putString("result", "{success: false,msg:\"" + text + "\"}");

		msg.setData(bundle);
		msg.what = 0;

		handler.sendMessage(msg);
	}

	private void result(String callback, JSONObject result) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("callback", callback);
		bundle.putString("result", result.toString());

		msg.setData(bundle);
		msg.what = 0;

		handler.sendMessage(msg);
	}

	public void setVerify(final String callback) {
		Time t = new Time();
		t.setToNow();

		String[] arrayOfString = { "image/jpg" };

		this.mClient.get(
				"https://219.143.230.66:9443/CwlProWeb/servlet/verifyCodeServlet?t="
						+ t.format2445(), new BinaryHttpResponseHandler(
						arrayOfString) {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] binaryData) {
						if ((binaryData != null) && (binaryData.length != 0)) {
							Bitmap localBitmap = BitmapFactory.decodeByteArray(
									binaryData, 0, binaryData.length);

							String url = BitmapTools.getInstance()
									.Bitmap2Base64String(localBitmap);
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] binaryData, Throwable error) {

						Login.this.error(callback, "网络错误");

					}
				});
	}

	private void redirect(final String callback, String token) {

		RequestParams p = new RequestParams();

		p.put("__RequestVerificationToken", token);
		p.put("provider", "CWLOauth2");

		this.mClient.post("http://115.29.141.126:8180/Acc/ExternalLogin", p,
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {

						Log.d("responseString", responseString);

						JSONObject json = new JSONObject();
						try {
							json.put("success", true);
							// json.put("src", token);

						} catch (JSONException e) {
							e.printStackTrace();
						}

						Login.this.result(callback, json);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {

						Log.d("redirect", "onFailure");

						Login.this.error(callback, "网络错误");
					}
				});

	}

	public void start(final String callback) {
		this.mClient.get("http://115.29.141.126:8180/acc/login",
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						String token = Regex
								.find(responseString,
										"(?<=<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\")(.*?)(?=\")");

						Log.d("responseString", responseString);
						Log.d("__RequestVerificationToken", token);

						if (null != token && !"".equals(token)) {

							Login.this.redirect(callback, token);

						} else {
							Login.this.error(callback, "网络错误");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						// TODO Auto-generated method stub

						Log.d("__RequestVerificationToken", "onFailure");

						Login.this.error(callback, "网络错误");
					}
				});

	}
}