package edu.zju.wfwei.snapshot;

import java.io.UnsupportedEncodingException;
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
			.compile(".*(\\.(js|css|ashx|bmp|gif|jpe?g|png|tiff?))$");
	private static String crawlDomain = null;
	private static String snapshotLocation = null;
	
	private static Logger logger = Logger.getLogger(SnapshotCrawler.class);

	public static void configure(String crawlDomain, String snapshotFolderName) {
		SnapshotCrawler.crawlDomain = crawlDomain;
		snapshotLocation = snapshotFolderName;
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
			logger.warn("page error: " + statusCode + "\t" + statusDescription
					+ "\t" + webUrl);
		}
	}

	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		//ignore radio and video
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
		// in-site link
		// TODO ֻ�5�����ҳ
		if (href.equals(crawlDomain)) {
			return true;
		}
		return false;
	}

	@Override
	public void visit(Page page) {
		WebURL weburl = page.getWebURL();
		String url = weburl.getURL();
		String path = weburl.getPath();
		String domain = weburl.getDomain();
		String subDomain = weburl.getSubDomain();
		byte[] contentData = page.getContentData();
		String fullLocPath = null;
		// 沒有指定html文件的url，默认指向index.html
		if (!UrlRel.fileSpecified(path)) {
			if (!path.endsWith("/"))
				path += "/";
			path += "index.html";
		}
		fullLocPath = snapshotLocation + "/" + subDomain + "." + domain + path;
		if (page.getContentType() != null
				&& page.getContentType().contains("text/html")) {
			// 将网页文件中的链接重定向本地
			HtmlParseData htmlpd = (HtmlParseData) page.getParseData();
			String nHtml = UrlRel.redirectUrls(htmlpd.getHtml(), domain,
					subDomain, path);
			try {
				// 统一使用utf-8编码
				nHtml = nHtml.replaceFirst("charset=[^\"]*\"", "charset=utf-8");
				contentData = nHtml.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("this should never happen" + e.toString());
			}
		}
		WriteResult.writeIdxFile(fullLocPath, url, snapshotLocation + "/../index/"
				+ UrlRel.getDomain(crawlDomain) + "/");
		WriteResult.writeBytesToFile(contentData, fullLocPath);
		logger.info("Stored: " + url);
	}

}
