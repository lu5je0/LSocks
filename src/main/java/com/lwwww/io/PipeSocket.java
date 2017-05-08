package com.lwwww.io;

import com.lwwww.proxy.SocksProxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by 73995 on 2017/5/9.
 */
public class PipeSocket {
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

	private Runnable getLocalWorker() {
		return () -> {
			BufferedInputStream localIn;
			BufferedOutputStream localOut;
			byte[] buffer = new byte[8192];
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
					if (proxy.isReady()) {
						sendRemote(buffer, readCount);
					} else {
						tmp = new byte[readCount];
						System.arraycopy(buffer, 0, tmp, 0, readCount);
						localOut.write(proxy.makeResponse(tmp));
						localOut.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
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
