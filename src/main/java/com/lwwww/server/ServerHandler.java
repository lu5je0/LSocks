package com.lwwww.server;

import com.lwwww.Constant;
import com.lwwww.proxy.SocksProxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;

/**
 * Created by 73995 on 2017/5/9.
 */
public class ServerHandler implements Runnable {
	private Socket client;
	//host
	private Socket remote;
	private Executor executor;
	private SocksProxy proxy;

	public ServerHandler(Socket client, Executor executor) {
		this.client = client;
	}

	@Override
	public void run() {
		executor.execute(getClientWorker());
		executor.execute(getRemoteWorker());
	}

	private Runnable getClientWorker() {
		return () -> {
			int readCount;
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
			BufferedInputStream in;
			BufferedOutputStream remoteOut;
			byte[] tmp;
			try {
				in = new BufferedInputStream(client.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			//到 host 的连接
			try {
				remoteOut = new BufferedOutputStream(remote.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			while (true) {
				try {
					readCount = in.read(buffer);
					if (readCount == -1) {
						throw new IOException("Client can't read");
					}

					tmp = new byte[readCount];
					System.arraycopy(buffer, 0, tmp, 0, readCount);
					remoteOut.write(tmp);
					remoteOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		};
	}

	private Runnable getRemoteWorker() {
		return () -> {
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
			BufferedOutputStream clientOut;
			BufferedInputStream remoteIn;
			byte[] tmp;
			int readCount;

			try {
				clientOut = new BufferedOutputStream(client.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			try {
				remoteIn = new BufferedInputStream(remote.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			while (true) {
				try {
					readCount = remoteIn.read(buffer);
					if (readCount == -1) {
						throw new IOException("Remote socket closed! (read)");
					}

					tmp = new byte[readCount];
					System.arraycopy(buffer, 0, tmp, 0, readCount);
					clientOut.write(tmp);
					clientOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
}
