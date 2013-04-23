package edu.uci.ics.crawler4j.snapshot;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.UrlRel;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.io.WriteResult;

public class SnapshotCrawler extends WebCrawler {

	private static final Pattern filters = Pattern.compile(
			".*(\\.(mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern staticFilePatterns = Pattern.compile(
			".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$",
			Pattern.CASE_INSENSITIVE);

	private static Logger logger = Logger.getLogger(SnapshotCrawler.class);

	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode,
			String statusDescription) {
		super.handlePageStatusCode(webUrl, statusCode, statusDescription);
		// record page errors
		if (statusCode != HttpStatus.SC_OK) {
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
					|| statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				return;
			logger.warn("page error: " + statusCode + "\t" + statusDescription
					+ "\t" + webUrl);
		}
	}

	/**
	 * 判断顺序很重要
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
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
				&& !url.getSubDomain().equals(
						SnapshotConfig.getConf().getCrawlURL().getSubDomain())) {
			return false;
		}

		// port
		if (!SnapshotConfig.getConf().isCrossPorts()
				&& url.getPort() != SnapshotConfig.getConf().getCrawlURL()
						.getPort()) {
			return false;
		}

		if (url.getDomain().equals(
				SnapshotConfig.getConf().getCrawlURL().getDomain())) {
			return true;
		}
		return false;
	}

	@Override
	public void visit(Page page) {
		WebURL weburl = page.getWebURL();
		byte[] contentData = page.getContentData();

		String fullValidDomain = UrlRel.getFullValidDomain(weburl);
		String validPath = UrlRel.appendFileToPath(weburl.getPath());
		String fullLocPath = SnapshotConfig.getConf().getSnapshotPage() + "/"
				+ fullValidDomain + validPath;
		// logger.info("FullLocPath:" + fullLocPath);

		if (page.getContentType() == null) {
			/* 查看上海残联日志，基本不会出现 */
			logger.warn("跳过 page without content type\t"
					+ page.getWebURL().getURL());
			return;
		}

		// check
		// logger.info("contentType:\t" + page.getContentType() + "\turl:\t"
		// + weburl.getURL());

		if (page.getContentType().contains("text/html")) {
			// 转码并重定向链接
			HtmlParseData htmlpd = (HtmlParseData) page.getParseData();
			String nHtml = UrlRel.redirectUrlsInHtml(htmlpd.getHtml(), weburl,
					validPath);
			try {
				// html页面的meta统一使用utf-8编码，如<meta http-equiv=content-type
				// content="text/html; charset=GBK">
				// 同时，其引用到的静态文件如果制定了编码，也统一改为utf-8，如
				// <script type="text/javascript" language="javascript"
				// src="test.js" charset="utf-8"></script>
				nHtml = nHtml
						.replaceFirst(
								"(<.*?charset\\s*=\\s*['\"]?)[^'\";,\\s>]*?(['\";,\\s>])",
								"$1utf-8$2");
				contentData = nHtml.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("this should never happen" + e.toString());
			}
		} else if (page.getContentType().contains("javascript")
				|| page.getContentType().contains("css")) {
			// 转码 js and css

			String content = page.getParseData().toString();
			String charset = page.getContentCharset().trim();
			if (charset != null) {
				/* 分别对js和css文件中的链接进行重定向 */
				if (page.getContentType().contains("javascript")) {
					content = UrlRel.redirectUrlsInJs(content, weburl,
							validPath);
				} else {
					// css中表明编码是首行@charset utf-8;的格式
					content = UrlRel.redirectUrlsInCss(content, weburl,
							validPath).replaceAll("@charset[^;]*;",
							"@charset \"utf-8\";");
				}

				try {
					contentData = content.getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
				}

			} else {
				logger.warn("write a js or css file without a charset\t"
						+ page.getWebURL().getURL());
			}
		} else if (page.getContentType().contains("image")) {
			// 包括image/png;image/jpeg等等
			if (!page.getWebURL().getURL().toLowerCase()
					.matches(".*(\\.(bmp|gif|jpe?g|png|tiff?|ico)).*")) {
				// 不保存不正常的图片，但是http://www.wrfrwrrr.com/images/line-tile.png?1331724647属于正常图片
				logger.warn("跳过\t" + page.getContentType() + "\t"
						+ page.getWebURL().getURL());
				return;
			}
		} else {

			/**
			 * 跳过的类型有：text/xml,text/plain,以及非js|css的application类型
			 */
			logger.warn("跳过\t" + page.getContentType() + "\turl:\t"
					+ page.getWebURL().getURL());
			return;
		}

		/* 写文件系统，但是前面有跳过部分文件，这样html中重定向的链接将会失效 */
		WriteResult.writeIdxFile(fullLocPath, weburl.getURL(), SnapshotConfig
				.getConf().getSnapshotIndex() + fullValidDomain + "/");
		WriteResult.writeBytesToFile(contentData, fullLocPath);
		logger.info("Stored: " + weburl.getURL());

	}
}
