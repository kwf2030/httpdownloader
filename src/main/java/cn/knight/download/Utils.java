package cn.knight.download;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public final class Utils {

	private static final boolean LOG_MODE = false;
	private static final boolean EXCEPTION_MODE = true;
	private static final String CONF_FILE_NAME = "conf.properties";
	private static final String HISTORY_FILE_NAME = "history.dat";
	private static final String EXCEPTION_FILE_NAME = "exception.txt";
	private static final int EXCEPTION_FILE_MAX_SIZE = 1048576;
	private static final SimpleDateFormat FORMAT4NAME = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final SimpleDateFormat FORMAT4LOG = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Properties PROP = new Properties();
	private static final String CHARSET = "UTF-8";
	
	public static final Font FONT = new Font("微软雅黑", Font.PLAIN, 14);
	
	public static void log(String msg) {
		if (LOG_MODE) {
			System.out.println(msg);
		}
	}

	public static void exception(Exception e) {
		if (EXCEPTION_MODE) {
			if (LOG_MODE) {
				e.printStackTrace();
			}
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(EXCEPTION_FILE_NAME, true);
				out.write(getTime(1).getBytes(CHARSET));
				out.write(System.lineSeparator().getBytes(CHARSET));
				e.printStackTrace(new PrintStream(out));
				out.write(System.lineSeparator().getBytes(CHARSET));
				out.write(System.lineSeparator().getBytes(CHARSET));

			} catch (IOException ex) {

			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		}
	}
	
	public static void saveHistory(List<DownloadItem> list) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE_NAME, false));
			out.writeObject(list);
			out.close();
			
		} catch (IOException e) {
			exception(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<DownloadItem> readHistory() {
	    ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(HISTORY_FILE_NAME));
			return (List<DownloadItem>) (in.readObject());

		} catch (IOException | ClassNotFoundException e) {
			exception(e);
			
		} finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                exception(e);
            }
		}
		return null;
	}
	
	public static String getConfProperty(String key) {
		if (PROP.isEmpty()) {
			loadConf();
		}
		return PROP.getProperty(key);
	}
	
	public static String getTime(int format) {
		if (format == 0) {
			return FORMAT4NAME.format(new Date());
		} else {
			return FORMAT4LOG.format(new Date());
		}
	}
	
	public static void deleteHistoryFile() {
		File f = new File(HISTORY_FILE_NAME);
		if (f.exists() && f.isFile()) {
			f.delete();
		}
	}
	
	public static void deleteExceptionFile() {
		File f = new File(EXCEPTION_FILE_NAME);
		if (f.exists() && f.isFile() && f.length() > EXCEPTION_FILE_MAX_SIZE) {
			f.delete();
		}
	}
	
	private static void loadConf() {
		try {
			PROP.load(Utils.class.getClassLoader().getResourceAsStream(CONF_FILE_NAME));
		} catch (IOException e) {
			exception(e);
		}
	}

}
