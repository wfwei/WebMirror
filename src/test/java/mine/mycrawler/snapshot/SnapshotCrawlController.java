package mine.mycrawler.snapshot;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import mine.mycrawler.util.UrlRel;
import mine.mycrawler.util.io.DeleteFileOrDir;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * @author WangFengwei <cf.wfwei at gmail dot com>
 */

public class SnapshotCrawlController {
	private static int numberOfCrawlers = 1;
	private static int maxDepth = -1;
	private static int politenessDelay = 100;
	private static boolean robots_status = false;
	private static String snapshotLocation = "D:/t2";
	private static String crawlDomain = "http://eagle.zju.edu.cn/";

	private static Logger logger = Logger
			.getLogger(SnapshotCrawlController.class);

	private static void loadConfigFile() {
		InputStream is;
		try {
			is = new FileInputStream("resources/crawler4j.properties");
			Properties prop = new Properties();
			prop.load(is);
			numberOfCrawlers = Integer.parseInt(prop.getProperty(
					"numOfCrawlers").trim());
			snapshotLocation = prop.getProperty("snapshotLocation").trim();

			crawlDomain = prop.getProperty("crawlDomains").trim();
			maxDepth = Integer.parseInt(prop.getProperty("maxDepth").trim());
			politenessDelay = Integer.parseInt(prop.getProperty(
					"politenessDelay").trim());
			robots_status = prop.getProperty("robots_status").trim()
					.contains("true");
			is.close();
		} catch (Exception e) {
			logger.warn("fail to load crawler4j.properties!");
		}
	}

	private static void rmResFile() {
		DeleteFileOrDir.delete(snapshotLocation + "/index/"
				+ UrlRel.getDomain(crawlDomain));
	}

	public static void runCrawler() {
		loadConfigFile();
		rmResFile();
		String snapshotFolder = snapshotLocation + "/snapshot";

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(snapshotLocation);
		config.setMaxDepthOfCrawling(maxDepth);
		config.setPolitenessDelay(politenessDelay);
		config.setIncludeBinaryContentInCrawling(true);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setEnabled(robots_status);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		try {
			CrawlController controller = new CrawlController(config,
					pageFetcher, robotstxtServer);
			controller.addSeed(crawlDomain);
			SnapshotCrawler.configure(crawlDomain, snapshotFolder);
			// run crawler
			controller.start(SnapshotCrawler.class, numberOfCrawlers);
		} catch (Exception e) {
			logger.error("fail to start snapshotcrawler");
		}

	}

	public static void main(String[] args) throws Exception {
		runCrawler();
	}

}
