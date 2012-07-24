package edu.zju.wfwei.snapshot;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.zju.wfwei.util.UrlRel;
import edu.zju.wfwei.util.io.WriteResult;

public class SnapshotCrawler extends WebCrawler {

	private static final Pattern filters = Pattern
			.compile(".*(\\.(mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private static final Pattern staticFilePatterns = Pattern
			.compile(".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$");

	private static WebURL crawlURL = Config.getCrawlURL();
	private static String snapshotPage = Config.getSnapshotPage();
	private static String snapshotIndex = Config.getSnapshotIndex();

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

		// in-site link
		if (href.startsWith("/"))
			return true;

		if (url.getDomain().equals(crawlURL.getDomain())) {
			// sub domain
			if (!Config.isCrossSubDomains()
					&& !url.getSubDomain().equals(crawlURL.getSubDomain()))
				return false;
			return true;
		}
		return false;
	}

	@Override
	public void visit(Page page) {
		WebURL weburl = page.getWebURL();
		String fullDomain = weburl.getSubDomain() + "." + weburl.getDomain();
		String path = UrlRel.specifyFile(weburl.getPath());
		byte[] contentData = page.getContentData();
		String fullLocPath = snapshotPage
				+ UrlRel.specifyFile(fullDomain + weburl.getPath());
		if (page.getContentType() != null) {
			logger.info("contentType:\t" + page.getContentType() + "\turl:\t"
					+ weburl.getURL());
			// 转码 html
			if (page.getContentType().contains("text/html")) {
				// 将网页文件中的链接重定向本地
				HtmlParseData htmlpd = (HtmlParseData) page.getParseData();
				String nHtml = UrlRel.redirectUrls(htmlpd.getHtml(),
						weburl.getSubDomain(), weburl.getDomain(), path);
				try {
					// 统一使用utf-8编码
					nHtml = nHtml
							.replaceFirst(
									"(<[Mm][Ee][Tt][Aa].*?charset\\s*=\\s*['\"]?)[^'\";,\\s>]*?(['\";,\\s>])",
									"$1utf-8$2");
					contentData = nHtml.getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
					logger.error("this should never happen" + e.toString());
				}
			} else if (page.getContentType().contains("javascript")
					|| page.getContentType().contains("css")) {
				// 转码 js and css
				String charset = page.getContentCharset();
				// TODO 如果文件没有指明编码，则不改期编码，这样文件的实际编码有可能是gb2312，但是不会出现乱码
				if (charset == null) {
					// 解析静态文件的编码
					CodepageDetectorProxy detector = CodepageDetectorProxy
							.getInstance();
					detector.add(JChardetFacade.getInstance());
					Charset icharset = null;
					try {
						ByteArrayInputStream bis = new ByteArrayInputStream(
								contentData);
						icharset = detector.detectCodepage(bis,
								contentData.length);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (null != icharset)
						charset = icharset.toString();
				}
				if (charset != null) {
					if (!charset.equals("utf-8")) {
						String str = null;
						try {
							str = new String(contentData, charset);
							contentData = str.getBytes("utf-8");
						} catch (UnsupportedEncodingException e) {
							logger.warn("invalid charset using default gb2312\t"
									+ page.getWebURL().getURL());
							try {
								str = new String(contentData, "gb2312");
								contentData = str.getBytes("utf-8");
							} catch (UnsupportedEncodingException e1) {
								logger.warn("this should nerver happen! in SnapshotCrawler.java");
							}
						}
					}
				} else {
					logger.warn("write a js or css file without a charset\t"
							+ page.getWebURL().getURL());
				}
			} else if (page.getContentType().contains("image")) {
				// TODO 现在不保存这类图片，以后可以修改文件名保存
				if (!page
						.getWebURL()
						.getURL()
						.matches(
								".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?|ico))$")) {
					return;
				}
			} else if (page.getContentType().contains("application")
					&& page.getContentType().contains("ms")) {
				// msword msexcel...
				logger.warn("跳过\t" + page.getContentType() + "\t"
						+ page.getWebURL().getURL());
				return;
			} else {
				logger.warn("跳过 uncommon content type:\t"
						+ page.getContentType() + "\turl:\t"
						+ page.getWebURL().getURL());
				return;
			}
		} else {
			logger.warn("跳过 page without content type\t"
					+ page.getWebURL().getURL());
			return;
		}
		WriteResult.writeIdxFile(fullLocPath, weburl.getURL(), snapshotIndex
				+ fullDomain + "/");
		// WriteResult.writeResFile(contentData, fullLocPath);
		WriteResult.writeBytesToFile(contentData, fullLocPath);
		logger.info("Stored: " + weburl.getURL());
	}
}
