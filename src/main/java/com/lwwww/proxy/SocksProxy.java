package com.lwwww.proxy;

import java.util.Arrays;

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
		byte[] hostBytes = null;
		byte[] portBytes = null;
		switch (type) {
			case IPv4:
				System.out.println("ipv4");
				break;
			case DOMAIN_NAME:
				hostBytes = new byte[bytes.length - 7];
				System.arraycopy(bytes, 5, hostBytes, 0, hostBytes.length);
				portBytes = new byte[2];
				System.arraycopy(bytes, bytes.length - 2, portBytes, 0, portBytes.length);
				break;
			case IPv6:
				System.out.println("ipv6");
				break;
			default:
				System.out.println(type + "unknown");
		}
		if (hostBytes != null) {
			host = new String(hostBytes);
		}
		if (portBytes != null) {
			this.port = (portBytes[0] & 0xFF) * 256 + (portBytes[1] & 0xFF);
		}
		System.out.println("Connected to " + host + ":" + port);
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
