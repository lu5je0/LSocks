package com.lwwww.proxy;

/**
 * Created by 73995 on 2017/5/6.
 */
public class SocksProxy {
	private enum STAGE {SOCK5_HELLO, SOCKS_ACK, SOCKS_READY}

	private static final int IPv4 = 0x01;
	private static final int DOMAIN_NAME = 0x03;
	private static final int IPv6 = 0x04;

	private String host;
	private int port;
	private STAGE stage;

	public SocksProxy() {
		stage = STAGE.SOCK5_HELLO;
	}

	public byte[] makeResponse(byte[] data) {
		byte[] respData = null;

		switch (stage) {
			case SOCK5_HELLO:
				if (isMine(data)) {
					respData = new byte[]{5, 0};
				} else {
					respData = new byte[]{0, 91};
				}
				stage = STAGE.SOCKS_ACK;
				break;
			case SOCKS_ACK:
				getHostAndPort(data);
				respData = new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0};
				stage = STAGE.SOCKS_READY;
				break;
			case SOCKS_READY:
				break;
		}

		return respData;
	}

	public boolean isMine(byte[] data) {
		return data[0] == 0x5;
	}

	private void getHostAndPort(byte[] bytes) {
		byte type = bytes[3];
		byte[] host = null;
		byte[] port = null;
		switch (type) {
			case IPv4:
				System.out.println("ipv4");
				break;
			case DOMAIN_NAME:
				host = new byte[bytes.length - 7];
				System.arraycopy(bytes, 5, host, 0, host.length);
				port = new byte[2];
				System.arraycopy(bytes, bytes.length - 2, port, 0, port.length);
				break;
			case IPv6:
				System.out.println("ipv6");
				break;
			default:
				System.out.println(type + "unknown");
		}
		if (host != null) {
			this.host = new String(host);
		}
		if (port != null) {
			this.port = (port[0] & 0xFF) * 256 + (port[1] & 0xFF);
		}
//		System.out.println(this.host + " " + this.port);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isReady() {
		return STAGE.SOCKS_READY == stage;
	}
}
