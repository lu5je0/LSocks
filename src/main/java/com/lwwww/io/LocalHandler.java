package com.lwwww.io;

import com.lwwww.misc.Constant;
import com.lwwww.proxy.SocksProxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Created by 73995 on 2017/5/9.
 */
public class LocalHandler implements Runnable {
	private Logger logger = Logger.getLogger(LocalHandler.class.getName());

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

	public LocalHandler(Executor executor, Socket local) {
		this.executor = executor;
		this.local = local;
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
						localOut.write(proxy.makeResponse(tmp));
						localOut.flush();
					} else {
						if (!isRemoteGetHostInfo) {
							serverOutStream.write((proxy.getHost() + ":" + proxy.getPort()).getBytes());
							serverOutStream.flush();
							if (serverInStream.read() != 'u') {
								logger.info("Can't send host info to server");
							}
							executor.execute(getRemoteWorker());
							isRemoteGetHostInfo = true;
						}
						sendRemote(buffer, readCount);
					}
				} catch (IOException e) {
					System.out.println("Local socket closed (Read)!");
					break;
				}
			}
			close();
		};
	}

	private Runnable getRemoteWorker() {
		return () -> {
			BufferedOutputStream localOut;
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
			int readCount;

			try {
				localOut = new BufferedOutputStream(local.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			while (true) {
				try {
					readCount = serverInStream.read(buffer);

					if (readCount == -1) {
						throw new IOException("Remote socket closed (Read)!");
					}

					localOut.write(buffer, 0, readCount);
					localOut.flush();
				} catch (IOException e) {
//					e.printStackTrace();
					System.out.println("Remote socket closed (Read)!");
					break;
				}
			}
			close();
		};
	}

	private void sendRemote(byte[] data, int length) throws IOException {
		serverOutStream.write(data, 0, length);
		serverOutStream.flush();
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
		System.out.println(proxy.getHost() + proxy.getPort() + " closed");
	}
}
