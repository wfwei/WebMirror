package edu.uci.ics.crawler4j.snapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class UrlSim {
	public static final Logger LOG = Logger.getLogger(UrlSim.class);

	/*
	 * ttp://auto.sohu.com/7/0903/96/column212969687.shtml
	 * ttp://auto.sohu.com/7/1103/61/column216206148.shtml
	 * ttp://auto.sohu.com/s2007/0155/s254359851/index1.shtml
	 * ttp://auto.sohu.com/s2007/5730/s249066842/index2.shtml
	 * 
	 * url1: www.spongeliu.com/xxx/123.html url2: www.spongeliu.com/xxx/456.html
	 * url3: www.spongeliu.com/xxx/abc.html
	 * 
	 * http://v.163.com/zixun/V8GAM7JAP/V925LTCPK.html
	 * http://v.163.com/paike/V8PV2GDG7/V925R5PG1.html
	 * 
	 * http://video.sina.com.cn/ent/s/h/2010-01-10/163961994.shtml
	 * http://video.sina.com.cn/ent/s/h/2012-11-10/163961890.shtml
	 * 是相似的URL，但是与链接3：http://video
	 * .sina.com.cn/ent/m/c/2010-01-10/164661995.shtml，
	 */

	// Protocol : //[user: password]@host[:port]/path /[?query][#fragment]
	private static Pattern UrlPatt = Pattern
			.compile(
					"(?<protocol>.*?)://(?<loginfo>(?<user>.*?):(?<pwd>.*?)@)?(?<host>[^/]+)(?:(?<path>/[^\\?#]*)(?<query>\\?[^#]+)?)?(?<frag>#.*)?",
					Pattern.CASE_INSENSITIVE);

	public static Pattern getUrlPatt() {
		return UrlPatt;
	}

	public static final double DefaulThreshold = 1d;

	public static double calcUrlSim(String urla, String urlb) {
		Matcher amatch = UrlPatt.matcher(urla);
		Matcher bmatch = UrlPatt.matcher(urlb);

		// urla & urlb should both be valid url
		if (amatch.find() && bmatch.find()) {
			// miss -> match.group("protocol");
			String ahost = amatch.group("host");
			String bhost = bmatch.group("host");
			// ahost & bhost should be the same
			if (ahost != null && bhost != null && ahost.equals(bhost)) {
				String apath = amatch.group("path");
				String bpath = bmatch.group("path");
				if (apath != null && bpath != null) {
					if (apath.endsWith("/"))
						apath = apath.substring(0, apath.length() - 1);
					if (bpath.endsWith("/"))
						bpath = bpath.substring(0, bpath.length() - 1);

					String[] apathes = apath.substring(1).split("/");
					String[] bpathes = bpath.substring(1).split("/");
					// path depth should > 1 and length should be equal
					if (apathes.length > 1 && apathes.length == bpathes.length) {
						// path depth 0 should be eaqual
						if (apathes[0].equals(bpathes[0])) {
							// path depthes [1,n-1] should be equal ignore
							// numbers
							boolean ok = true;
							for (int i = 1; i < apathes.length - 1; i++) {
								if (!apathes[i].replaceAll("\\d", "").equals(
										bpathes[i].replaceAll("\\d", "")))
									ok = false;
							}
							if (ok) {
								// last path depth shoud similar in structure
								if (simInStructure(apathes[apathes.length - 1],
										bpathes[bpathes.length - 1]))
									return 1d;
							}
						}
					}
				}
			}
		}
		return 0d;
	}

	/**
	 * 判断字符串a，b是否结构相似
	 * 
	 * 1. 如果有文件类型，应该相同 2. 组成成分（数字，汉字，字母） 3. 成分顺序(暂不考虑)
	 * 
	 * 峨眉山-1002.html vs 双峰山-1231.html 相似
	 * 
	 * slide_4_704_60761.html vs slide_2_702_60714.html 相似
	 * 
	 * 峨眉山-1002.html vs content-1231.html 不相似
	 * 
	 * doc_4_60761.html vs doc_2_702_60714.html 不相似
	 * 
	 */
	private static boolean simInStructure(String a, String b) {
		// 判断后缀名
		int aExtIdx = a.lastIndexOf('.'), bExtIdx = b.lastIndexOf('.');
		if (aExtIdx > 0 && bExtIdx > 0) {
			// extension should be the same
			if (!a.substring(aExtIdx).equals(b.substring(bExtIdx)))
				return false;
			a = a.substring(0, aExtIdx);
			b = b.substring(0, bExtIdx);
		} else if ((aExtIdx > 0 && bExtIdx == -1)
				|| (aExtIdx == -1 && bExtIdx > 0)) {
			// a,b should both contain extensions or both not
			return false;
		}

		// a,b should contain same gradients
		short[] aIngredient = parseIngredient(a);
		short[] bIngredient = parseIngredient(b);
		for (int i = 0; i < 6; i++) {
			if (aIngredient[i] > 0 && bIngredient[i] == 0)
				return false;
			else if (aIngredient[i] == 0 && bIngredient[i] > 0)
				return false;
		}

		// a,b should have similar seperators
		if (aIngredient[4] != aIngredient[4])
			return false;

		return true;
	}

	/**
	 * 计算src中的成分种类数量。
	 * 
	 * 0. 小写字母 1. 大写字母 2. 数字 3. 汉字 4. 分隔符(-_.) 5. 其他字符
	 */
	private static short[] parseIngredient(String src) {
		short[] table = new short[] { 0, 0, 0, 0, 0, 0 };
		for (char c : src.toCharArray()) {
			// System.out.print(c);
			if ((c >= 'a') && (c <= 'z')) {
				// System.out.println(" is small English");
				table[0]++;
			} else if ((c >= 'A') && (c <= 'Z')) {
				// System.out.println(" is big English");
				table[1]++;
			} else if ((c >= '0') && (c <= '9')) {
				// System.out.println(" is Number");
				table[2]++;
			} else if ((c >= 0x4e00) && (c <= 0x9fbb)) {
				// System.out.println(" is Chinese");
				table[3]++;
			} else if (c == '-' || c == '_' || c == '.') {
				// System.out.println(" is Others");
				table[4]++;
			} else {
				table[5]++;
			}

		}
		return table;
	}

}
