package edu.zju.wfwei.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlRel {
	/**
	 * redirect in-site urls
	 * 
	 * @author WangFengwei
	 * 
	 * @param rHtml
	 *            : src of web page
	 * @param domain
	 *            : domain of web page, such as: wrfrwrrr.com
	 * @param subDomain
	 *            :subdomain of web page, such as: www or baike
	 * @param path
	 *            : path of web page, such as: blog/main.css
	 * @return relative address ../../main.css
	 */
	public static String redirectUrls(String rHtml, String domain,
			String subDomain, String path) {
		String supdirs = "./";
		String curSite = subDomain + "." + domain;
		String tarSite = null;
		int idx = -1;
		while ((idx = path.indexOf('/', idx + 1)) != -1) {
			if (idx > 0)
				supdirs += "../";
		}
		// 找到以href或者src开头并且之后的双引号中，以http地址开头或/开头的字符串
		String regstr = "(href|src)\\s*=\\s*\"\\s*(/|((http://)([^/\"]+)[/]?))([^\"]*\\s*\")";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(rHtml);

		StringBuffer sb = new StringBuffer();
		while (matchRes.find()) {
			// System.out.println("group count: " + matchRes.groupCount());
			tarSite = matchRes.group(matchRes.groupCount() - 1);
			if (tarSite != null && !tarSite.equals(curSite))
				matchRes.appendReplacement(sb,
						matchRes.group(1) + "=\"" + supdirs + "../" + tarSite
								+ "/" + matchRes.group(matchRes.groupCount()));
			else
				matchRes.appendReplacement(sb, matchRes.group(1) + "=\""
						+ supdirs + matchRes.group(matchRes.groupCount()));

			if (sb.charAt(sb.length() - 2) == '/')
				sb.insert(sb.length() - 1, "index.html");
		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 判断路径是否包含了文件名，比如：···/path/to/file/a.html 返回true；···/path/to/file
	 * 或···/path/to/file/ 返回false；
	 * 
	 * @param path
	 * @return
	 */
	public static boolean fileSpecified(String path) {
		if (path.endsWith("/"))
			return false;
		if (path.lastIndexOf('.') < path.lastIndexOf('/'))
			return false;
		return true;
	}

	/**
	 * 返回url的域名 比如 http://www.abc.com/ 的domain是www.abc.com
	 * 
	 * @param url
	 * @return
	 */
	public static String getDomain(String url) {
		String domain = url;
		if (domain.startsWith("http://")) {
			domain = domain.substring(7);
		}
		if (url.endsWith("/")) {
			domain = domain.substring(0, domain.length() - 1);
		}
		return domain;
	}

	public static void main(String args[]) {
		String rHtml = null, domain = "cdpsn.org.cn", subDomain = "www", path = "blog/main.html";
		rHtml = "href = \" /abc/def.html\"   src = \" http://baike.baidu.com/123/456.html\" ";
		redirectUrls(rHtml, domain, subDomain, path);
	}
}
