package cn.knight.download;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public final class DownloadListModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 8698632918715493941L;
	
	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_DIR = 1;
	public static final int COLUMN_SIZE = 2;
	public static final int COLUMN_SPEED = 3;
	public static final int COLUMN_PROGRESS = 4;
	
	private List<DownloadItem> mDownloadList = new ArrayList<DownloadItem>();

	private NumberFormat mPercentFormat = NumberFormat.getPercentInstance();
	
	private StringBuilder mStringBuilder = new StringBuilder();
	
	private String[] mColumnNames;

	public DownloadListModel(String[] columnNames) {
		mPercentFormat.setMaximumFractionDigits(2);
		mColumnNames = columnNames;
	}

	@Override
	public String getColumnName(int column) {
		return mColumnNames[column];
	}

	@Override
	public int getColumnCount() {
		return mColumnNames.length;
	}

	@Override
	public int getRowCount() {
		return mDownloadList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DownloadItem item = (DownloadItem) mDownloadList.get(rowIndex);
		mStringBuilder.delete(0, mStringBuilder.length());

		switch (columnIndex) {
		case COLUMN_NAME:
			mStringBuilder.append(item.getName());
			break;
			
		case COLUMN_DIR:
			mStringBuilder.append(item.getDir());
			break;
			
		case COLUMN_SIZE:
			mStringBuilder.append(new BigDecimal(item.getSize() / 1024 / 1024.0F).setScale(2, BigDecimal.ROUND_HALF_UP));
			mStringBuilder.append(" MB");
			break;
			
		case COLUMN_SPEED:
			int speed = item.getSpeed();
			if (speed != -1) {
				if (speed < 1) {
					mStringBuilder.append("<1");
				} else {
					mStringBuilder.append(speed);
				}
				mStringBuilder.append(" KB/s");
			} else {
				mStringBuilder.append("");
			}
			break;
			
		case COLUMN_PROGRESS:
			int status = item.getStatus();
			
			if (status == DownloadItem.WAITING) {
				mStringBuilder.append("等待中");
			} else if (status == DownloadItem.COMPLETED) {
				mStringBuilder.append("下载完成");
			} else if (status == DownloadItem.FAILED) {
				mStringBuilder.append("下载失败");
			} else if (status == DownloadItem.STARTED) {
				mStringBuilder.append(getPercent(item.getPos(), item.getSize())).append(" --正在下载");
			} else if (status == DownloadItem.PAUSED) {
				mStringBuilder.append(getPercent(item.getPos(), item.getSize())).append(" --已暂停");
			} else {
				mStringBuilder.append("未知错误");
			}
			break;
		}
		
		return mStringBuilder.toString();
	}

	public void add(DownloadItem item) {
		mDownloadList.add(item);
		fireTableRowsInserted(mDownloadList.indexOf(item), mDownloadList.indexOf(item));
	}

	public void add(List<DownloadItem> list) {
		mDownloadList.addAll(list);
		fireTableRowsInserted(mDownloadList.indexOf(list.get(0)), mDownloadList.indexOf(list.get(list.size() - 1)));
	}

	public void remove(int index) {
		mDownloadList.remove(index);
		fireTableRowsDeleted(index, index);
	}

	public void clear() {
		int count = mDownloadList.size();
		mDownloadList.clear();
		fireTableRowsDeleted(0, count);
	}

	public List<DownloadItem> getDownloadList() {
		List<DownloadItem> list = new ArrayList<DownloadItem>();
		for (DownloadItem item : mDownloadList) {
			list.add(item);
		}
		return list;
	}
	
	public DownloadItem getDownloadItem(int index) {
		return mDownloadList.get(index);
	}
	
	public void update(int column, DownloadItem item) {
		fireTableCellUpdated(mDownloadList.indexOf(item), column);
	}

	private String getPercent(long downloadedBytes, long size) {
		if (size == 0) {
			return "0%";
		}
		return mPercentFormat.format((float) downloadedBytes / size);
	}
	
}