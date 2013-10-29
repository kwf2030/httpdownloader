package cn.knight.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

public class FileFetcher implements Runnable {

	private static final int BUFFER_SIZE = 1048576;
	private static final int CONNECTION_TIMEOUT = 10000;
	private static final int SO_TIMEOUT = 10000;

	private HttpClient mHttpClient = new DefaultHttpClient();
	private StringBuilder mRangeString = new StringBuilder();
	private ExecutorService mProgressExecutor = Executors.newSingleThreadExecutor();
	private boolean mProgressChecking = true;
	private boolean mDownloading = true;

	private DownloadItem mDownloadItem;
	private RandomAccessFile mRaf;
	private DownloadListModel mDownloadListModel;

	public FileFetcher(DownloadItem info, DownloadListModel model) {
		mDownloadItem = info;
		mDownloadListModel = model;
		if ((info.getProxy() != null) && (info.getPort() != 0)) {
			HttpHost proxy = new HttpHost(info.getProxy(), info.getPort());
			mHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
		mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
		
		try {
			mRaf = new RandomAccessFile(new File(info.getDir(), info.getName()), "rw");
		} catch (FileNotFoundException e) {
			Utils.exception(e);
		}
	}

	public void fetch() throws URISyntaxException, ClientProtocolException, IOException, InterruptedException {
		while (mDownloading) {
			HttpGet request = new HttpGet(mDownloadItem.getUrl());
			HttpResponse response = null;
			initCommonHeaders(request);

			long pos = mDownloadItem.getPos();
			long size = mDownloadItem.getSize();
			long block = mDownloadItem.getBlock();
			
			if (pos == 0) {
				addRangeHeader(request, 0, block - 1);
				response = mHttpClient.execute(request);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					String range = response.getHeaders("Content-Range")[0].getValue();
					size = Long.parseLong(range.split("/")[1]);
					mDownloadItem.setSize(size);
					mDownloadListModel.update(DownloadListModel.COLUMN_SIZE, mDownloadItem);
					download(response.getEntity().getContent(), pos);
					if (size <= block) {
						finish(DownloadItem.COMPLETED);
					}
				}
				
			} else if (size - pos <= block) {
				addRangeHeader(request, pos, -1);
				response = mHttpClient.execute(request);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					download(response.getEntity().getContent(), pos);
					finish(DownloadItem.COMPLETED);
				}
				
			} else {
				addRangeHeader(request, pos, pos + block - 1);
				response = mHttpClient.execute(request);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
					download(response.getEntity().getContent(), pos);
				}
			}

			TimeUnit.MILLISECONDS.sleep(100);
		}
	}

	private void download(InputStream in, long pos) {
		byte[] buf = new byte[BUFFER_SIZE];
		int len = -1;
		BufferedInputStream bin = new BufferedInputStream(in);
		try {
			mRaf.seek(pos);
			while (mDownloadItem.getStatus() == DownloadItem.STARTED && (len = bin.read(buf)) != -1) {
				mRaf.write(buf, 0, len);
				mDownloadItem.setPos(mDownloadItem.getPos() + len);
			}
			bin.close();
		} catch (SocketTimeoutException e) {
			Utils.exception(e);
			try {
				finish(DownloadItem.FAILED);
			} catch (IOException ex) {
				Utils.exception(ex);
			}
		} catch (IOException e) {
			Utils.exception(e);
		}
	}

	private void initCommonHeaders(HttpRequest request) {
		request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
		request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		request.addHeader("Accept-Language", "h-CN,zh;q=0.8");
		request.addHeader("Proxy-Connection", "keep-alive");
		request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.1");
	}

	private void addRangeHeader(HttpRequest request, long start, long end) {
		mRangeString.delete(0, mRangeString.length());
		mRangeString.append("bytes=").append(start).append("-");
		if (end != -1) {
			mRangeString.append(end);
		}
		request.addHeader("RANGE", mRangeString.toString());
	}
	
	public void pause() {
		try {
			finish(DownloadItem.PAUSED);
		} catch (IOException e) {
			Utils.exception(e);
		}
	}
	
	private void finish(int type) throws IOException {
		mDownloadItem.setStatus(type);
		mDownloadItem.setSpeed(-1);
		mDownloading = false;
		mProgressChecking = false;
		mProgressExecutor.shutdown();
		mRaf.close();
		releaseConnection();
		mDownloadListModel.update(DownloadListModel.COLUMN_SPEED, mDownloadItem);
		mDownloadListModel.update(DownloadListModel.COLUMN_PROGRESS, mDownloadItem);
	}

	private void releaseConnection() {
		if (mHttpClient != null) {
			mHttpClient.getConnectionManager().shutdown();
			mHttpClient = null;
		}
	}

	@Override
	public void run() {
		mDownloadItem.setStatus(DownloadItem.STARTED);
		mProgressExecutor.execute(new ProgressChecker());
		try {
			fetch();
		} catch (URISyntaxException | IOException | InterruptedException e) {
			Utils.exception(e);
		}
	}
	
	private class ProgressChecker implements Runnable {
		private long lastDownloadPos = mDownloadItem.getPos();
		private long lastTime = System.currentTimeMillis();
		
		public void run() {
			while (mProgressChecking) {
				try {
					calcSpeed();
					mDownloadListModel.update(DownloadListModel.COLUMN_SPEED, mDownloadItem);
					mDownloadListModel.update(DownloadListModel.COLUMN_PROGRESS, mDownloadItem);
					TimeUnit.MILLISECONDS.sleep(2000);
				} catch (InterruptedException e) {
					Utils.exception(e);
				}
			}
		}
		
		private void calcSpeed() {
			long downloadedBytes = mDownloadItem.getPos() - lastDownloadPos;
			long spendTime = System.currentTimeMillis() - lastTime;
			lastDownloadPos = mDownloadItem.getPos();
			lastTime = System.currentTimeMillis();
			try {
				mDownloadItem.setSpeed((int) ((downloadedBytes * 1024) / (spendTime * 1000)));
			} catch (ArithmeticException e) {
				mDownloadItem.setSpeed(-1);
			}
		}
	}

}
