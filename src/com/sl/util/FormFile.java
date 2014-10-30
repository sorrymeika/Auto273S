package com.sl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.graphics.Bitmap;

public class FormFile {

	private InputStream inStream;
	private File file;
	/* 文件名称 */
	private String filname;
	/* 请求参数名称 */
	private String parameterName;
	/* 内容类型 */
	private String contentType = "application/octet-stream";

	public FormFile(String filname, byte[] data, String parameterName,
			String contentType) {

		this.inStream = BitmapTools.getInstance().Byte2InputStream(data);
		this.filname = filname;
		this.parameterName = parameterName;
		if (contentType != null)
			this.contentType = contentType;
	}

	public FormFile(String filname, Bitmap image, String parameterName,
			String contentType) {

		this.inStream = BitmapTools.getInstance().Bitmap2InputStream(image);
		this.filname = filname;
		this.parameterName = parameterName;
		if (contentType != null)
			this.contentType = contentType;
	}

	public FormFile(String filname, File file, String parameterName,
			String contentType) {
		this.filname = filname;
		this.parameterName = parameterName;
		this.file = file;
		try {
			this.inStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (contentType != null)
			this.contentType = contentType;
	}

	public File getFile() {
		return file;
	}

	public InputStream getInStream() {
		return inStream;
	}

	public String getFilname() {
		return filname;
	}

	public void setFilname(String filname) {
		this.filname = filname;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}