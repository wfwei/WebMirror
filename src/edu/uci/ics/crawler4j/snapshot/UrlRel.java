package edu.uci.ics.crawler4j.snapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

public class UrlRel {

	private static final Logger LOG = Logger.getLogger(UrlRel.class);

	private static final Pattern filters = Pattern.compile(
			".*(\\.(mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern staticFilePatterns = Pattern.compile(
			".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$",
			Pattern.CASE_INSENSITIVE);

	// urlsInHtml urlsInJs urlsInCss 查找文件中的url，注意括号
	private static final Pattern urlsInHtml = Pattern.compile(
			"((href|src)\\s*=\\s*['\"]\\s*)([^\\s'\">]*)([\\s'\">])",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern urlsInJs = Pattern
			.compile(
					"((href|src)\\s*=\\s*[\\\\]?['\"]?\\s*)([^\\s'\">\\\\]*)([\\s'\">\\\\]{2}?)",
					Pattern.CASE_INSENSITIVE);
	private static final Pattern urlsInCss = Pattern.compile(
			"((url)\\s*[(]['\"]?)([^)'\"]*)(['\"]?[)])",
			Pattern.CASE_INSENSITIVE);

	public static String redirectUrls4Server(String page, String serverRoot,
			String contentType) {
		Matcher matchRes = null;
		if (contentType.contains("html"))
			matchRes = urlsInHtml.matcher(page);
		else if (contentType.contains("javascript"))
			matchRes = urlsInJs.matcher(page);
		else
			matchRes = urlsInCss.matcher(page);

		StringBuffer sb = new StringBuffer();
		String urlMatch = null, preMatch = null, postMatch = null;
		while (matchRes.find()) {
			urlMatch = matchRes.group(3).trim();
			preMatch = matchRes.group(1);
			postMatch = matchRes.group(4);

			while (urlMatch.startsWith("."))
				urlMatch.substring(urlMatch.indexOf('/') + 1);

			String replacement = preMatch + serverRoot + urlMatch + postMatch;

			matchRes.appendReplacement(sb,
					Matcher.quoteReplacement(replacement));
		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 重定向页面中的url，使得可以通过文件系统相对路径访问
	 * 
	 * @param rHtml
	 *            页面源码
	 * @param webUrl
	 *            页面url
	 * @param localPath
	 *            页面本地路径
	 * @param contentType
	 *            页面类型:"html" or "javascript" or "css"
	 * @return
	 */
	public static String redirectUrls(String page, WebURL webUrl,
			String localPath, String contentType) {
		String supdirs = "./";
		int idx = -1;
		while ((idx = localPath.indexOf('/', idx + 1)) != -1) {
			if (idx > 0)
				supdirs += "../";
		}

		Matcher matchRes = null;
		if (contentType.contains("html"))
			matchRes = urlsInHtml.matcher(page);
		else if (contentType.contains("javascript"))
			matchRes = urlsInJs.matcher(page);
		else
			matchRes = urlsInCss.matcher(page);

		StringBuffer sb = new StringBuffer();
		String wholeMatch = null, urlMatch = null, preMatch = null, postMatch = null;
		while (matchRes.find()) {
			wholeMatch = matchRes.group(0);
			urlMatch = matchRes.group(3);
			preMatch = matchRes.group(1);
			postMatch = matchRes.group(4);
			String curl = URLCanonicalizer.getCanonicalURL(urlMatch.trim(),
					webUrl.getURL());
			WebURL cweburl = new WebURL();
			if (curl != null && curl.length()>"http://".length()) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				LOG.debug("curl不规范：\t" + curl + "\t<--\t" + urlMatch);
			}
			/* 判断是否redirect */
			String replacement = null;
			if (shouldRedirect(cweburl, webUrl)) {
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
	public static boolean shouldRedirect(WebURL url, WebURL context) {
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
				&& !context.getSubDomain().equals(url.getSubDomain())) {
			return false;
		}

		// port
		if (!SnapshotConfig.getConf().isCrossPorts()
				&& url.getPort() != context.getPort()) {
			return false;
		}

		if (context.getDomain().equals(url.getDomain())) {
			return true;
		}
		return false;

	}

	public static void main(String args[]) {
		// String rHtml = null, path = "blog/main.html";
		// rHtml =
		// "href = \" /abc/def.html\"   src = \" http://baike.cdpsn.org.cn/123/456\" ";
		// System.out.println(redirectUrls(rHtml, "www", "cdpsn.org.cn", path));
		// String invalidString = "\\:*?\"<>";
		// System.out.println(replaceInvalidChar(invalidString));
		WebURL weburl = new WebURL();
		// // ip不能解析
		// //
		weburl.setURL("http://www.scdpf.org.cn:8080/Content/ggtz/ggtz1.html");
		// weburl.setURL("http://10.214.43.12:8080/a/b/c");
		// System.out.println(weburl.getPath());
		String url = "./../abc.sf/com";
		System.out.println(url);
		while (url.startsWith(".")) {
			url = url.substring(url.indexOf('/') + 1);
			System.out.println(url);
		}
		System.out.println(url);

	}
}
