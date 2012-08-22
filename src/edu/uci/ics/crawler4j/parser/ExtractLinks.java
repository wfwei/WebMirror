package edu.uci.ics.crawler4j.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * 
 * @author WangFengwei
 * 
 */
public class ExtractLinks {

	/**
	 * extract src from <script src="/javascripts/ender.js"></script>
	 * 
	 * @param rHtml
	 * @return
	 */
	public static HashSet<ExtractedUrlAnchorPair> extractJsLinks(String rHtml) {
		HashSet<ExtractedUrlAnchorPair> jslinks = new HashSet<ExtractedUrlAnchorPair>();

		// MARK 提取script的src的正则表达式
		String regex = "<script([^>]+)src\\s*=\\s*['\"]([^'\"]+)['\"]";
		Pattern urlFilter = Pattern.compile(regex);
		Matcher matchRes = urlFilter.matcher(rHtml);

		while (matchRes.find()) {
//			System.out.println(matchRes.group(2));
			ExtractedUrlAnchorPair jsPair = new ExtractedUrlAnchorPair();
			jsPair.setAnchor(matchRes.group(2));
			jsPair.setHref(matchRes.group(2));
			jslinks.add(jsPair);
		}
		return jslinks;
	}

	/**
	 * 提取js css代码中的链接
	 * 
	 * js: document.writeln(" <li><a href=\"http://www.gsmz.gov.cn\"
	 * title=\"甘肃民政信息网\"><img src=\"/wza2012/images/friend_4.jpg\"
	 * alt=\"甘肃民政信息网\" /></a></li>");
	 * http://www.gsdpf.org.cn/wza2012/script/headerwrite.js
	 * 
	 * css: background:url(../../content/image/publice.jpg)
	 * www.gsdpf.org.cn/wza2012/styles/mainframe.css
	 * 
	 * @param content
	 * @param contextUrl
	 * @return
	 */
	public static WebURL[] getUrlFromJsCSS(String content, WebURL contextUrl) {
		ArrayList<WebURL> outGoingUrls = new ArrayList<WebURL>();
		String regex_js = "((href|src)\\s*=\\s*[\\\\]?['\"]?\\s*)([^\\s'\">\\\\]*)([\\s'\">\\\\])";
		String regex_css = "url\\s*[(]([^)]*)[)]";
		String regex = null;
		int group_id = 0;
		if (contextUrl.getURL().contains(".css")) {
			regex = regex_css;
			group_id = 1;
		} else {
			regex = regex_js;
			group_id = 3;
		}
		Pattern urlFilter = Pattern.compile(regex);
		Matcher matchRes = urlFilter.matcher(content);

		while (matchRes.find()) {
			// curl是标准化后的url 地址中不得含有中括号 URL UrlRel中也有类似代码
			String curl = matchRes.group(group_id).trim();
			curl = URLCanonicalizer.getCanonicalURL(curl, contextUrl.getURL());
			if (curl != null && curl.startsWith("http://")) {
				WebURL cweburl = new WebURL();
				cweburl.setURL(curl);
				outGoingUrls.add(cweburl);
			}
		}
		if (outGoingUrls.size() == 0) {
			return new WebURL[0];
		}
		return outGoingUrls.toArray(new WebURL[0]);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// extractJsLinks("<script type=\"sfsjd\" src=\"/javascripts/ender.js\"></script><script jlsafjlasdjf src=\"/javascripts/ender.js\"></script><script jlsafjlasdjf src=\"/javascripts/ender.js\"></script>");

	}

}
