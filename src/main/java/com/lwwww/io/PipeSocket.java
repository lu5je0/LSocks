package com.lwwww.io;

import com.lwwww.Constant;
import com.lwwww.proxy.SocksProxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by 73995 on 2017/5/9.
 */
public class PipeSocket implements Runnable {
	private Logger logger = Logger.getLogger(PipeSocket.class.getName());

	private Executor executor;
	private Socket local;
	private Socket remote;
	private SocksProxy proxy;

	public void init() {
		Executor executor = Executors.newFixedThreadPool(5);
		executor.execute(getLocalWorker());
	}

	public PipeSocket(Executor executor, Socket local) {
		this.executor = executor;
		this.local = local;
		proxy = new SocksProxy();
	}

	@Override
	public void run() {
		executor.execute(getLocalWorker());
	}

	private Runnable getLocalWorker() {
		return () -> {
			BufferedInputStream localIn;
			BufferedOutputStream localOut;
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
			byte[] tmp;
			int readCount;

			try {
				localIn = new BufferedInputStream(local.getInputStream());
				localOut = new BufferedOutputStream(local.getOutputStream());
			} catch (IOException e) {
				logger.info(e.toString());
				e.printStackTrace();
				return;
			}
			while (true) {
				try {
					readCount = localIn.read(buffer);
					if (readCount == -1) {
						throw new IOException("Local socket closed (Read)!");
					}
					//init proxy
					if (!proxy.isReady()) {
						tmp = new byte[readCount];
						System.arraycopy(buffer, 0, tmp, 0, readCount);
						localOut.write(proxy.makeResponse(tmp));
						localOut.flush();
						logger.info("Connected to " + proxy.getHost() + ":" + proxy.getPort());
					} else {
						sendRemote(buffer, readCount);
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		};
	}

	private Runnable getRemoteWorker() {
		return () -> {
			//todo
		};
	}

	private void sendRemote(byte[] data, int length) {
		System.out.println(new String(data));
	}
}
