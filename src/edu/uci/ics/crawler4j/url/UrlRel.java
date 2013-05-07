package edu.uci.ics.crawler4j.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.snapshot.SnapshotConfig;

public class UrlRel {

	private static final Logger LOG = Logger.getLogger(UrlRel.class);

	private static final Pattern filters = Pattern.compile(
			".*(\\.(mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern staticFilePatterns = Pattern.compile(
			".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * 对html文件中的url进行重定向，使其指向本地资源 TODO 把swf定向到非本地的绝对路径，可以系列reconstructure一下
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
		/*
		 * script中的形式会导致问题: ga.src = ('https:' == document.location.protocol ?
		 * 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
		 */
		String regstr = "((href|src)\\s*=\\s*['\"]\\s*)([^\\s'\">]*)([\\s'\">])";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(rHtml);

		StringBuffer sb = new StringBuffer();
		String wholeMatch = null, urlMatch = null, preMatch = null, postMatch = null;
		while (matchRes.find()) {
			wholeMatch = matchRes.group(0);
			urlMatch = matchRes.group(3);
			preMatch = matchRes.group(1);
			postMatch = matchRes.group(4);
			// for test
			// System.out.println("regex\t" + regstr + "\t\turlMatch:  "
			// + urlMatch);
			// curl是标准化后的url 地址中不得含有中括号 ExtractLinks中类似代码
			String curl = URLCanonicalizer.getCanonicalURL(urlMatch.trim(),
					weburl.getURL());
			WebURL cweburl = new WebURL();
			if (curl != null && curl.startsWith("http://")) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				LOG.debug("curl不规范：\t" + curl + "\t<--\t" + urlMatch);
			}
			/* 判断是否redirect */
			String replacement = null;
			if (shouldRedirect(cweburl, weburl)) {
				String locRelPath = getFullValidDomain(cweburl)
						+ appendFileToPath(cweburl.getPath());
				replacement = preMatch + supdirs + "../" + locRelPath
						+ postMatch;
			} else {
				replacement = wholeMatch;
			}
			/* MARK 使用Matcher.quoteRepalcement()过滤特殊字符 */
			matchRes.appendReplacement(sb,
					Matcher.quoteReplacement(replacement));
		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 重定向js文件中的链接到本地资源，由于js可被嵌入到不同的html中，所以其中的写url操作需要用绝对地址
	 * 
	 * @param content
	 * @param weburl
	 * @param validPath
	 * @return
	 */
	public static String redirectUrlsInJs(String content, WebURL weburl,
			String validPath) {
		String snapshotPageDir = SnapshotConfig.getConf().getSnapshotPage();
		// test http://www.gsdpf.org.cn/wza2012/script/headerwrite.js
		String regstr = "((href|src)\\s*=\\s*[\\\\]?['\"]?\\s*)([^\\s'\">\\\\]*)([\\s'\">\\\\]{2}?)";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(content);

		StringBuffer sb = new StringBuffer();
		String wholeMatch = null, urlMatch = null, preMatch = null, postMatch = null;

		while (matchRes.find()) {
			wholeMatch = matchRes.group(0);
			urlMatch = matchRes.group(3);
			preMatch = matchRes.group(1);
			postMatch = matchRes.group(4);
			// for test
			// System.out.println("regex\t" + regstr + "\t\turlMatch:  "
			// + urlMatch);

			// curl是标准化后的url ExtractLinks中类似代码
			String curl = URLCanonicalizer.getCanonicalURL(urlMatch.trim(),
					weburl.getURL());
			// System.out.println("curl:\t" + curl);
			WebURL cweburl = new WebURL();
			String replacement = null;
			if (curl != null && curl.startsWith("http://")) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				LOG.debug("curl不规范：\t" + curl + "\t<--\t" + urlMatch);
			}
			/* 判断是否redirect */
			if (shouldRedirect(cweburl, weburl)) {
				String locRelPath = getFullValidDomain(cweburl)
						+ appendFileToPath(cweburl.getPath());
				replacement = preMatch + snapshotPageDir + locRelPath
						+ postMatch;
			} else {
				replacement = wholeMatch;
			}
			matchRes.appendReplacement(sb,
					Matcher.quoteReplacement(replacement));
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
		String wholeMatch = null, urlMatch = null, preMatch = null, postMatch = null;

		while (matchRes.find()) {
			wholeMatch = matchRes.group(0);
			urlMatch = matchRes.group(2);
			preMatch = matchRes.group(1);
			postMatch = matchRes.group(3);
			// for test
			// System.out.println("regex\t" + regstr + "\t\turlMatch:  "
			// + urlMatch);
			String curl = URLCanonicalizer.getCanonicalURL(urlMatch.trim(),
					weburl.getURL());

			WebURL cweburl = new WebURL();
			if (curl != null && curl.startsWith("http://")) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				LOG.debug("curl不规范：\t" + curl + "\t<--\t" + urlMatch);
			}
			/* 判断是否redirect */
			String replacement = null;
			if (shouldRedirect(cweburl, weburl)) {
				String locRelPath = getFullValidDomain(cweburl)
						+ appendFileToPath(cweburl.getPath());
				replacement = preMatch + supdirs + "../" + locRelPath
						+ postMatch;
			} else {
				replacement = wholeMatch;
			}
			matchRes.appendReplacement(sb,
					Matcher.quoteReplacement(replacement));
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
		if (!SnapshotConfig.getConf().isCrossSubDomains()
				&& !url.getSubDomain().equals(context.getSubDomain())) {
			return false;
		}

		// port
		if (!SnapshotConfig.getConf().isCrossPorts()
				&& url.getPort() != context.getPort()) {
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
