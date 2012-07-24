package edu.zju.wfwei.util.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class WriteResult {

	private static Logger logger = Logger.getLogger(WriteResult.class);

	/**
	 * 根据待写文件的类型，将文件的本地存储地址localPath和url写到destination中
	 * 比如，localPath:d:/dir/file.html url:http://www.url.com
	 * 将被写到destination目录下的html.idx文件中
	 * 
	 * @author WangFengwei
	 * @param localPath
	 * @param url
	 * @param destination
	 */
	public static synchronized void writeIdxFile(String localPath, String url,
			String destination) {
		String filetype = localPath.substring(localPath.lastIndexOf('.') + 1);
		String data = localPath + "\t" + url + "\n";
		destination += "/" + filetype + ".idx";
		createParentFolders(destination);
		try {
			FileChannel fc = new FileOutputStream(destination, true)
					.getChannel();
			fc.write(ByteBuffer.wrap(data.getBytes()));
			fc.close();
		} catch (IOException ioe) {
			logger.warn("error in writeIdxFile" + ioe.toString());
		}
	}

	/**
	 * 写文件操作
	 * 
	 * @author WangFengwei
	 * @param bytes
	 *            字节流
	 * @param des
	 *            目的文件
	 */
	public static void writeBytesToFile(byte[] bytes, String des) {
		try {
			createParentFolders(des);
			FileChannel fc = new FileOutputStream(des).getChannel();
			fc.write(ByteBuffer.wrap(bytes));
			fc.close();
		} catch (Exception e) {
			logger.warn("error in writeBytesToFile" + e.toString());
		}
	}

	private static void createParentFolders(String path) {
		File dir = new File(path);
		if (!dir.isDirectory()) {
			dir = dir.getParentFile();
		}
		dir.mkdirs();
	}

	public static void writeResFile(byte[] contentData, String des) {
		try {
			createParentFolders(des);
			FileOutputStream fos = new FileOutputStream(des);
			fos.write(contentData);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			logger.warn("error in writeResFile" + e.toString());
		}

	}

	public static void main(String args[]) {
		byte[] bs;
		try {
			bs = "你好a ".getBytes("gb2312");
			writeBytesToFile(bs, "d:/test.txt");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
