package edu.uci.ics.crawler4j.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Locality Sensitive Url Map
 * 
 * @author WangFengwei
 */
public class LsUrlMap {

	private HashMap<Integer, List<String>> map = new HashMap<Integer, List<String>>();

	public synchronized void add(String url) {
		Integer key = hashSimKey(url);
		List<String> urls = map.get(key);
		if (urls == null) {
			urls = new ArrayList<String>();
			map.put(key, urls);
		}
		urls.add(url);
	}

	// TODO if necessary to add synchronized keywords
	public synchronized List<String> getSimilar(String url) {
		Integer key = hashSimKey(url);
		if (map.containsKey(key))
			return Collections.unmodifiableList(map.get(key));
		else
			return new ArrayList<String>();
	}

	protected int hashSimKey(String url) {
		return UrlSim.calcSimSig(url);
	}

}
