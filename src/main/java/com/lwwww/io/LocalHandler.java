package com.lwwww.io;

import com.lwwww.crypt.ICrypt;
import com.lwwww.misc.Constant;
import com.lwwww.proxy.SocksProxy;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;

/**
 * Created by 73995 on 2017/5/9.
 */
public class LocalHandler implements Runnable {
	private Logger logger = Logger.getLogger(LocalHandler.class);

	private Executor executor;
	private Socket local;
	private Socket remote;
	private BufferedOutputStream serverOutStream;
	private BufferedInputStream serverInStream;
	private BufferedInputStream localIn;
	private BufferedOutputStream localOut;
	private SocksProxy proxy;
	private boolean isClosed;
	private boolean isRemoteGetHostInfo = false;
	private ICrypt crypt;

	private void init() {
		proxy = new SocksProxy();
		isClosed = false;
		try {
			remote = new Socket("127.0.0.1", 31562);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			serverOutStream = new BufferedOutputStream(remote.getOutputStream());
			serverInStream = new BufferedInputStream(remote.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			localIn = new BufferedInputStream(local.getInputStream());
			localOut = new BufferedOutputStream(local.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public LocalHandler(Executor executor, Socket local, ICrypt crypt) {
		this.executor = executor;
		this.local = local;
		this.crypt = crypt;
		init();
	}

	@Override
	public void run() {
		executor.execute(getLocalWorker());
	}

	private Runnable getLocalWorker() {
		return () -> {
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
			byte[] tmp;
			int readCount;

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
						sendLocal(proxy.makeResponse(tmp));
					} else {
						if (!isRemoteGetHostInfo) {
							sendServer((proxy.getHost() + ":" + proxy.getPort()).getBytes());
							if (serverInStream.read() != 'u') {
								logger.info("Can't send host info to server");
							}
							executor.execute(getRemoteWorker());
							isRemoteGetHostInfo = true;
						}

						sendServer(buffer, readCount);
					}
				} catch (IOException e) {
					logger.info("Local socket closed (Read)!");
					break;
				}
			}
			close();
		};
	}

	private Runnable getRemoteWorker() {
		return () -> {
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
			int readCount;

			while (true) {
				try {
					readCount = serverInStream.read(buffer);

					if (readCount == -1) {
						throw new IOException("Remote socket closed (Read)!");
					}

					sendLocal(buffer, readCount);
				} catch (IOException e) {
					logger.info("Remote socket closed (Read)!");
					break;
				}
			}
			close();
		};
	}

	private void sendLocal(byte[] data, int length) throws IOException {
		localOut.write(data, 0, length);
		localOut.flush();
	}

	private void sendLocal(byte[] data) throws IOException {
		sendLocal(data, data.length);
	}

	private void sendServer(byte[] data, int length) throws IOException {
		byte[] tmp = new byte[length];
		System.arraycopy(data, 0, tmp, 0, length);
		byte[] crypto = crypt.encrypt(tmp);
		serverOutStream.write(crypto, 0, crypto.length);
		serverOutStream.flush();
	}

	private void sendServer(byte[] data) throws IOException {
		sendServer(data, data.length);
	}

	private void close() {
		if (isClosed) {
			return;
		}
		isClosed = true;

		try {
			local.shutdownInput();
			local.shutdownOutput();
			local.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			remote.shutdownOutput();
			remote.shutdownInput();
			remote.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info(proxy.getHost() + proxy.getPort() + " closed");
	}
}
