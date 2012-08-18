package edu.uci.ics.crawler4j.util;

import org.apache.log4j.Logger;

public class Debug {
	public static Logger checkLogger = Logger
			.getLogger(Debug.class);
	public static void main(String args[]){
		checkLogger.warn("logger test");
	}
}
