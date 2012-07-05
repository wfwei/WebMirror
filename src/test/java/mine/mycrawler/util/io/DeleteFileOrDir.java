package mine.mycrawler.util.io;

import java.io.File;
import org.apache.log4j.Logger;

/**
 * 删除文件或目录
 * 
 * @author WangFengwei
 * @since 2012-6-20
 */
public class DeleteFileOrDir {

	private static Logger logger = Logger.getLogger(DeleteFileOrDir.class);

	/**
	 * 删除文件，可以是单个文件或文件夹
	 * 
	 * @param fileName
	 *            待删除的文件名
	 * @return true 文件删除成功
	 * @return false 删除失败
	 */
	public static boolean delete(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			logger.warn("删除文件失败：" + fileName + "文件不存在");
			return false;
		} else {
			if (file.isFile()) {
				return deleteFile(fileName);
			} else {
				return deleteDirectory(fileName);
			}
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param fileName
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true,否则返回false
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			file.delete();
			logger.info("删除单个文件" + fileName + "成功！");
			return true;
		} else {
			logger.warn("删除单个文件" + fileName + "失败！");
			return false;
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param dir
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true,否则返回false
	 */
	public static boolean deleteDirectory(String dir) {
		// 如果dir不以文件分隔符结尾，自动添加文件分隔符
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		File dirFile = new File(dir);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			logger.warn("删除目录失败" + dir + "目录不存在！");
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
			// 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			logger.warn("删除目录失败");
			return false;
		}

		// 删除当前目录
		if (dirFile.delete()) {
			logger.info("删除目录" + dir + "成功！");
			return true;
		} else {
			logger.warn("删除目录" + dir + "失败！");
			return false;
		}
	}

	public static void main(String[] args) {
		// String fileName = "D:/tmp/abc.txt";
		// DeleteFileOrDir.deleteFile(fileName);
		String fileDir = "D:/temp/tmp0";
		// DeleteFileOrDir.deleteDirectory(fileDir);
		DeleteFileOrDir.delete(fileDir);

	}
}