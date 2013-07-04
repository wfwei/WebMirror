package edu.uci.ics.crawler4j.snapshot;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.util.io.DeleteFileOrDir;

/**
 * @author WangFengwei <cf.wfwei at gmail dot com>
 */

public class SnapshotCrawlController {

	private static Logger LOG = Logger.getLogger(SnapshotCrawlController.class);

	public static void runCrawler() {

		SnapshotConfig config = new SnapshotConfig();
		config.initFromFile();

		if (!config.isResumableCrawling()) {
			DeleteFileOrDir.delete(config.getSnapshotIndex());
		}

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setEnabled(false);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		try {
			int maxDepth = config.getMaxDepthOfCrawling();
			// 检测到第i层，需要将第i+1层的资源都下载下来，所以层数加一
			if (maxDepth > -1) {
				config.setMaxDepthOfCrawling(maxDepth + 1);
			}
			LOG.info("当前爬虫配置:\n" + config);
			CrawlController controller = new CrawlController(config,
					pageFetcher, robotstxtServer);
			controller.addSeed(config.getCrawlURL().getURL());
			// run crawler
			controller.startNonBlocking(SnapshotCrawler.class,
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
			LOG.error("fail to start snapshotcrawler");
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		runCrawler();
	}

}
