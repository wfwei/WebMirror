package edu.uci.ics.crawler4j.tests;

import java.util.regex.Matcher;
import edu.uci.ics.crawler4j.snapshot.UrlSim;

public class UrlSimTest {
	public static void testUrlPatt() {
		String[] urls = {
				"http://auto.sohu.com/7/0903/96/column212969687.shtml#abc",
				"https://www.google.com.hk/search?q=named+group+regex+java&oq=named+group+regex+java&aqs=chrome.0.57.5777j0&sourceid=chrome&ie=UTF-8#abc",
				"http://user_wfwei:pwd_wfwei@wfwei.github.io/posts/regex/#abc",
				"ftp://wfw:wfw@10.214.52.10:9999/?raw" };
		Matcher match;
		for (String url : urls) {
			match = UrlSim.getUrlPatt().matcher(url);
			if (match.find()) {
				System.out
						.format("-----------------------------\n"
								+ "url:\t\t%s\nprotocol:\t%s\nloginfo:\t%s\nhost:\t\t%s\n"
								+ "path:\t\t%s\nquery:\t\t%s\nfragment:\t%s\n\n",
								match.group(), match.group("protocol"),
								match.group("loginfo"), match.group("host"),
								match.group("path"), match.group("query"),
								match.group("frag"));
			}
		}
	}

	public static void testCalcUrlSim() {
		String[] simUrls = {
				"http://news.163.com/13/0706/19/934EKSC300014JB5.html;http://news.163.com/13/0706/05/932VPFP200011229.html",
				"http://auto.sohu.com/s2007/0155/s254359851/index1.shtml;http://auto.sohu.com/s2007/5730/s249066842/index2.shtml",
				"http://auto.sohu.com/7/0903/96/column212969687.shtml;http://auto.sohu.com/7/1103/61/column216206148.shtml",
				"http://www.spongeliu.com/xxx/123.html;http://www.spongeliu.com/xxx/456.html" };

		String[] notSimUrls = {
				"http://v.163.com/zixun/V8GAM7JAP/V925LTCPK.html;http://v.163.com/paike/V8PV2GDG7/V925R5PG1.html",
				"http://auto.sohu.com/s2007/0155/s254359851/index1.shtml;http://auto.sohu.com/7/1103/61/column216206148.shtml",
				"http://www.spongeliu.com/xxx/123.html;http://www.spongeliu.com/xxx/abc.html" };

		for (String simUrl : simUrls) {
			String[] urls = simUrl.split(";");
			double sim = UrlSim.calcUrlSim(urls[0], urls[1]);
			System.out.println(sim == 1d);
		}
		for (String simUrl : notSimUrls) {
			String[] urls = simUrl.split(";");
			double sim = UrlSim.calcUrlSim(urls[0], urls[1]);
			System.out.println(sim == 0d);
		}
	}

	public static void main(String[] args) {
		// countIngredient("Wfw123,，王峰伟-=._");
		// testUrlPatt();
		testCalcUrlSim();
	}
}
