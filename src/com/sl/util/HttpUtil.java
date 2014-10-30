package com.sl.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpUtil {

	public static boolean IsNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static boolean IsWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static boolean IsMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	private static String _post(String url, List<NameValuePair> nameValuePairs,
			List<FormFile> files) {

		Log.i("log", "start " + url);

		int connection_Timeout = 15000;

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams,
				connection_Timeout);
		HttpConnectionParams.setSoTimeout(my_httpParams, connection_Timeout);
		HttpClient client = new DefaultHttpClient(my_httpParams);

		HttpPost post = new HttpPost(url);

		if (null != files && 0 != files.size()) {
			MultipartEntity mpEntity = new MultipartEntity();

			for (FormFile uploadFile : files) {

				InputStreamBody isb = new InputStreamBody(
						uploadFile.getInStream(), uploadFile.getContentType(),
						uploadFile.getFilname());

				Log.d("upload", uploadFile.getParameterName() + " "
						+ uploadFile.getContentType());

				mpEntity.addPart(uploadFile.getParameterName(), isb);
			}

			for (NameValuePair nv : nameValuePairs) {

				mpEntity.addPart(
						nv.getName(),
						StringBody.create(nv.getValue(), "text/plain",
								Charset.forName("UTF-8")));
			}

			post.setEntity(mpEntity);

		} else if (null != nameValuePairs && 0 != nameValuePairs.size()) {
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		HttpResponse response;
		HttpEntity resEntity = null;
		String json = "";

		try {
			response = client.execute(post);
			resEntity = response.getEntity();

			// System.out.println(response.getStatusLine());// 通信Ok

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (resEntity != null) {
				json = EntityUtils.toString(resEntity, "utf-8");
				resEntity.consumeContent();
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		client.getConnectionManager().shutdown();

		// System.out.println(json);

		Log.i("log", "end");

		return json;
	}

	private static String _get(String url) {

		Log.i("log", "start " + url);

		int connection_Timeout = 15000;

		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams,
				connection_Timeout);
		HttpConnectionParams.setSoTimeout(my_httpParams, connection_Timeout);
		HttpClient client = new DefaultHttpClient(my_httpParams);

		HttpGet post = new HttpGet(url);

		HttpResponse response;
		HttpEntity resEntity = null;

		try {
			response = client.execute(post);
			resEntity = response.getEntity();

			// System.out.println(response.getStatusLine());// 通信Ok

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String json = "";

		try {
			if (resEntity != null) {
				json = EntityUtils.toString(resEntity, "utf-8");
				resEntity.consumeContent();
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		client.getConnectionManager().shutdown();

		// System.out.println(json);

		Log.i("log", "end");

		return json;
	}

	public static String get(String url) {
		return _get(url);
	}

	public static String post(String path, List<NameValuePair> params,
			FormFile file) throws Exception {

		List<FormFile> files = new ArrayList<FormFile>();
		files.add(file);

		return _post(path, params, files);
	}

	public static String post(String path, List<NameValuePair> params)
			throws Exception {
		return _post(path, params, null);
	}

	public static String post(String path, List<NameValuePair> params,
			List<FormFile> files) throws Exception {
		return _post(path, params, files);
	}
}