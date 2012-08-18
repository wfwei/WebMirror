package edu.uci.ics.crawler4j.snapshot;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.util.Config;

/**
 * @author WangFengwei <cf.wfwei at gmail dot com>
 */

public class SnapshotCrawlController {

	private static Logger logger = Logger
			.getLogger(SnapshotCrawlController.class);

	public static void runCrawler() {
		Config.configFromFile();
		
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(Config.getSnapshotRoot());
		config.setMaxDepthOfCrawling(Config.getMaxDepth());
		config.setPolitenessDelay(Config.getPolitenessDelay());
		config.setIncludeBinaryContentInCrawling(true);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setEnabled(Config.isRobotsEnabled());
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		try {
			CrawlController controller = new CrawlController(config,
					pageFetcher, robotstxtServer);
			controller.addSeed(Config.getCrawlURL().getURL());
			// run crawler
			controller.start(SnapshotCrawler.class,
					Config.getNumberOfCrawlers());
		} catch (Exception e) {
			logger.error("fail to start snapshotcrawler");
		}

	}

	public static void main(String[] args) throws Exception {
		runCrawler();
	}

}
