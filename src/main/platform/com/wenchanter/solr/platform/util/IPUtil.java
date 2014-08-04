package com.wenchanter.solr.platform.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

public class IPUtil {

	public static String ip = null;

	static {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress().toString();//获得本机IP
			//			String name = addr.getHostName().toString();//获得本机名称
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static String getLocalIP() {
		return ip;
	}

	public static String getClinetIP(HttpServletRequest request) {

		//获取ip
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		//截取第一个ip
		if (ip == null || (!ip.contains(","))) {
			return ip;
		} else {
			String[] ips = ip.split(",");
			return ips[0];
		}
	}

	public static void main(String[] args) {
		System.out.println(getLocalIP());
	}
}
