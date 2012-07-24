package edu.zju.wfwei.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.url.WebURL;
import edu.zju.wfwei.snapshot.Config;

public class UrlRel {

	/**
	 * 对站内链接进行redirect，同时考虑子域名
	 * 
	 * @param rHtml
	 * @param subDomain
	 * @param domain
	 * @param path
	 * @return
	 */
	public static String redirectUrls(String rHtml, String subDomain,
			String domain, String path) {
		String supdirs = "./";
		int idx = -1;
		while ((idx = path.indexOf('/', idx + 1)) != -1) {
			if (idx > 0)
				supdirs += "../";
		}
		// 找到以href或者src开头并且之后的双引号中，以http地址开头或/开头的字符串
		// String regstr =
		// "(href|src)\\s*=\\s*\"\\s*(/|((http://)([^/\"]+[/]?)))([^\"]*\")";
		String regstr = "((href|src)\\s*=\\s*['\"]?\\s*)([^\\s'\">]*)([\\s'\">])";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(rHtml);

		StringBuffer sb = new StringBuffer();
		while (matchRes.find()) {
			// TODO for test
			// System.out.println("regex\t" + regstr);
			// for (int i = 0; i <= matchRes.groupCount(); i++) {
			// System.out.println("group" + i + ":\t" + matchRes.group(i));
			// }
			//
			String url = specifyFile(matchRes.group(3)).trim();
			if (url.startsWith("http://")) {
				
				if (!url.endsWith("/") && !url.substring(7).contains("/")) {
					// 规范url http://www.url.com==>http://www.url.com/
					url = url + "/";
				}
				WebURL newUrl = new WebURL();
				newUrl.setURL(url);
				String locPath = newUrl.getSubDomain() + "."
						+ newUrl.getDomain() + newUrl.getPath();
				//System.out.println("locPath\t" + locPath);

				if (url.contains(domain)) {
					if (!Config.isCrossSubDomains() && !url.contains(subDomain)) {
						matchRes.appendReplacement(sb, matchRes.group());
					} else {
						matchRes.appendReplacement(sb, matchRes.group(1)
								+ supdirs + "../" + locPath + matchRes.group(4));
					}
					continue;
				}
				if (url.matches(".*?(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$")) {
					// http://www.newdomain.com.cn/abd/dfsd.jpg
					matchRes.appendReplacement(sb, matchRes.group(1) + supdirs
							+ "../" + locPath + matchRes.group(4));
				} else {
					matchRes.appendReplacement(sb, matchRes.group());
				}
				continue;
			}
			matchRes.appendReplacement(sb, matchRes.group(1) + supdirs + url
					+ matchRes.group(4));

		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 判断路径是否包含了文件名，比如：···/path/to/file/a.html 直接返回；···/path/to/file
	 * 或···/path/to/file/ 返回···/path/to/file/index.html；
	 * 
	 * path的几种情况： 1. host/a/b/index.html 不用改 2. host/a/b/ -->
	 * host/a/b/index.html 3. host/a/b ---> host/a/b/index.html 4.
	 * host/a/b/page.asp?para=1 --> host/a/b/page.asp
	 * 
	 * 
	 * @param path
	 * @return
	 */
	public static String specifyFile(String path) {
		if (null == path) {
			return "/index.html";
		}
		// 2
		if (path.endsWith("/"))
			return path + "index.html";
		// 4
		if (path.contains("?")) {
			return replaceInvalidChar(path) + "/index.html";
		}
		// 3
		if (path.lastIndexOf('.') <= path.lastIndexOf('/')) {
			return path + "/index.html";
		}
		// 1
		return path;
	}

	private static String replaceInvalidChar(String str) {
		return str.replaceAll("[:?*\"<>|\\\\]", "REPLACEMENT");
	}

	public static void main(String args[]) {
		// String rHtml = null, path = "blog/main.html";
		// rHtml =
		// "href = \" /abc/def.html\"   src = \" http://baike.cdpsn.org.cn/123/456\" ";
		// System.out.println(redirectUrls(rHtml, "www", "cdpsn.org.cn", path));
		String invalidString = "\\:*?\"<>";
		System.out.println(replaceInvalidChar(invalidString));
	}
}