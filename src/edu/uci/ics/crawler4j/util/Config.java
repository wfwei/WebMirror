package edu.uci.ics.crawler4j.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.url.UrlRel;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.io.DeleteFileOrDir;

public class Config {
	private static int numberOfCrawlers = 1;
	private static int maxDepth = -1;
	private static int politenessDelay = 100;
	private static boolean robotsEnabled = false;
	private static String snapshotRoot = "D:/temp";
	private static String snapshotPage = "D:/temp/snapshot/";
	private static String snapshotIndex = "D:/temp/index/";
	private static WebURL crawlURL = new WebURL();
	private static boolean crossSubDomains = true;
	private static boolean crossPorts = true;
	private static Logger logger = Logger.getLogger(Config.class);

	public static void configFromFile() {
		InputStream is;
		try {
			is = Config.class
					.getResourceAsStream("/crawler4j.properties");
			Properties prop = new Properties();
			prop.load(is);
			numberOfCrawlers = Integer.parseInt(prop.getProperty(
					"num_of_crawlers").trim());
			snapshotRoot = prop.getProperty("snapshot_root").trim();
			snapshotPage = prop.getProperty("snapshot_page").trim();
			snapshotIndex = prop.getProperty("snapshot_index").trim();
			crawlURL.setURL(prop.getProperty("crawl_domains").trim());
			maxDepth = Integer.parseInt(prop.getProperty("max_depth").trim());
			politenessDelay = Integer.parseInt(prop.getProperty(
					"politeness_delay").trim());
			robotsEnabled = prop.getProperty("robots_status").trim()
					.contains("true");
			crossSubDomains = prop.getProperty("cross_sub_domains").trim()
					.contains("true");
			crossPorts = prop.getProperty("cross_ports").trim()
					.contains("true");
			rmResFile();
			is.close();
		} catch (Exception e) {
			logger.warn("fail to load crawler4j.properties!");
		}
	}

	private static void rmResFile() {
		String fullValidDomain = UrlRel.getFullValidDomain(crawlURL);
		DeleteFileOrDir.delete(snapshotIndex + fullValidDomain);
	}

	public static int getNumberOfCrawlers() {
		return numberOfCrawlers;
	}

	public static void setNumberOfCrawlers(int numberOfCrawlers) {
		Config.numberOfCrawlers = numberOfCrawlers;
	}

	public static int getMaxDepth() {
		return maxDepth;
	}

	public static void setMaxDepth(int maxDepth) {
		Config.maxDepth = maxDepth;
	}

	public static int getPolitenessDelay() {
		return politenessDelay;
	}

	public static void setPolitenessDelay(int politenessDelay) {
		Config.politenessDelay = politenessDelay;
	}

	public static boolean isRobotsEnabled() {
		return robotsEnabled;
	}

	public static void setRobotsEnabled(boolean robotsStatus) {
		Config.robotsEnabled = robotsStatus;
	}

	public static WebURL getCrawlURL() {
		return crawlURL;
	}

	public static void setCrawlURL(String crawlURL) {
		Config.crawlURL.setURL(crawlURL);
	}

	public static boolean isCrossSubDomains() {
		return crossSubDomains;
	}

	public static void setCrossSubDomains(boolean crossSubDomains) {
		Config.crossSubDomains = crossSubDomains;
	}

	public static boolean isCrossPorts() {
		return crossPorts;
	}

	public static void setCrossPorts(boolean crossPorts) {
		Config.crossPorts = crossPorts;
	}

	public static String getSnapshotPage() {
		return snapshotPage;
	}

	public static void setSnapshotPage(String snapshotPage) {
		Config.snapshotPage = snapshotPage;
	}

	public static String getSnapshotRoot() {
		return snapshotRoot;
	}

	public static void setSnapshotRoot(String snapshotRoot) {
		Config.snapshotRoot = snapshotRoot;
	}

	public static String getSnapshotIndex() {
		return snapshotIndex;
	}

	public static void setSnapshotIndex(String snapshotIndex) {
		Config.snapshotIndex = snapshotIndex;
	}
}
