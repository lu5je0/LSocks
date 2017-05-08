package com.lwwww;

import com.lwwww.misc.Stream2Bytes;
import com.lwwww.proxy.SocksProxy;
import com.lwwww.server.ServerHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 73995 on 2017/5/6.
 */
public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket serverSocket = new ServerSocket(1082);
		Socket socket = serverSocket.accept();
		BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

		SocksProxy socksProxy = new SocksProxy();
		out.write(socksProxy.makeResponse(Stream2Bytes.readStream(in)));
		out.flush();
		out.write(socksProxy.makeResponse(Stream2Bytes.readStream(in)));
		out.flush();


		ServerHandler serverHandler = new ServerHandler();
		while (true) {
			byte[] rep = Stream2Bytes.readStream(in);
			System.out.println(new String(rep));
			byte[] resp =  serverHandler.makeResponse(socksProxy.getHost(), socksProxy.getPort(), rep);
			System.out.println(new String(resp, "gb2312"));
			out.write(resp);
			out.flush();
		}
	}
}
