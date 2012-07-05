package edu.uci.ics.crawler4j.parser;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author WangFengwei
 *
 */
public class ExtractJsLink {

	/**
	 * extract src from <script src="/javascripts/ender.js"></script>
	 * 
	 * @param rHtml 
	 * @return
	 */
	public static HashSet<ExtractedUrlAnchorPair> extractJsLinks(String rHtml) {
		HashSet<ExtractedUrlAnchorPair> jslinks = new HashSet<ExtractedUrlAnchorPair>();
		
		String regex = "<script([^>]+)src=\"([^\"]+)\"";
		Pattern urlFilter = Pattern.compile(regex);
		Matcher matchRes = urlFilter.matcher(rHtml);

		while (matchRes.find()) {
			ExtractedUrlAnchorPair jsPair = new ExtractedUrlAnchorPair();
			jsPair.setAnchor(matchRes.group(2));
			jsPair.setHref(matchRes.group(2));
			jslinks.add(jsPair);
		}
		return jslinks;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		extractJsLinks("<script type=\"sfsjd\" src=\"/javascripts/ender.js\"></script><script jlsafjlasdjf src=\"/javascripts/ender.js\"></script><script jlsafjlasdjf src=\"/javascripts/ender.js\"></script>");
	}

}
