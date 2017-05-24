package com.lwwww.crypt;

/**
 * Created by 73995 on 2017/5/20.
 */
public interface ICrypt {
	byte[] encrypt(byte[] bytes);
	byte[] decrypt(byte[] bytes);
}
