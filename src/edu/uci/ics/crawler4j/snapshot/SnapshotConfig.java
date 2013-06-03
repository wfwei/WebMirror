package edu.uci.ics.crawler4j.snapshot;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;

public class SnapshotConfig extends CrawlConfig {
	private static final Logger LOG = Logger.getLogger(SnapshotConfig.class);

	private WebURL crawlURL = new WebURL();
	private String snapshotRoot = "D:/temp";
	private String snapshotPage = snapshotRoot + "/snapshot/";
	private String snapshotIndex = snapshotRoot + "/index/";
	private int numberOfCrawlers = 1;
	private boolean robotsEnabled = false;
	private boolean crossSubDomains = true;
	private boolean crossPorts = true;

	private static final SnapshotConfig config = new SnapshotConfig();

	private SnapshotConfig() {
	}

	public static SnapshotConfig getConf() {
		return config;
	}

	public void initFromDB() {
		// TODO or never do
		System.out.println("not implemented yet");
	}

	public void initFromFile() {
		InputStream is = null;
		try {
			is = SnapshotConfig.class
					.getResourceAsStream("/crawler4j.properties");
			Properties prop = new Properties();
			prop.load(is);
			this.setNumberOfCrawlers(Integer.parseInt(prop
					.getProperty("num_of_crawlers")));
			this.setSnapshotRoot(prop.getProperty("snapshot_root"));
			this.setCrawlStorageFolder(this.getSnapshotRoot());
			this.setSnapshotIndex(this.getSnapshotRoot() + "/index/");
			this.setSnapshotPage(this.getSnapshotRoot() + "/snapshot/");
			this.getCrawlURL().setURL(prop.getProperty("crawl_domains"));
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
			this.setResumableCrawling(prop.getProperty("resumableCrawling")
					.contains("true"));
//			this.setProxyHost("localhost");
//			this.setProxyPort(8087);
			is.close();
		} catch (Exception e) {
			LOG.warn("fail to load crawler4j.properties!");
		}
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

}
