package edu.zju.wfwei.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.zju.wfwei.snapshot.Config;

public class UrlRel {

	/**
	 * 对站内链接进行redirect，同时考虑子域名
	 * 
	 * @param rHtml
	 * @param subDomain
	 * @param domain
	 * @param path
	 * @return
	 */
	public static String redirectUrls(String rHtml, String subDomain,
			String domain, String path) {
		String supdirs = "./";
		int idx = -1;
		while ((idx = path.indexOf('/', idx + 1)) != -1) {
			if (idx > 0)
				supdirs += "../";
		}
		// 找到以href或者src开头并且之后的双引号中，以http地址开头或/开头的字符串
		String regstr = "(href|src)\\s*=\\s*\"\\s*(/|((http://)([^/\"]+[/]?)))([^\"]*\")";
		Pattern urlFilter = Pattern.compile(regstr, Pattern.CASE_INSENSITIVE);
		Matcher matchRes = urlFilter.matcher(rHtml);

		StringBuffer sb = new StringBuffer();
		while (matchRes.find()) {

//			System.out.println("regex\t" + regstr);
//			for (int i = 0; i <= matchRes.groupCount(); i++) {
//				System.out.println("group" + i + ":\t" + matchRes.group(i));
//			}

			String curPath = matchRes.group(matchRes.groupCount());
			if (curPath.length() > 0) {
				curPath = specifyFile(curPath
						.substring(0, curPath.length() - 1))
						+ curPath.charAt(curPath.length() - 1);
			}
			if (matchRes.group(2).startsWith("http://")) {
				// only redirect in-site domains
				if (!matchRes.group(2).contains(domain))
					matchRes.appendReplacement(sb, matchRes.group(0));
				else if (!Config.isCrossSubDomains()
						&& !matchRes.group(2).contains(subDomain))
					matchRes.appendReplacement(sb, matchRes.group(0));
				else
					matchRes.appendReplacement(sb, matchRes.group(1) + "=\""
							+ supdirs + "../" + matchRes.group(5) + curPath);
			} else {
				matchRes.appendReplacement(sb, matchRes.group(1) + "=\""
						+ supdirs + curPath);
			}
		}
		matchRes.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 判断路径是否包含了文件名，比如：···/path/to/file/a.html 直接返回；···/path/to/file
	 * 或···/path/to/file/ 返回···/path/to/file/index.html；
	 * 
	 * @param path
	 * @return
	 */
	public static String specifyFile(String path) {
		if(null == path){
			return "/index.html";
		}
		if (path.endsWith("/"))
			return path + "index.html";
		if (path.lastIndexOf('.') <= path.lastIndexOf('/')) {
			return path + "/index.html";
		}
		return path;
	}

	public static void main(String args[]) {
		String rHtml = null, path = "blog/main.html";
		rHtml = "href = \" /abc/def.html\"   src = \" http://baike.cdpsn.org.cn/123/456\" ";
		System.out.println(redirectUrls(rHtml, "www", "cdpsn.org.cn", path));
	}
}
