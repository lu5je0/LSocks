package com.lwwww;

import com.lwwww.crypt.CryptFactory;
import com.lwwww.crypt.ICrypt;
import com.lwwww.server.ServerHandler;
import org.apache.log4j.Logger;

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
		Logger logger = Logger.getLogger(ServerMain.class);
		ServerSocket serverSocket;
		Executor executor = Executors.newCachedThreadPool();
		ICrypt crypt;
		try {
			crypt = CryptFactory.getCrypt("AES", "56231");
		} catch (Exception e) {
			logger.error("Crypto can not found!");
			return;
		}

		try {
			serverSocket = new ServerSocket(31562);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		while (true) {
			try {
				Socket client = serverSocket.accept();
				ServerHandler serverHandler = new ServerHandler(client, executor, crypt);
				executor.execute(serverHandler);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
