package com.lwwww.server;

import com.lwwww.misc.Stream2Bytes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by 73995 on 2017/5/9.
 */
public class ServerHandler {
	public byte[] makeResponse(String host, int port, byte[] bytes) throws IOException, InterruptedException {
		Socket socket = new Socket(host, port);
		BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
		out.write(bytes);
		out.flush();
		byte[] resp = Stream2Bytes.readRemoteStream(in);
		return resp;
	}
}
