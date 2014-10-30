package com.sl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sl.auto273s.R;
import com.sl.ui.UIWebView;
import com.sl.util.Regex;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.graphics.Bitmap;

public class LoginActivity extends Activity {

	private WebView webView;
	private String Status;
	private String ASPXCOOKIEWebApi;
	private String NET_SessionId;
	private String UserName;
	private Boolean IsLogining = false;

	private void tip(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void loadWebView() {

		webView = (WebView) this.findViewById(R.id.loginWebview);

		WebSettings settings = webView.getSettings();

		settings.setCacheMode(WebSettings.LOAD_DEFAULT);

		settings.setJavaScriptEnabled(true);

		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				Log.d("OnReceivedSslError", "OnReceivedSslError");
				handler.proceed();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

				if ("https://219.143.230.66:9443/CwlProWeb/index/index.action"
						.equals(url)) {
					webView.loadUrl("http://ms1.962666.com/acc/login");
					return;
				} else if (url.indexOf("/Acc/ExternalLoginCallback") != -1) {
					IsLogining = true;
					return;
				}
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (url.indexOf("/Acc/ExternalLoginCallback") != -1) {
					webView.loadUrl("javascript:prompt(document.body.innerHTML);");
					return;
				}
				super.onPageFinished(view, url);
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {

			public void onExceededDatabaseQuota(String url,
					String databaseIdentifier, long currentQuota,
					long estimatedSize, long totalUsedQuota,
					WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(50 * 1024 * 1024);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {

				Log.d("html", message);

				IsLogining = false;

				Status = Regex
						.find(message,
								"(?<=<input type=\"hidden\" name=\"Status\" value=\")(.*?)(?=\")");

				Log.d("Status", Status);

				ASPXCOOKIEWebApi = Regex
						.find(message,
								"(?<=<input type=\"hidden\" name=\"\\.ASPXCOOKIEWebApi\" value=\")(.*?)(?=\")");

				Log.d("ASPXCOOKIEWebApi", ASPXCOOKIEWebApi);

				NET_SessionId = Regex
						.find(message,
								"(?<=<input type=\"hidden\" name=\"ASP\\.NET_SessionId\" value=\")(.*?)(?=\")");

				Log.d("NET_SessionId", NET_SessionId);

				UserName = Regex
						.find(message,
								"(?<=<input type=\"hidden\" name=\"UserName\" value=\")(.*?)(?=\")");

				Log.d("UserName", UserName);

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						finishActivity();
					}

				}, 1000);

				result.confirm(null);
				return true;
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (100 == newProgress) {

				}
				super.onProgressChanged(view, newProgress);
			}
		});

		webView.loadUrl("http://ms1.962666.com/acc/login");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		loadWebView();

		Button button = (Button) this.findViewById(R.id.btnBack);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finishActivity();
			}

		});
	}

	private void finishActivity() {
		if (IsLogining) {
			tip("正在登录，请稍候...");
			return;
		}

		Intent i = new Intent();
		if ("0".equals(Status)) {
			i.putExtra("success", "true");
			i.putExtra("_ASPXCOOKIEWebApi", ASPXCOOKIEWebApi);
			i.putExtra("ASP_NET_SessionId", NET_SessionId);
			i.putExtra("UserName", UserName);

		} else {

			i.putExtra("success", "false");
		}
		setResult(RESULT_OK, i);
		webView.destroy();
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finishActivity();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
