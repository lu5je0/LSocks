package com.lwwww.crypt;

/**
 * Created by 73995 on 2017/5/20.
 */
public class CryptFactory {
	public static ICrypt getCrypt(String cryptName, String password) throws Exception {
		if (cryptName.equals("AES")) {
			return new AesCrypt(password);
		}
		else {
			throw new Exception("No such crypto");
		}
	}
}
