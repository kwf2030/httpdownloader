package cn.knight.download;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class HttpDownloadUI extends JFrame {
	
	private static final long serialVersionUID = 7814388808089165890L;
	
	private static final String ICON_FIME_NAME = "icon.png";
	
	private JTable mDownloadList = new JTable();
	
	private DownloadManager mDownloadMgr;
	
	private int mModifiedIndex;
	
	private TrayIcon mTrayIcon;

	public HttpDownloadUI() {
		final DownloadListModel model = new DownloadListModel(new String[] {"文件名", "目录", "大小", "速度", "进度"});
		mDownloadMgr = DownloadManager.getInstance(model);

		setBounds(new Rectangle(550, 400));
		setLocationRelativeTo(null);
		setTitle("HTTP Downloader");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mDownloadMgr.saveProgress();
				System.exit(0);
			}

			@Override
			public void windowIconified(WindowEvent e) {
				if (SystemTray.isSupported()) {
					if (mTrayIcon == null) {
						Image ico = getToolkit().getImage(getClass().getClassLoader().getResource(ICON_FIME_NAME));
						mTrayIcon = new TrayIcon(ico, "HTTP Download for ZTE");
						mTrayIcon.setImageAutoSize(true);
						mTrayIcon.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (e.getClickCount() == 2) {
									SystemTray.getSystemTray().remove(mTrayIcon);
									setExtendedState(JFrame.NORMAL);
									HttpDownloadUI.this.setVisible(true);
								}
							}
						});
					}

					try {
						SystemTray.getSystemTray().add(mTrayIcon);
					} catch (AWTException ex) {
						Utils.exception(ex);
					}

					setExtendedState(JFrame.ICONIFIED);
					HttpDownloadUI.this.setVisible(false);
				}
			}
		});
		
		Font font = Utils.FONT;

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);

		JButton addBtn = new JButton("添加");
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewTaskDialog dialog = new NewTaskDialog(HttpDownloadUI.this);
				if (dialog.getResult() == NewTaskDialog.RESULT_OK) {
					DownloadItem item = dialog.getDownloadItem();
					mDownloadMgr.addTask(item);
				}
			}
		});
		addBtn.setFont(font);
		panel.add(addBtn);

		JButton removeBtn = new JButton("删除");
		removeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = mDownloadList.getSelectedRow();
				if (row == -1) {
					return;
				}
				mDownloadMgr.removeTask(row);
			}
		});
		removeBtn.setFont(font);
		panel.add(removeBtn);

		JButton resumeBtn = new JButton("开始");
		resumeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = mDownloadList.getSelectedRow();
				if (row == -1) {
					return;
				}
				mDownloadMgr.resumeTask(row);
			}
		});
		resumeBtn.setFont(font);
		panel.add(resumeBtn);

		JButton pauseBtn = new JButton("暂停");
		pauseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = mDownloadList.getSelectedRow();
				if (row == -1) {
					return;
				}
				mDownloadMgr.pauseTask(row);
			}
		});
		pauseBtn.setFont(font);
		panel.add(pauseBtn);
		
		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem modifyUrlItem = new JMenuItem("重定位下载地址");
		modifyUrlItem.setFont(font);
		modifyUrlItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ModifyUrlDialog dialog = new ModifyUrlDialog(HttpDownloadUI.this, model.getDownloadItem(mModifiedIndex).getUrl());
				if (dialog.getResult() == ModifyUrlDialog.RESULT_OK) {
					mDownloadMgr.changeUrl(mModifiedIndex, dialog.getNewUrl());
				}
			}
		});
		popupMenu.add(modifyUrlItem);

		mDownloadList.setModel(model);
		mDownloadList.setFont(font);
		mDownloadList.getTableHeader().setFont(font);
		mDownloadList.setRowHeight(25);
		mDownloadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mDownloadList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					mModifiedIndex = mDownloadList.rowAtPoint(e.getPoint());
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(mDownloadList);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		setVisible(true);
	}

	public static void main(String[] args) {
		new HttpDownloadUI();
		Utils.deleteExceptionFile();
	}
	
}