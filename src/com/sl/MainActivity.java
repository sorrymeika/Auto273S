package com.sl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sl.auto273s.R;
import com.sl.ui.ColorPickerDialog;
import com.sl.ui.UIWebView;
import com.sl.ui.UpdateDialog;
import com.sl.util.BitmapTools;
import com.sl.util.FormFile;
import com.sl.util.HttpUtil;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends Activity {

	private UIWebView webView;

	private String selectImageCallback;
	private String loginCallback;
	private String takePhotoCallback;
	private static int RESULT_LOAD_IMAGE = 1;
	private static int RESULT_LOGIN = 2;
	private static int RESULT_TAKE_PHOTO = 3;

	private Boolean created = false;
	private Button loginButton;

	private SensorManager sensorManager;
	private Vibrator vibrator;

	private static final String TAG = "SensorActivity";
	private static final int SENSOR_SHAKE = 10;
	private UpdateDialog updateManage;

	private Handler handler = new Handler();
	private Handler threadHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			switch (msg.what) {
			case 0:
				webView.loadUrl("javascript:window.hybridFunctions." + bundle.getString("callback") + "("
						+ bundle.getString("result") + ");");
				break;
			case SENSOR_SHAKE:
				webView.loadUrl("javascript:window.app_trigger('motion');");
				break;
			}
		}
	};

	private void setFullScreen() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void cancelFullScreen() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void tip(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void loadWebView() {

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}

		webView = (UIWebView) this.findViewById(R.id.webview);

		WebSettings settings = webView.getSettings();

		settings.setCacheMode(WebSettings.LOAD_DEFAULT);

		settings.setJavaScriptEnabled(true);

		settings.setAllowFileAccess(true);
		settings.setDatabaseEnabled(true);
		String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		settings.setDatabasePath(dir);
		settings.setDomStorageEnabled(true);
		settings.setGeolocationEnabled(true);
		settings.setTextSize(TextSize.NORMAL);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			settings.setAllowFileAccessFromFileURLs(true);
			settings.setAllowUniversalAccessFromFileURLs(true);
		}

		registerForContextMenu(webView);

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		webView.setWebChromeClient(new WebChromeClient() {

			public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota,
					long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(50 * 1024 * 1024);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
					JsPromptResult result) {

				try {
					result.confirm(null);

					JSONObject info = new JSONObject(message);

					hybrid(info.getString("method"), info.has("callback") ? info.getString("callback") : null,
							info.has("params") ? info.get("params") : null);

				} catch (JSONException e) {
					e.printStackTrace();
				}

				return true;
			}
			
			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setTitle("通知");
				builder.setMessage(message);
				// 更新
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						result.confirm();
					}
				});
				
				Dialog noticeDialog = builder.create();
				noticeDialog.show();
				
				return true;
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (100 == newProgress) {
				}
				super.onProgressChanged(view, newProgress);
			}
		});

		webView.loadUrl("file:///android_asset/index.html");
	}

	private void hybrid(final String method, final String callback, final Object params) {

		if (method.equals("exitLauncher")) {
			exitLauncher(callback);

		} else if ("exit".equals(method)) {
			tip("退出");
			finish();
			System.exit(0);

		} else if ("tip".equals(method)) {
			Toast.makeText(MainActivity.this, (String) params, Toast.LENGTH_LONG).show();

		} else if ("getAppInfo".equals(method)) {

		} else if ("queryThumbnailList".equals(method)) {

			JSONArray thumbnailList = queryThumbnailList();

			hybridReturn(callback, thumbnailList.toString());

		} else if ("pickColor".equals(method)) {
			ColorPickerDialog colorpicker = new ColorPickerDialog(this, 0xFFFFFF, "选择背景色",
					new ColorPickerDialog.OnColorChangedListener() {

						@Override
						public void colorChanged(int color) {
							hybridReturn(callback, "\"" + String.format("#%06X", (0xFFFFFF & color)) + "\"");
						}
					});
			colorpicker.show();

		} else if ("share".equals(method)) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
			intent.putExtra(Intent.EXTRA_TEXT, "分享");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(intent, getTitle()));

		} else if ("login".equals(method)) {

			Intent i = new Intent(MainActivity.this, LoginActivity.class);
			startActivityForResult(i, RESULT_LOGIN);

		} else if ("pickImage".equals(method)) {
			selectImageCallback = callback;

			Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

			startActivityForResult(i, RESULT_LOAD_IMAGE);

		} else if ("takePhoto".equals(method)) {
			takePhotoCallback = callback;

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, RESULT_TAKE_PHOTO);

		} else if ("updateApp".equals(method)) {
			Log.d("updateApp", "begin updateApp");
			JSONObject data = (JSONObject) params;
			// 检查软件更新
			try {
				if (null == updateManage) {
					updateManage = new UpdateDialog(MainActivity.this);
				}

				updateManage.showNoticeDialog(data.getString("downloadUrl"), data.getString("versionName"));

			} catch (JSONException e) {
				e.printStackTrace();
			}

		} else if ("startMotion".equals(method)) {
			if (sensorManager != null) {// 注册监听器
				sensorManager.registerListener(sensorEventListener,
						sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
				// 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
			}
		} else if ("stopMotion".equals(method)) {
			if (sensorManager != null) {// 取消监听器
				sensorManager.unregisterListener(sensorEventListener);
			}
		} else {

			if ("post".equals(method)) {
				if (!HttpUtil.IsNetworkConnected(this)) {
					tip("网络连接已断开");
				}
			}

			new Thread() {
				@Override
				public void run() {
					Message msg = new Message();
					Bundle bundle = new Bundle();

					if (null != callback) {
						bundle.putString("callback", callback);
					}

					if ("post".equals(method)) {
						String result = httpPost((JSONObject) params);

						Log.i("httpPostResult", callback + result);

						bundle.putString("result", result);
					}

					msg.setData(bundle);
					msg.what = 0;

					threadHandler.sendMessage(msg);
				}
			}.start();

		}
	}

	private void hybridReturn(String callback, String param) {
		js("hybridFunctions." + callback, param);
	}

	private void js(final String func, final String param) {

		if (func != null)
			handler.post(new Runnable() {
				public void run() {

					Log.i("js", "javascript:window." + func + "(" + param + ");");
					webView.loadUrl("javascript:window." + func + "(" + param + ");");
				}
			});
	}

	private void exitLauncher(final String callback) {
		cancelFullScreen();

		Animation anim = AnimationUtils.loadAnimation(this, R.anim.push_left_in);

		webView.startAnimation(anim);

		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				hybridReturn(callback, null);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		View viewSplash = this.findViewById(R.id.loading);
		viewSplash.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.push_left_out));
		viewSplash.setVisibility(View.GONE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.findViewById(R.id.loading).setVisibility(View.VISIBLE);

		// setFullScreen();

		loadWebView();
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	}

	@Override
	protected void onResume() {
		if (created) {
		} else {
			created = true;
		}
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		Log.d("orientation",
				"orientation " + newConfig.orientation + " " + webView.getMeasuredWidth() + ","
						+ webView.getMeasuredHeight());

		super.onConfigurationChanged(newConfig);
	}

	private long mExitTime = 0;

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			// webView.loadUrl("javascript:window.appBack();");

			if (webView.canGoBack()) {
				webView.goBack();

			} else {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
					mExitTime = System.currentTimeMillis();
				} else {
					finish();
					System.exit(0);
				}
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 重力感应监听
	 */
	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// 传感器信息改变时执行该方法
			float[] values = event.values;
			float x = values[0]; // x轴方向的重力加速度，向右为正
			float y = values[1]; // y轴方向的重力加速度，向前为正
			float z = values[2]; // z轴方向的重力加速度，向上为正
			Log.d(TAG, "x轴方向的重力加速度" + x + "；y轴方向的重力加速度" + y + "；z轴方向的重力加速度" + z);
			// 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。
			int medumValue = 19;// 三星 i9250怎么晃都不会超过20，没办法，只设置19了
			if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
				// vibrator.vibrate(200);
				Message msg = new Message();
				msg.what = SENSOR_SHAKE;
				threadHandler.sendMessage(msg);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	@SuppressLint({ "NewApi", "SdCardPath" })
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			if (requestCode == RESULT_LOAD_IMAGE && null != data) {

				Uri selectedImage = data.getData();

				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

				String picturePath = cursor.getString(columnIndex);

				cursor.close();

				BitmapTools bitmapTools = BitmapTools.getInstance();
				Bitmap bitmap = bitmapTools.BitmapCrop(picturePath);

				// Uri uri =
				// Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
				// bitmap, null, null));

				// Uri uri = Uri.parse(picturePath);

				JSONObject json = new JSONObject();
				try {
					json.put("path", picturePath);
					json.put("src", bitmapTools.Bitmap2Base64String(bitmap));

				} catch (JSONException e) {
					e.printStackTrace();
				}

				hybridReturn(selectImageCallback, json.toString());

			} else if (requestCode == RESULT_TAKE_PHOTO) {
				String sdStatus = Environment.getExternalStorageState();
				if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
					Log.i("TestFile", "SD card is not avaiable/writeable right now.");
					return;
				}

				String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";

				// Toast.makeText(this, name, Toast.LENGTH_LONG).show();
				Bundle bundle = data.getExtras();
				Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式

				FileOutputStream b = null;
				File file = new File("/sdcard/Image/");
				file.mkdirs();// 创建文件夹
				String picturePath = "/sdcard/Image/" + name;

				try {
					b = new FileOutputStream(picturePath);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						b.flush();
						b.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				picturePath = Uri.parse(picturePath).toString();
				BitmapTools bitmapTools = BitmapTools.getInstance();
				bitmap = bitmapTools.BitmapCrop(picturePath);

				JSONObject json = new JSONObject();
				try {
					json.put("path", picturePath);
					json.put("src", bitmapTools.Bitmap2Base64String(bitmap));

				} catch (JSONException e) {
					e.printStackTrace();
				}

				hybridReturn(takePhotoCallback, json.toString());

			} else if (requestCode == RESULT_LOGIN) {

				if ("false".equals(data.getStringExtra("success"))) {
					tip("登录失败！");
				} else {
					tip("登录成功！");

					data.getStringExtra("_ASPXCOOKIEWebApi");
					data.getStringExtra("ASP_NET_SessionId");
					data.getStringExtra("UserName");

					JSONObject json = new JSONObject();
					try {
						json.put(".ASPXCOOKIEWebApi", data.getStringExtra("_ASPXCOOKIEWebApi"));
						json.put("ASP.NET_SessionId", data.getStringExtra("ASP_NET_SessionId"));
						json.put("UserName", data.getStringExtra("UserName"));

					} catch (JSONException e) {
						e.printStackTrace();
					}

					hybridReturn(loginCallback, json.toString());

				}

			}

		}
	}

	public JSONArray queryThumbnailList() {
		JSONArray array = new JSONArray();
		JSONObject item;

		// 获取上下文
		Context ctx = MainActivity.this;
		// 获取ContentResolver对象
		ContentResolver resolver = ctx.getContentResolver();
		// 获得外部存储卡上的图片缩略图
		Cursor cursor = resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, null, null, null);
		// 为了for循环性能优化，用一变量存储数据条数
		int totalCount = cursor.getCount();

		// 将Cursor移动到第一位
		cursor.moveToFirst();
		// 将缩略图数据添加到ArrayList中
		for (int i = 0; i < totalCount; i++) {

			item = new JSONObject();

			int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
			String src = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));

			int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
			String imageSrc = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

			try {
				item.put("id", id);
				item.put("src", Uri.parse(src).toString());
				item.put("imageId", imageId);
				item.put("imageSrc", imageSrc);

			} catch (JSONException e) {
				e.printStackTrace();
			}

			array.put(item);
			cursor.moveToNext();
		}
		// 关闭游标
		cursor.close();

		return array;
	}

	public String queryImageByThumbnailId(Integer thumbId) {

		String selection = MediaStore.Images.Thumbnails._ID + " = ?";
		String[] selectionArgs = new String[] { thumbId + "" };
		Cursor cursor = queryThumbnails(selection, selectionArgs);

		if (cursor.moveToFirst()) {
			int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
			cursor.close();
			return queryImageById(imageId);
		} else {
			cursor.close();
			return null;
		}
	}

	private Cursor queryThumbnails(String selection, String[] selectionArgs) {
		String[] columns = new String[] { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails._ID,
				MediaStore.Images.Thumbnails.IMAGE_ID };

		// 获取上下文
		Context ctx = MainActivity.this;
		// 获取ContentResolver对象
		ContentResolver resolver = ctx.getContentResolver();

		return resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs,
				MediaStore.Images.Thumbnails.DEFAULT_SORT_ORDER);
	}

	public String queryImageById(int imageId) {
		String selection = MediaStore.Images.Media._ID + "=?";
		String[] selectionArgs = new String[] { imageId + "" };
		Cursor cursor = queryImages(selection, selectionArgs);
		if (cursor.moveToFirst()) {
			String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
			cursor.close();

			return path;
		} else {
			cursor.close();
			return null;
		}
	}

	public Cursor queryImages(String selection, String[] selectionArgs) {
		String[] columns = new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DISPLAY_NAME };

		// 获取上下文
		Context ctx = MainActivity.this;
		// 获取ContentResolver对象
		ContentResolver resolver = ctx.getContentResolver();

		return resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs,
				MediaStore.Images.Media.DEFAULT_SORT_ORDER);
	}

	private String httpPost(JSONObject data) {

		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		List<FormFile> files = null;
		String key;
		JSONObject postFiles;
		BitmapTools bitmapTools;
		String path;

		try {
			if (data.has("data") && !data.isNull("data")) {
				JSONObject postParams = data.getJSONObject("data");

				if (data != null)
					for (Iterator<?> itr = postParams.keys(); itr.hasNext();) {
						key = itr.next().toString();

						Log.d("post", key + '=' + postParams.getString(key));

						postData.add(new BasicNameValuePair(key, postParams.getString(key)));
					}
			}

			if (data.has("files") && !data.isNull("files")) {

				postFiles = data.getJSONObject("files");

				if (postFiles != null) {
					files = new ArrayList<FormFile>();

					Log.d("files", postFiles.toString());

					for (Iterator<?> itr = postFiles.keys(); itr.hasNext();) {
						key = itr.next().toString();

						path = postFiles.getString(key);

						Log.d("files", key + '=' + postFiles.getString(key));

						files.add(new FormFile(path, new File(path), key, "image/jpeg"));
					}
				}

			}

			if (data.has("images") && !data.isNull("images")) {

				postFiles = data.getJSONObject("images");

				if (postFiles != null) {
					bitmapTools = BitmapTools.getInstance();
					if (null == files)
						files = new ArrayList<FormFile>();

					for (Iterator<?> itr = postFiles.keys(); itr.hasNext();) {
						key = itr.next().toString();

						path = postFiles.getString(key);

						Bitmap bitmap = bitmapTools.BitmapCrop(path);

						bitmap = bitmapTools.CompressBitmap(bitmap, 1024, 1024);

						files.add(new FormFile(path, bitmap, key, "image/jpeg"));
					}
				}
			}

			// Log.i("url", data.getString("url"));

			String result = HttpUtil.post(data.getString("url"), postData, files);

			return result;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			tip("网络错误");
		}

		return "";
	}

}
