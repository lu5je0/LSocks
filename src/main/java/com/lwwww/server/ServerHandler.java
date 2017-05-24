package com.lwwww.server;

import com.lwwww.crypt.ICrypt;
import com.lwwww.misc.Constant;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Executor;

/**
 * Created by 73995 on 2017/5/9.
 */
public class ServerHandler implements Runnable {
	private Socket client;
	//host
	private Socket remote;
	private Executor executor;
	private Stage stage;
	private String host;
	private int port;
	private boolean isClosed;
	private BufferedInputStream clientIn;
	private BufferedOutputStream clientOut;
	private BufferedOutputStream remoteOut;
	private BufferedInputStream remoteIn;
	private int ID;
	private Logger logger = Logger.getLogger(ServerHandler.class);
	private ICrypt crypt;

	private enum Stage {HELLO, READY}

	public ServerHandler(Socket client, Executor executor, ICrypt crypt) throws IOException {
		this.client = client;
		this.executor = executor;
		this.crypt = crypt;
		init();
	}

	private void init() {
		stage = Stage.HELLO;
		isClosed = false;
		ID = Math.abs(new Random().nextInt(10000));
		try {
			clientIn = new BufferedInputStream(client.getInputStream());
			clientOut = new BufferedOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		executor.execute(getClientWorker());
	}

	private Socket getRemoteSocket(byte[] data) {
		String hostInfo = new String(data);
		int tag = hostInfo.lastIndexOf(':');
		port = Integer.parseInt(hostInfo.substring(tag + 1, hostInfo.length()));
		host = hostInfo.substring(0, tag);
		stage = Stage.READY;
		try {
			remote = new Socket(host, port);
		} catch (IOException e) {
			logger.info("Can't open remote socket!");
		}
		//通知client，已经获取host_info
		try {
			clientOut.write('u');
			clientOut.flush();
		} catch (IOException e) {
			logger.error("12");
		}
		System.out.println("Connected to " + host + ":" + port);
		return remote;
	}

	private Runnable getClientWorker() {
		return () -> {
			int readCount;
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
			byte[] tmp;

			//初始化remote连接
			if (stage == Stage.HELLO) {
				try {
					readCount = clientIn.read(buffer);
					if (readCount == -1) {
						throw new IOException("Client socket closed! (read)");
					}
					tmp = new byte[readCount];
					System.arraycopy(buffer, 0, tmp, 0, readCount);
					remote = getRemoteSocket(crypt.decrypt(tmp));
					if (remote == null) {
						close();
						return;
					}
					executor.execute(getRemoteWorker());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				stage = Stage.READY;
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
					readCount = clientIn.read(buffer);
					if (readCount == -1) {
						throw new IOException("Client socket closed! (write)");
					}

					tmp = new byte[readCount];
					System.arraycopy(buffer, 0, tmp, 0, readCount);
					sendRemote(tmp);
				} catch (IOException e) {
					logger.info("Client socket closed! (write)");
					break;
				}
			}

			close();
		};
	}

	private Runnable getRemoteWorker() {
		return () -> {
			byte[] buffer = new byte[Constant.BUFFER_LENGTH];
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
					logger.info("Remote socket closed! (read)");
					break;
				}
			}
			close();
		};
	}

	private void sendRemote(byte[] bytes) {
		try {
			remoteOut.write(crypt.decrypt(bytes));
			remoteOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {
		if (isClosed) {
			return;
		}
		isClosed = true;

		if (remote != null) {
			try {
				remote.shutdownOutput();
				remote.shutdownInput();
				remote.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (client != null) {
			try {
				client.shutdownInput();
				client.shutdownOutput();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(host + ":" + port + " closed");
		}
	}
}
