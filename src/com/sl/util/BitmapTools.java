package com.sl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

public class BitmapTools {
	private static class BitmapToolsHolder {
		private static BitmapTools instance = new BitmapTools();
	}

	private BitmapTools() {

	}

	public static BitmapTools getInstance() {
		return BitmapToolsHolder.instance;
	}

	public Bitmap CompressImageFromFile(String srcPath, float maxWidth,
			float maxHeight) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// 只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;

		int be = 1;// be=1表示不缩放
		if (w > h && w > maxWidth) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / maxWidth);
		} else if (w < h && maxHeight != 0 && h > maxHeight) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / maxHeight);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置采样率

		newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;// 该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		// return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
		// 其实是无效的,大家尽管尝试
		return bitmap;
	}

	public String Bitmap2Base64String(Bitmap bitmap) {
		return "data:image/png;base64,"
				+ Base64.encodeToString(Bitmap2Bytes(bitmap), Base64.DEFAULT);
	}

	public Bitmap CompressBitmap(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 90, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 90;
		while (options >= 50 && baos.size() / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	public Bitmap CompressBitmap(Bitmap image, float maxWidth, float maxHeight) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

		Log.d("baos.size()", "baos.size()=" + baos.size());

		if (baos.size() / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为

		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > maxWidth) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / maxWidth);
		} else if (w < h && maxHeight != 0 && h > maxHeight) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / maxHeight);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return CompressBitmap(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	public Bitmap BitmapCrop(Bitmap bitmap) {
		int w = bitmap.getWidth(); // 得到图片的宽，高
		int h = bitmap.getHeight();

		int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

		int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
		int retY = w > h ? 0 : (h - w) / 2;

		// 下面这句是关键
		return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
	}

	public Bitmap Path2Bitmap(String bitmapPath) {

		File file = new File(bitmapPath);

		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		Bitmap bitmap = this.InputStream2Bitmap(fs);

		try {
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fs = null;

		return bitmap;
	}

	public void RecycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			// 回收并且置为null
			bitmap.recycle();
			bitmap = null;
		}
		System.gc();
	}

	public Bitmap BitmapCrop(String bitmapPath) {

		Bitmap bitmap = this.CompressImageFromFile(bitmapPath, 640, 0);

		Bitmap result = this.BitmapCrop(bitmap);

		// RecycleBitmap(bitmap);

		bitmap = null;

		return result;
	}

	// 将byte[]转换成InputStream
	public InputStream Byte2InputStream(byte[] b) {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		return bais;
	}

	// 将InputStream转换成byte[]
	public byte[] InputStream2Bytes(InputStream is) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		byte[] result = null;
		try {
			while ((len = is.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			is.close();

			result = outStream.toByteArray();
			outStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	// 将Bitmap转换成InputStream
	public InputStream Bitmap2InputStream(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	// 将Bitmap转换成InputStream
	public InputStream Bitmap2InputStream(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	// 将InputStream转换成Bitmap
	public Bitmap InputStream2Bitmap(InputStream is) {
		return BitmapFactory.decodeStream(is);
	}

	// Drawable转换成InputStream
	public InputStream Drawable2InputStream(Drawable d) {
		Bitmap bitmap = this.Drawable2Bitmap(d);
		return this.Bitmap2InputStream(bitmap);
	}

	// InputStream转换成Drawable
	public Drawable InputStream2Drawable(InputStream is) {
		Bitmap bitmap = this.InputStream2Bitmap(is);
		return this.Bitmap2Drawable(bitmap);
	}

	// Drawable转换成byte[]
	public byte[] Drawable2Bytes(Drawable d) {
		Bitmap bitmap = this.Drawable2Bitmap(d);
		return this.Bitmap2Bytes(bitmap);
	}

	// byte[]转换成Drawable
	public Drawable Bytes2Drawable(byte[] b) {
		Bitmap bitmap = this.Bytes2Bitmap(b);
		return this.Bitmap2Drawable(bitmap);
	}

	// Bitmap转换成byte[]
	public byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// byte[]转换成Bitmap
	public Bitmap Bytes2Bitmap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		}
		return null;
	}

	// Drawable转换成Bitmap
	public Bitmap Drawable2Bitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	// Bitmap转换成Drawable
	public Drawable Bitmap2Drawable(Bitmap bitmap) {
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		Drawable d = (Drawable) bd;
		return d;
	}

}