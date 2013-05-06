package edu.uci.ics.crawler4j.snapshot;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.UrlRel;
import edu.uci.ics.crawler4j.util.io.DeleteFileOrDir;

/**
 * @author WangFengwei <cf.wfwei at gmail dot com>
 */

public class SnapshotCrawlController {

	private static Logger logger = Logger
			.getLogger(SnapshotCrawlController.class);

	public static void runCrawler() {

		SnapshotConfig config = SnapshotConfig.getConf();
		config.initFromFile();
		if (!config.isResumableCrawling()) {
			String fullValidDomain = UrlRel.getFullValidDomain(config
					.getCrawlURL());
			DeleteFileOrDir.delete(config.getSnapshotIndex() + fullValidDomain);
		}


		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setEnabled(false);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		try {
			CrawlController controller = new CrawlController(config,
					pageFetcher, robotstxtServer);
			controller.addSeed(config.getCrawlURL().getURL());
			// run crawler
			controller.start(SnapshotCrawler.class,
					config.getNumberOfCrawlers());

			// controller.startNonBlocking(SnapshotCrawler.class,
			// Config.getNumberOfCrawlers());

			// // Wait for 300 seconds
			// Thread.sleep(300 * 1000);
			//
			// // Send the shutdown request and then wait for finishing
			// controller.Shutdown();
			// controller.waitUntilFinish();
		} catch (Exception e) {
			logger.error("fail to start snapshotcrawler");
		}

	}

	public static void main(String[] args) throws Exception {
		runCrawler();
	}

}
