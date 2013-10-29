package cn.knight.download;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class DownloadManager {
	
	private static DownloadManager mDownloadMgr;
	
	private ThreadPoolExecutor mDownloadExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	private Map<DownloadItem, FileFetcher> mDownloadQueue = new HashMap<DownloadItem, FileFetcher>();
	
	private DownloadListModel mDownloadListModel;
	
	private DownloadManager(DownloadListModel model) {
		mDownloadListModel = model;
		List<DownloadItem> list = Utils.readHistory();
		if (list != null && list.size() != 0) {
			model.add(list);
		}
	}
	
	public static DownloadManager getInstance(DownloadListModel model) {
		if (mDownloadMgr == null) {
			synchronized (DownloadManager.class) {
				if (mDownloadMgr == null) {
					mDownloadMgr = new DownloadManager(model);
				}
			}
		}
		return mDownloadMgr;
	}
	
	public void addTask(DownloadItem item) {
		List<DownloadItem> list = mDownloadListModel.getDownloadList();
		for (DownloadItem i : list) {
			if (i.equals(item)) {
				return;
			}
		}
		
		FileFetcher fetcher = new FileFetcher(item, mDownloadListModel);
		mDownloadQueue.put(item, fetcher);
		mDownloadExecutor.execute(fetcher);
		mDownloadListModel.add(item);
	}
	
	public void removeTask(int index) {
		DownloadItem item = mDownloadListModel.getDownloadItem(index);
		if (item.getStatus() == DownloadItem.STARTED) {
			pauseTask(index);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Utils.exception(e);
			}
		}
		mDownloadExecutor.remove(mDownloadQueue.get(item));
		mDownloadListModel.remove(index);
	}
	
	public void resumeTask(int index) {
		DownloadItem item = mDownloadListModel.getDownloadItem(index);
		if (item.getStatus() == DownloadItem.COMPLETED) {
			return;
		} else if (item.getStatus() != DownloadItem.STARTED) {
			FileFetcher fetcher = new FileFetcher(item, mDownloadListModel);
			mDownloadQueue.put(item, fetcher);
			mDownloadExecutor.execute(fetcher);
			item.setStatus(DownloadItem.WAITING);
			update(item);
		}
	}
	
	public void pauseTask(int index) {
		DownloadItem item = mDownloadListModel.getDownloadItem(index);
		if (item.getStatus() == DownloadItem.COMPLETED || item.getStatus() == DownloadItem.FAILED) {
			return;
		} else if (item.getStatus() == DownloadItem.STARTED) {
			mDownloadQueue.get(item).pause();
		} else if (item.getStatus() == DownloadItem.WAITING) {
			mDownloadExecutor.remove(mDownloadQueue.get(item));
			item.setStatus(DownloadItem.PAUSED);
			update(item);
		}
	}
	
	public void changeUrl(int index, String newUrl) {
		DownloadItem item = mDownloadListModel.getDownloadItem(index);
		if (item.getStatus() == DownloadItem.COMPLETED) {
			return;
		} else if (item.getStatus() == DownloadItem.STARTED) {
			pauseTask(index);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Utils.exception(e);
			}
			
			item.setUrl(newUrl);
			resumeTask(index);
		} else {
			item.setUrl(newUrl);
		}
	}
	
	public void saveProgress() {
		List<DownloadItem> list = mDownloadListModel.getDownloadList();
		if (list != null) {
			for (int i = list.size() - 1; i >= 0; i--) {
				DownloadItem item = list.get(i);
				if (mDownloadQueue.get(item) != null) {
					mDownloadExecutor.remove(mDownloadQueue.get(item));
				}
				if (item.getStatus() == DownloadItem.STARTED) {
					mDownloadQueue.get(item).pause();
				}
			}
			Utils.saveHistory(list);
		}
	}
	
	private void update(DownloadItem item) {
		mDownloadListModel.update(DownloadListModel.COLUMN_SPEED, item);
		mDownloadListModel.update(DownloadListModel.COLUMN_PROGRESS, item);
	}
	
}
