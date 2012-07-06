package edu.zju.wfwei.util.io;

import java.io.File;
import org.apache.log4j.Logger;

/**
 * ɾ���ļ���Ŀ¼
 * 
 * @author WangFengwei
 * @since 2012-6-20
 */
public class DeleteFileOrDir {

	private static Logger logger = Logger.getLogger(DeleteFileOrDir.class);

	/**
	 * ɾ���ļ��������ǵ����ļ����ļ���
	 * 
	 * @param fileName
	 *            ��ɾ����ļ���
	 * @return true �ļ�ɾ��ɹ�
	 * @return false ɾ��ʧ��
	 */
	public static boolean delete(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			logger.warn("ɾ���ļ�ʧ�ܣ�" + fileName + "�ļ�������");
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
	 * ɾ����ļ�
	 * 
	 * @param fileName
	 *            ��ɾ���ļ����ļ���
	 * @return �����ļ�ɾ��ɹ�����true,���򷵻�false
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			file.delete();
			logger.info("ɾ����ļ�" + fileName + "�ɹ���");
			return true;
		} else {
			logger.warn("ɾ����ļ�" + fileName + "ʧ�ܣ�");
			return false;
		}
	}

	/**
	 * ɾ��Ŀ¼���ļ��У��Լ�Ŀ¼�µ��ļ�
	 * 
	 * @param dir
	 *            ��ɾ��Ŀ¼���ļ�·��
	 * @return Ŀ¼ɾ��ɹ�����true,���򷵻�false
	 */
	public static boolean deleteDirectory(String dir) {
		// ���dir�����ļ��ָ���β���Զ�����ļ��ָ��
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		File dirFile = new File(dir);
		// ���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			logger.warn("ɾ��Ŀ¼ʧ��" + dir + "Ŀ¼�����ڣ�");
			return false;
		}
		boolean flag = true;
		// ɾ���ļ����µ������ļ�(��(��Ŀ¼)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// ɾ�����ļ�
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
			// ɾ����Ŀ¼
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			logger.warn("ɾ��Ŀ¼ʧ��");
			return false;
		}

		// ɾ��ǰĿ¼
		if (dirFile.delete()) {
			logger.info("ɾ��Ŀ¼" + dir + "�ɹ���");
			return true;
		} else {
			logger.warn("ɾ��Ŀ¼" + dir + "ʧ�ܣ�");
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