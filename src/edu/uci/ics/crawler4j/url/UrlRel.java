package edu.uci.ics.crawler4j.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.util.Config;
import edu.uci.ics.crawler4j.util.Debug;

public class UrlRel {

	private static final Pattern filters = Pattern.compile(
			".*(\\.(mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern staticFilePatterns = Pattern.compile(
			".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * 对html文件中的url进行重定向，使其指向本地资源
	 * 
	 * @param rHtml
	 *            待修改的网页源码
	 * @param subDomain
	 *            未经替换的子域名，据此判断是否在统一子域名下
	 * @param domain
	 *            未经替换的域名，据此判断是否为统一站点
	 * @param validPath
	 *            本地存储的path，据此判断深度，得到supdirs
	 * @return
	 */
	public static String redirectUrlsInHtml(String rHtml, WebURL weburl,
			String validPath) {
		String supdirs = "./";
		int idx = -1;
		while ((idx = validPath.indexOf('/', idx + 1)) != -1) {
			if (idx > 0)
				supdirs += "../";
		}
		// MARK 找到html中所有链接的正则表达式
		/*
		 * script中的形式会导致问题: ga.src = ('https:' == document.location.protocol ?
		 * 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
		 */
		String regstr = "((href|src)\\s*=\\s*['\"]\\s*)([^\\s'\">]*)([\\s'\">])";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(rHtml);

		StringBuffer sb = new StringBuffer();
		while (matchRes.find()) {
			// System.out.println("regex\t" + regstr);
			// for (int i = 0; i <= matchRes.groupCount(); i++) {
			// System.out.println("group" + i + ":\t" + matchRes.group(i));
			// }
			// curl是标准化后的url 地址中不得含有中括号 ExtractLinks中类似代码
			String curl = URLCanonicalizer.getCanonicalURL(matchRes.group(3)
					.trim(), weburl.getURL());
			WebURL cweburl = new WebURL();
			if (curl != null && curl.startsWith("http://")) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				Debug.checkLogger.debug("curl不规范：\t" + curl + "\t<--\t"
						+ matchRes.group(3));
			}
			/* 判断是否redirect */
			if (shouldRedirect(cweburl, weburl)) {
				String locRelPath = getFullValidDomain(cweburl)
						+ appendFileToPath(cweburl.getPath());

				matchRes.appendReplacement(sb, matchRes.group(1) + supdirs
						+ "../" + locRelPath + matchRes.group(4));
			} else {
				matchRes.appendReplacement(sb, matchRes.group());
			}
		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	/**
	 * TODO 链接重定向的地址不能为相对地址，相对的地址在不断变化。。。
	 * 
	 * @param content
	 * @param weburl
	 * @param validPath
	 * @return
	 */
	public static String redirectUrlsInJs(String content, WebURL weburl,
			String validPath) {
		String supdirs = "./";
		int idx = -1;
		while ((idx = validPath.indexOf('/', idx + 1)) != -1) {
			if (idx > 0)
				supdirs += "../";
		}
		// 找到所有连接
		String regstr = "((href|src)\\s*=\\s*[\\\\]?['\"]?\\s*)([^\\s'\">\\\\]*)([\\s'\">\\\\]{2}?)";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(content);

		StringBuffer sb = new StringBuffer();
		while (matchRes.find()) {
//			System.out.println("regex\t" + regstr);
//			for (int i = 0; i <= matchRes.groupCount(); i++) {
//				System.out.println("group" + i + ":\t" + matchRes.group(i));
//			}
			// curl是标准化后的url 地址中不得含有中括号 ExtractLinks中类似代码，当group(3)是空的时候如何处理
			String curl = URLCanonicalizer.getCanonicalURL(matchRes.group(3)
					.trim(), weburl.getURL());
//			System.out.println("curl:\t" + curl);
			WebURL cweburl = new WebURL();
			String replacement = null;
			if (curl != null && curl.startsWith("http://")) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				Debug.checkLogger.debug("curl不规范：\t" + curl + "\t<--\t"
						+ matchRes.group(3));
			}
			/* 判断是否redirect */
			if (shouldRedirect(cweburl, weburl)) {
				String locRelPath = getFullValidDomain(cweburl)
						+ appendFileToPath(cweburl.getPath());
				replacement = matchRes.group(1) + supdirs + "../" + locRelPath
						+ matchRes.group(4);
			} else {
				replacement = matchRes.group();
			}
			// MARK 四个反斜杠，我擦
			matchRes.appendReplacement(sb,
					replacement.replaceAll("\\\\", "\\\\\\\\"));
		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	public static String redirectUrlsInCss(String content, WebURL weburl,
			String validPath) {
		String supdirs = "./";
		int idx = -1;
		while ((idx = validPath.indexOf('/', idx + 1)) != -1) {
			if (idx > 0)
				supdirs += "../";
		}
		// url('/images/email.png_WH_1331724647')
		String regstr = "(url\\s*[(]['\"]?)([^)'\"]*)(['\"]?[)])";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(content);

		StringBuffer sb = new StringBuffer();
		while (matchRes.find()) {
			// System.out.println("regex\t" + regstr);
			// for (int i = 0; i <= matchRes.groupCount(); i++) {
			// System.out.println("group" + i + ":\t" + matchRes.group(i));
			// }
			// curl是标准化后的url 地址中不得含有中括号 ExtractLinks中类似代码
			String curl = URLCanonicalizer.getCanonicalURL(matchRes.group(2)
					.trim(), weburl.getURL());
			WebURL cweburl = new WebURL();
			if (curl != null && curl.startsWith("http://")) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				Debug.checkLogger.debug("curl不规范：\t" + curl + "\t<--\t"
						+ matchRes.group(1));
			}
			/* 判断是否redirect */
			if (shouldRedirect(cweburl, weburl)) {
				String locRelPath = getFullValidDomain(cweburl)
						+ appendFileToPath(cweburl.getPath());

				matchRes.appendReplacement(sb, matchRes.group(1) + supdirs
						+ "../" + locRelPath + matchRes.group(3));
			} else {
				matchRes.appendReplacement(sb, matchRes.group());
			}
		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 为url中的path添加文件，判断路径是否包含了文件名，比如：···/path/to/file/a.html
	 * 直接返回；···/path/to/file 或···/path/to/file/ 返回···/path/to/file/index.html；
	 * 
	 * path的几种情况： 1. host/a/b/index.html 不用改 2. host/a/b/ -->
	 * host/a/b/index.html 3. host/a/b ---> host/a/b/index.html 4.
	 * host/a/b/page.asp?para=1 --> host/a/b/page.asp
	 * 
	 * 
	 * @param path
	 * @return
	 */
	public static String appendFileToPath(String path) {
		path = replaceInvalidChar(path);

		if (null == path) {
			return "/index.html";
		}

		if (path.endsWith("/")) {
			return path + "index.html";
		}

		if (path.lastIndexOf('.') <= path.lastIndexOf('/')) {
			return path + "/index.html";
		}

		return path;
	}

	/**
	 * 将url合法化：包括获取端口，对特殊字符转化
	 * 
	 * @param weburl
	 * @return
	 */
	public static String getFullValidDomain(WebURL weburl) {
		String fullValidDomain = null;
		fullValidDomain = (weburl.getSubDomain() == "") ? (weburl.getDomain())
				: (weburl.getSubDomain() + "." + weburl.getDomain());
		if (weburl.getPort() != 80) {
			fullValidDomain += ":" + weburl.getPort();
		}
		fullValidDomain = replaceInvalidChar(fullValidDomain);
		return fullValidDomain;
	}

	/**
	 * 将str中的:?*"<>|\8个符号进行替换 MARK 使用特殊字符要注意，开始用的是#号，但是#出现在url中会以数字代替，就是一个bug
	 * 
	 * @param str
	 * @return
	 */
	private static String replaceInvalidChar(String str) {
		return str.replaceAll("\\?", "_WH_").replaceAll(":", "_MH_")
				.replaceAll("[*\"<>|\\\\]", "_QT_");
	}

	/**
	 * 和SnapshotCrawler中的shouldVisit方法一致
	 * 
	 * @param url
	 * @return
	 */
	private static boolean shouldRedirect(WebURL url, WebURL context) {
		String href = url.getURL().toLowerCase();
		// ignore radio video etc...
		if (filters.matcher(href).matches()) {
			return false;
		}

		// download static files like:pic, js, css
		if (staticFilePatterns.matcher(href).matches()) {
			return true;
		}

		// sub domain
		if (!Config.isCrossSubDomains()
				&& !url.getSubDomain().equals(context.getSubDomain())) {
			return false;
		}

		// port
		if (!Config.isCrossPorts() && url.getPort() != context.getPort()) {
			return false;
		}

		if (url.getDomain().equals(context.getDomain())) {
			return true;
		}
		return false;

	}

	public static void main(String args[]) {
		// String rHtml = null, path = "blog/main.html";
		// rHtml =
		// "href = \" /abc/def.html\"   src = \" http://baike.cdpsn.org.cn/123/456\" ";
		// System.out.println(redirectUrls(rHtml, "www", "cdpsn.org.cn", path));
		String invalidString = "\\:*?\"<>";
		System.out.println(replaceInvalidChar(invalidString));
		// WebURL weburl = new WebURL();
		// // ip不能解析
		// //
		// weburl.setURL("http://www.scdpf.org.cn:8080/Content/ggtz/ggtz1.html");
		// weburl.setURL("http://10.214.43.12:8080/a/b/c");
		// System.out.println(weburl.getPath());
		// System.out.println(weburl.getShortPath());
	}
}
