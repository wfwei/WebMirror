package edu.uci.ics.crawler4j.snapshot;

import java.nio.charset.Charset;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.io.WriteResult;

public class SnapshotCrawler extends WebCrawler {

	private static Logger LOG = Logger.getLogger(SnapshotCrawler.class);
	Charset UTF8 = Charset.forName("utf-8");

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
		return UrlRel.shouldRedirect(url, SnapshotConfig.getConf()
				.getCrawlURL());
	}

	@Override
	public void visit(Page page) {
		WebURL weburl = page.getWebURL();
		byte[] contentData = page.getContentData();

		String locDir = UrlRel.getDataDir(weburl);
		String locPath = UrlRel.appendFileToPath(weburl.getPath());
		String fullLocPath = SnapshotConfig.getConf().getSnapshotPage() + "/"
				+ locDir + locPath;
		String serverRoot = SnapshotConfig.getConf().getServerRoot();
		String serverPath = serverRoot + locDir + locPath;

		// 转码并重定向链接
		if (page.getContentType().contains("text/html")) {
			HtmlParseData htmlpd = (HtmlParseData) page.getParseData();
			String nHtml = UrlRel.redirectUrls4Server(htmlpd.getHtml(), weburl,
					serverRoot, "html");
			nHtml = nHtml.replaceAll(
					"(<.*?charset\\s*=\\s*['\"]?)[^'\";,\\s>]*?(['\";,\\s>])",
					"$1utf-8$2");
			nHtml = nHtml.replace("window.print()", "");
			contentData = nHtml.getBytes(UTF8);
		} else if (page.getContentType().contains("javascript")) {
			String content = page.getParseData().toString();
			content = UrlRel.redirectUrls4Server(content, weburl, serverRoot,
					"javascript");
			content = content.replace("window.print()", "");
			contentData = content.getBytes(UTF8);
		} else if (page.getContentType().contains("css")) {
			String content = page.getParseData().toString();
			content = UrlRel.redirectUrls4Server(content, weburl, serverRoot,
					"css").replaceAll("@charset[^;]*;", "@charset \"utf-8\";");
			contentData = content.getBytes(UTF8);
		} else if (page.getContentType().contains("image")) {
			// 包括image/png;image/jpeg等等
			if (!page.getWebURL().getURL().toLowerCase()
					.matches(".*(\\.(bmp|gif|jpe?g|png|tiff?|ico)).*")) {
				// 不保存不正常的图片，但是http://www.wrfrwrrr.com/images/line-tile.png?1331724647属于正常图片
				LOG.warn("跳过\t" + page.getContentType() + "\t"
						+ page.getWebURL().getURL());
				return;
			}
		} else {

			/**
			 * 跳过的类型有：text/xml,text/plain,以及非js|css的application类型
			 */
			LOG.warn("跳过\t" + page.getContentType() + "\turl:\t"
					+ page.getWebURL().getURL());
			return;
		}

		String idxFileDir = UrlRel.getIdxDir(weburl);
		String filetype = fullLocPath
				.substring(fullLocPath.lastIndexOf('.') + 1);
		int depth = weburl.getDepth();
		if (depth > SnapshotConfig.getConf().getMaxTaskDepth())
			depth = SnapshotConfig.getConf().getMaxTaskDepth();
		String idxDesFile = idxFileDir + filetype + depth + ".idx";
		String idxDataItem = serverPath + "\t" + weburl.getURL() + "\n";

		WriteResult.writeIdxFile(idxDataItem, idxDesFile);
		WriteResult.writeBytesToFile(contentData, fullLocPath);
		LOG.info("Stored: " + weburl.getURL());
	}

}
