/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package net.redis.distributed.lock.util;

import java.util.Arrays;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

public class JedisUtils {
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = Protocol.DEFAULT_PORT;
	public static final int DEFAULT_TIMEOUT = Protocol.DEFAULT_TIMEOUT;

	private static final String OK_CODE = "OK";
	private static final String OK_MULTI_CODE = "+OK";

	/**
	 * 判断 是 OK 或 +OK.
	 */
	public static boolean isStatusOk(String status) {
		return (status != null) && (OK_CODE.equals(status) || OK_MULTI_CODE.equals(status));
	}
	
	public static boolean setnxex(Jedis jedis, String key, String value, long milliseconds){
		String result = jedis.set(key, value, "NX", "PX", milliseconds);
		return JedisUtils.isStatusOk(result);
	}
	
	public static Object evalScript(Jedis jedis, String script, String[] keys, String[] args){
		return jedis.eval(script, Arrays.asList(keys), Arrays.asList(args));
	}

	/**
	 * 退出然后关闭Jedis连接。
	 */
	public static void closeJedis(Jedis jedis) {
		if (jedis.isConnected()) {
			try {
				try {
					jedis.quit();
				} catch (Exception e) {
				}
				jedis.disconnect();
			} catch (Exception e) {
			}
		}
	}
}
