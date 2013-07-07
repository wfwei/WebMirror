package edu.uci.ics.crawler4j.snapshot;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;

public class SnapshotConfig extends CrawlConfig {
	private static final Logger LOG = Logger.getLogger(SnapshotConfig.class);

	private long crawlId = 0;
	private WebURL crawlURL = new WebURL();
	private String snapshotRoot = "D:/tmp/";
	private String snapshotPage = snapshotRoot + "snapshot/" + crawlId + "/";
	private String snapshotIndex = snapshotRoot + "index/" + crawlId + "/";

	private String serverName = "http://localhost/";
	private String serverRoot = "localFile/";
	private String serverNameRoot = serverName + serverRoot + crawlId + "/";

	private int numberOfCrawlers = 1;
	private boolean robotsEnabled = false;
	private boolean crossSubDomains = true;
	private boolean crossPorts = true;

	private double urlSimThreshold = UrlSim.DefaulThreshold;

	private final int maxTaskDepth = 6;

	public String toString() {
		return String
				.format("CrawlerConfig:{crawlId:%s, crawlURL:%s, max_depth:%s, snapshotRoot:%s, serverRoot:%s, numberOfCrawlers:%s, crossSubDomains:%s, crossPorts:%s, resumableCrawling:%s, politeness_delay:%s}",
						this.getCrawlId(), this.getCrawlURL(),
						this.getMaxDepthOfCrawling(), this.getSnapshotRoot(),
						this.getServerRoot(), this.getNumberOfCrawlers(),
						this.isCrossPorts(), this.isCrossPorts(),
						this.isResumableCrawling(), this.getPolitenessDelay());
	}

	public void initFromFile() {
		InputStream is = null;
		try {
			is = SnapshotConfig.class
					.getResourceAsStream("/crawler4j.properties");
			Properties prop = new Properties();
			prop.load(is);

			this.setCrawlId(Integer.parseInt(prop.getProperty("crawl_id")));
			this.getCrawlURL().setURL(prop.getProperty("crawl_domains"));
			this.setSnapshotRoot(prop.getProperty("snapshot_root"));
			// generated values
			this.setCrawlStorageFolder(this.getSnapshotRoot() + "frontier/"
					+ this.getCrawlId() + "/");
			this.setSnapshotIndex(this.getSnapshotRoot() + "index/"
					+ this.getCrawlId() + "/");
			this.setSnapshotPage(this.getSnapshotRoot() + "snapshot/"
					+ this.getCrawlId() + "/");
			this.setServerName(prop.getProperty("server_name"));
			this.setServerRoot(prop.getProperty("server_root"));
			this.setServerNameRoot(this.getServerName() + this.getServerRoot()
					+ this.getCrawlId() + "/");
			this.setNumberOfCrawlers(Integer.parseInt(prop
					.getProperty("num_of_crawlers")));
			this.setMaxDepthOfCrawling(Integer.parseInt(prop
					.getProperty("max_depth")));

			this.setPolitenessDelay(Integer.parseInt(prop
					.getProperty("politeness_delay")));
			this.setRobotsEnabled(prop.getProperty("robots_status").contains(
					"true"));

			this.setCrossSubDomains(prop.getProperty("cross_sub_domains")
					.contains("true"));
			this.setCrossPorts(prop.getProperty("cross_ports").contains("true"));
			this.setIncludeBinaryContentInCrawling(prop.getProperty(
					"include_binary_content_in_in_crawling").contains("true"));
			this.setResumableCrawling(prop.getProperty("resumable_crawling")
					.contains("true"));

			this.setUrlSimThreshold(Double.parseDouble(prop
					.getProperty("url_sim_threshold")));
			is.close();
		} catch (Exception e) {
			LOG.warn("fail to load crawler4j.properties!");
		}
	}

	public long getCrawlId() {
		return crawlId;
	}

	public void setCrawlId(long crawlId) {
		this.crawlId = crawlId;
	}

	public WebURL getCrawlURL() {
		return crawlURL;
	}

	public void setCrawlURL(WebURL crawlURL) {
		this.crawlURL = crawlURL;
	}

	public String getSnapshotRoot() {
		return snapshotRoot;
	}

	public void setSnapshotRoot(String snapshotRoot) {
		this.snapshotRoot = snapshotRoot;
	}

	public String getSnapshotPage() {
		return snapshotPage;
	}

	public void setSnapshotPage(String snapshotPage) {
		this.snapshotPage = snapshotPage;
	}

	public String getSnapshotIndex() {
		return snapshotIndex;
	}

	public void setSnapshotIndex(String snapshotIndex) {
		this.snapshotIndex = snapshotIndex;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerRoot() {
		return serverRoot;
	}

	public void setServerRoot(String serverRoot) {
		this.serverRoot = serverRoot;
	}

	public String getServerNameRoot() {
		return serverNameRoot;
	}

	public void setServerNameRoot(String serverNameRoot) {
		this.serverNameRoot = serverNameRoot;
	}

	public int getNumberOfCrawlers() {
		return numberOfCrawlers;
	}

	public void setNumberOfCrawlers(int numberOfCrawlers) {
		this.numberOfCrawlers = numberOfCrawlers;
	}

	public boolean isRobotsEnabled() {
		return robotsEnabled;
	}

	public void setRobotsEnabled(boolean robotsEnabled) {
		this.robotsEnabled = robotsEnabled;
	}

	public boolean isCrossSubDomains() {
		return crossSubDomains;
	}

	public void setCrossSubDomains(boolean crossSubDomains) {
		this.crossSubDomains = crossSubDomains;
	}

	public boolean isCrossPorts() {
		return crossPorts;
	}

	public void setCrossPorts(boolean crossPorts) {
		this.crossPorts = crossPorts;
	}

	public int getMaxTaskDepth() {
		return maxTaskDepth;
	}

	public double getUrlSimThreshold() {
		return urlSimThreshold;
	}

	public void setUrlSimThreshold(double urlSimThreshold) {
		this.urlSimThreshold = urlSimThreshold;
	}

}
