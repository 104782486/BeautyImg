package com.huewu.pla.sample;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ��ȡHTML��ҳ����
 * 
 * @author Administrator
 *
 */
public class GetHtmlContent {

	public static String htmlContent(String path) {
		URL url;
		try {
			url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			if (conn.getResponseCode() == 200) {
				InputStream inStream = conn.getInputStream();// ͨ����������ȡhtml����
				byte[] data = readInputStream(inStream);// �õ�html�Ķ���������
				String html = new String(data, "gb2312");
				return html;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}
}
