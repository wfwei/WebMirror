package edu.uci.ics.crawler4j.snapshot;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.io.WriteResult;

public class SnapshotCrawler extends WebCrawler {

	private static final Logger LOG = Logger.getLogger(SnapshotCrawler.class);
	private static final Charset UTF8 = Charset.forName("utf-8");
	private SnapshotConfig config;

	public SnapshotCrawler(SnapshotConfig config) {
		this.config = config;
	}

	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode,
			String statusDescription) {
		super.handlePageStatusCode(webUrl, statusCode, statusDescription);
		// record page errors
		if (statusCode != HttpStatus.SC_OK) {
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
					|| statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				return;
			LOG.warn("page error: " + statusCode + "\t" + statusDescription
					+ "\t" + webUrl);
		}
	}

	@Override
	public boolean shouldVisit(WebURL url) {

		WebURL context = config.getCrawlURL();

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
		if (!config.isCrossSubDomains()
				&& !context.getSubDomain().equals(url.getSubDomain())) {
			return false;
		}

		// port
		if (!config.isCrossPorts() && url.getPort() != context.getPort()) {
			return false;
		}

		if (url.getDomain().equals(context.getDomain())) {
			return true;
		}

		return false;
	}

	private String getFullLocalPath(WebURL weburl) {
		return config.getSnapshotPage() + weburl.getFullDomainAndPort()
				+ normalizePath(weburl.getPath());
	}

	private String getFullServerPath(WebURL weburl) {
		return config.getServerNameRoot() + weburl.getFullDomainAndPort()
				+ normalizePath(weburl.getPath());
	}

	private String getFullIdxPath(WebURL weburl) {
		String validPath = normalizePath(weburl.getPath());
		String filetype = validPath.substring(validPath.lastIndexOf('.') + 1);
		int depth = weburl.getDepth() > config.getMaxTaskDepth() ? config
				.getMaxTaskDepth() : weburl.getDepth();
		return config.getSnapshotIndex() + filetype + depth + ".idx";
	}

	@Override
	public void visit(Page page) {
		WebURL weburl = page.getWebURL();
		byte[] contentData = page.getContentData();

		String fullLocalPath = getFullLocalPath(weburl);
		String fullServerPath = getFullServerPath(weburl);

		String idxDesFile = getFullIdxPath(weburl);
		String idxDataItem = fullServerPath + "\t" + weburl.getURL() + "\n";

		// 转码并重定向链接
		if (page.getContentType().contains("text/html")) {
			HtmlParseData htmlpd = (HtmlParseData) page.getParseData();
			String nHtml = redirectUrls4Server(htmlpd.getHtml(), weburl,
					config.getServerNameRoot(), "html");
			nHtml = nHtml.replaceAll(
					"(<.*?charset\\s*=\\s*['\"]?)[^'\";,\\s>]*?(['\";,\\s>])",
					"$1utf-8$2");
			if (!MetaCharset.matcher(nHtml).find()) {
				nHtml = nHtml
						.replace("<head>",
								"<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
			}
			nHtml = nHtml.replace("window.print()", "");// 渲染的时候会出错，所以全部删掉
			contentData = nHtml.getBytes(UTF8);
		} else if (page.getContentType().contains("javascript")) {
			String content = page.getParseData().toString();
			content = redirectUrls4Server(content, weburl,
					config.getServerNameRoot(), "javascript");
			content = content.replace("window.print()", "");
			contentData = content.getBytes(UTF8);
		} else if (page.getContentType().contains("css")) {
			String content = page.getParseData().toString();
			content = redirectUrls4Server(content, weburl,
					config.getServerNameRoot(), "css").replaceAll(
					"@charset[^;]*;", "@charset \"utf-8\";");
			contentData = content.getBytes(UTF8);
		} else if (page.getContentType().contains("image")) {
			// save all images
		} else {
			/**
			 * 跳过这些类型：text/xml,text/plain,以及非js|css的application类型
			 */
			LOG.warn("跳过\t" + page.getContentType() + "\turl:\t"
					+ page.getWebURL().getURL());
			return;
		}

		WriteResult.writeIdxFile(idxDataItem, idxDesFile);
		WriteResult.writeBytesToFile(contentData, fullLocalPath);
		LOG.info("Stored: " + weburl.getURL());
	}

	/**
	 * 将页面中的url转成相对于服务器根目录的绝对网址，即：/localFile/...
	 * 
	 * @param page
	 *            页面源码
	 * @param webUrl
	 *            页面url
	 * @param serverRoot
	 *            服务器根目录
	 * @param contentType
	 *            文件类型，html,css,javascript
	 * @return
	 */
	public String redirectUrls4Server(String page, WebURL webUrl,
			String serverRoot, String contentType) {

		Matcher matchRes = null;
		if (contentType.contains("html"))
			matchRes = urlsInHtml.matcher(page);
		else if (contentType.contains("javascript"))
			matchRes = urlsInJs.matcher(page);
		else
			matchRes = urlsInCss.matcher(page);

		StringBuffer sb = new StringBuffer();
		String wholeMatch = null, preMatch = null, urlMatch = null, postMatch = null;
		while (matchRes.find()) {
			wholeMatch = matchRes.group(0);
			preMatch = matchRes.group(1);
			urlMatch = matchRes.group(3);
			postMatch = matchRes.group(4);
			String curl = URLCanonicalizer.getCanonicalURL(urlMatch.trim(),
					webUrl.getURL());
			WebURL cweburl = new WebURL();
			if (curl != null && curl.length() > "http://".length()) {
				cweburl.setURL(curl);
			} else {
				/* 匹配到的链接不规范，忽略之 */
				cweburl.setURL("http://www.fakeUrl.com/");
				LOG.debug("curl不规范：\t" + curl + "\t<--\t" + urlMatch);
			}
			/* 判断是否redirect */
			String replacement = null;
			if (shouldVisit(cweburl)) {
				replacement = preMatch + getFullServerPath(cweburl) + postMatch;
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
	 * 使url中的path合法化
	 * 
	 * path的几种情况：
	 * 
	 * 1. host/a/b/index.html 不用改
	 * 
	 * 2. host/a/b/ --> host/a/b/index.html
	 * 
	 * 3. host/a/b ---> host/a/b/index.html
	 * 
	 * 4. host/a/b/page.asp?para=1 --> host/a/b/page.asp_WH_para=1/index.html
	 * 
	 */
	public static String normalizePath(String path) {
		// 对含有中文的path解码
		try {
			path = URLDecoder.decode(path, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// 去除不合法字符
		path = path.replaceAll("\\?", "_WH_").replaceAll(":", "_MH_")
				.replaceAll("[*\"<>|\\\\]", "_QT_");

		// 添加合法文件
		if (null == path) {
			return "/index.html";
		}

		if (path.endsWith("/")) {
			return path + "index.html";
		}

		if (path.lastIndexOf('.') <= path.lastIndexOf('/')) {
			return path + "/index.html";
		}

		if (!staticFilePatterns.matcher(path).find() && !path.endsWith(".html"))
			return path + "/index.html";

		return path;
	}

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
	private static final Pattern MetaCharset = Pattern
			.compile("<meta.*?charset.*>");
	
	private static final Pattern filters = Pattern.compile(
			".*(\\.(mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern staticFilePatterns = Pattern.compile(
			".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$",
			Pattern.CASE_INSENSITIVE);

}
