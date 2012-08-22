/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.parser;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class Parser extends Configurable {

	private HtmlParser htmlParser;
	private ParseContext parseContext;

	public Parser(CrawlConfig config) {
		super(config);
		htmlParser = new HtmlParser();
		parseContext = new ParseContext();
	}

	public boolean parse(Page page, String contextURL) {

		if (Util.hasBinaryContent(page.getContentType())) {
			/* image audio video application */
			if (!config.isIncludeBinaryContentInCrawling()) {
				return false;
			} else {
				BinaryParseData bParseData = new BinaryParseData();

				if (page.getContentType().contains("javascript")
						|| page.getContentType().contains("css")) {
					/* 解析js/css文件的编码，默认使用gb编码，所以可能有误 */
					String defaultCharset = "gb2312";
					Charset icharset = null;
					String bContent = "";
					CodepageDetectorProxy detector = CodepageDetectorProxy
							.getInstance();
					detector.add(JChardetFacade.getInstance());
					try {
						ByteArrayInputStream bis = new ByteArrayInputStream(
								page.getContentData());
						icharset = detector.detectCodepage(bis,
								page.getContentData().length);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						if (null != icharset) {
							bContent = new String(page.getContentData(),
									icharset.toString());
							page.setContentCharset(icharset.toString());
						} else {
							bContent = new String(page.getContentData(),
									defaultCharset);
							page.setContentCharset(defaultCharset);
						}
					} catch (UnsupportedEncodingException uee) {
						try {
							bContent = new String(page.getContentData(),
									defaultCharset);
						} catch (UnsupportedEncodingException e) {
						}
					}
					bParseData.setContent(bContent);
				}

				page.setParseData(bParseData);
				return true;
			}
		} else if (Util.hasPlainTextContent(page.getContentType())) {
			/* text/plain */
			try {
				TextParseData parseData = new TextParseData();
				String charset = page.getContentCharset();
				if (null == charset) {
					charset = "gb2312";
				}
				parseData.setTextContent(new String(page.getContentData(),
						charset));
				page.setParseData(parseData);
				return true;
			} catch (Exception e) {
				System.err.println("plain text charset error:\t" + contextURL
						+ e.toString());
				e.printStackTrace();
			}
			return false;
		}

		/* 解析html */
		Metadata metadata = new Metadata();
		HtmlContentHandler contentHandler = new HtmlContentHandler();
		InputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(page.getContentData());
			/* 解析html，识别编码，提取链接,可以获取href="path/file" */
			htmlParser.parse(inputStream, contentHandler, metadata,
					parseContext);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (page.getContentCharset() == null
				|| page.getContentCharset().length() == 0) {
			/**
			 * 贵州残联的网站返回的http信息中charset异常，有值，但是是空，这里检测charset的长度，如果太短，
			 * 则从网页的meta中重新获取charset，但是，contentType可能还是有问题的
			 */
			page.setContentCharset(metadata.get("Content-Encoding"));
		}

		HtmlParseData parseData = new HtmlParseData();
		parseData.setText(contentHandler.getBodyText().trim());
		parseData.setTitle(metadata.get(Metadata.TITLE));

		try {
			if (page.getContentCharset() == null) {
				parseData.setHtml(new String(page.getContentData()));
			} else {
				parseData.setHtml(new String(page.getContentData(), page
						.getContentCharset()));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		/**
		 * @author WangFengwei extract js links
		 */
		for (ExtractedUrlAnchorPair jsLinkPair : (HashSet<ExtractedUrlAnchorPair>) ExtractLinks
				.extractJsLinks(parseData.getHtml())) {
			contentHandler.addOutgoingUrls(jsLinkPair);
			// System.out.println(jsLinkPair.getAnchor());
		}

		List<WebURL> outgoingUrls = new ArrayList<WebURL>();

		String baseURL = contentHandler.getBaseUrl();
		if (baseURL != null) {
			contextURL = baseURL;
		}
		int urlCount = 0;
		for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler
				.getOutgoingUrls()) {
			String href = urlAnchorPair.getHref();
			href = href.trim();
			if (href.length() == 0) {
				continue;
			}
			String hrefWithoutProtocol = href.toLowerCase();
			if (href.startsWith("http://")) {
				hrefWithoutProtocol = href.substring(7);
			}
			if (!hrefWithoutProtocol.contains("javascript:")
					&& !hrefWithoutProtocol.contains("@")) {
				String url = URLCanonicalizer.getCanonicalURL(href, contextURL);
				// System.out.println("canonical url:\t"+url);
				if (url != null) {
					WebURL webURL = new WebURL();
					webURL.setURL(url);
					webURL.setAnchor(urlAnchorPair.getAnchor());
					outgoingUrls.add(webURL);
					urlCount++;
					if (urlCount > config.getMaxOutgoingLinksToFollow()) {
						break;
					}
				}
			}
		}

		parseData.setOutgoingUrls(outgoingUrls);
		page.setParseData(parseData);
		return true;

	}

}
