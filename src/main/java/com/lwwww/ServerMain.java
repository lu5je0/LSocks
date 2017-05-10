package com.lwwww;

import com.lwwww.server.ServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by 73995 on 2017/5/9.
 */
public class ServerMain {
	public static void main(String[] args) {
		ServerSocket serverSocket;
		Executor executor = Executors.newCachedThreadPool();

		try {
			serverSocket = new ServerSocket(31562);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		while (true) {
			try {
				Socket client = serverSocket.accept();
				ServerHandler serverHandler = new ServerHandler(client, executor);
				serverHandler.init();
				executor.execute(serverHandler);
				System.out.println("New client");
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
