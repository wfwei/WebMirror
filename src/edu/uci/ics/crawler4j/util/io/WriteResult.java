package edu.uci.ics.crawler4j.util.io;

import java.io.File;
import java.io.FileOutputStream;
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
		// aspx_WH_EMailType=0这种网页会认为是aspx
		if (filetype.contains("_")) {
			filetype = filetype.substring(0, filetype.indexOf('_'));
		}
		// 去掉端口一并存储 例如：
		// D:/snapshottheweb/tmp9/index/www.sdcl.org.cn_MH_8085/---->D:/snapshottheweb/tmp9/index/www.sdcl.org.cn/
		destination = destination.replaceAll("_MH_[\\d]*", "") + filetype
				+ ".idx";
		createParentFolders(destination);
		String data = localPath + "\t" + url + "\n";
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
			e.printStackTrace();
		}
	}

}
