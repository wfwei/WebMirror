package edu.uci.ics.crawler4j.tests;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class FreeTest {
	public static void main(String[] args) throws Exception {
		String a = "http://www.baidu.com/辅助产品";
		System.out.println(a);
		String encoded = URLEncoder.encode(a, "utf-8");
		System.out.println(encoded);
		String decoded = URLDecoder.decode(encoded, "utf-8");
		System.out.println(decoded);
	}
}
