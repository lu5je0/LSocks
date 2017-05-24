package com.lwwww;

import com.lwwww.crypt.CryptFactory;
import com.lwwww.crypt.ICrypt;
import com.lwwww.io.LocalHandler;
import org.apache.log4j.Logger;

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
		Logger logger = Logger.getLogger(Main.class);
		Executor executor = Executors.newCachedThreadPool();
		ServerSocket serverSocket = new ServerSocket(1082);
		ICrypt crypt;
		try {
			crypt = CryptFactory.getCrypt("AES", "56231");
		} catch (Exception e) {
			logger.error("Crypto can not found!");
			return;
		}
		while (true) {
			Socket socket = serverSocket.accept();
			LocalHandler localHandler = new LocalHandler(executor, socket, crypt);
			executor.execute(localHandler);
		}
	}
}
