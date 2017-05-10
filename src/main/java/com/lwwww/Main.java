package com.lwwww;

import com.lwwww.io.PipeSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by 73995 on 2017/5/6.
 */
public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		Executor executor = Executors.newCachedThreadPool();
		ServerSocket serverSocket = new ServerSocket(1082);
		while (true) {
			Socket socket = serverSocket.accept();
			PipeSocket pipeSocket = new PipeSocket(executor, socket);
			pipeSocket.init();
			executor.execute(pipeSocket);
		}
	}
}
