package com.lwwww.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 73995 on 2017/5/6.
 */
public class Stream2Bytes {
	public static byte[] readStream(InputStream in) throws InterruptedException {
		int count = 0;
		while (count == 0) {
			try {
				count = in.available();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] bytes = new byte[count];
		int read = 0;
		while (read != count) {
			try {
				read += in.read(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}

	public static byte[] readRemoteStream(InputStream in) throws InterruptedException, IOException {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int rc = 0;
		while ((rc = in.read(buff, 0, 1024)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] bytes = swapStream.toByteArray();
		return bytes;
	}
}
