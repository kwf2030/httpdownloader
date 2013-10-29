package cn.knight.download;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NewTaskDialog extends JDialog {
	
	private static final long serialVersionUID = 6397602019565545770L;
	
	public static final int RESULT_OK = 1;
	public static final int RESULT_CANCEL = 2;
	
	private JTextArea mUrlArea = new JTextArea();
	private JTextField mNameField = new JTextField();
	private JTextField mDirField = new JTextField();
	private JTextField mProxyField = new JTextField();
	private JTextField mPortField = new JTextField();
	private JTextField mBlockField = new JTextField();
	
	private JButton mBrowseBtn = new JButton();
	private JButton mConfirmBtn = new JButton();
	private JButton mCancelBtn = new JButton();
	
	private DownloadItem mDownloadItem = new DownloadItem();

	private int mResult;

	public NewTaskDialog(Frame frame) {
		super(frame, true);

		setBounds(new Rectangle(400, 300));
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		getContentPane().setLayout(null);
		
		Font font = Utils.FONT;

		JLabel urlLabel = new JLabel("下载地址");
		urlLabel.setBounds(10, 10, 100, 20);
		urlLabel.setFont(font);
		getContentPane().add(urlLabel);

		mUrlArea = new JTextArea();
		mUrlArea.setBounds(10, 35, 360, 60);
		mUrlArea.setFont(font);
		mUrlArea.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(mUrlArea);
		scrollPane.setBounds(10, 35, 370, 60);
		getContentPane().add(scrollPane);

		JLabel nameLabel = new JLabel("文件名");
		nameLabel.setBounds(10, 107, 50, 20);
		nameLabel.setFont(font);
		getContentPane().add(nameLabel);

		mNameField = new JTextField();
		mNameField.setBounds(60, 105, 235, 25);
		mNameField.setFont(font);
		mNameField.setColumns(10);
		mNameField.setText(mDownloadItem.getName());
		getContentPane().add(mNameField);

		JLabel dirLabel = new JLabel("路径");
		dirLabel.setBounds(10, 137, 40, 20);
		dirLabel.setFont(font);
		getContentPane().add(dirLabel);

		mDirField = new JTextField();
		mDirField.setBounds(60, 135, 235, 25);
		mDirField.setFont(font);
		mDirField.setColumns(10);
		mDirField.setText(mDownloadItem.getDir());
		getContentPane().add(mDirField);

		JLabel proxyLabel = new JLabel("代理");
		proxyLabel.setBounds(10, 167, 40, 20);
		proxyLabel.setFont(font);
		getContentPane().add(proxyLabel);

		mProxyField = new JTextField();
		mProxyField.setBounds(60, 165, 180, 25);
		mProxyField.setFont(font);
		mProxyField.setColumns(10);
		mProxyField.setText(mDownloadItem.getProxy());
		getContentPane().add(mProxyField);

		JLabel portLabel = new JLabel("端口");
		portLabel.setBounds(285, 167, 40, 20);
		portLabel.setFont(font);
		getContentPane().add(portLabel);

		mPortField = new JTextField();
		mPortField.setBounds(325, 165, 60, 25);
		mPortField.setFont(font);
		mPortField.setColumns(10);
		mPortField.setText(String.valueOf(mDownloadItem.getPort()));
		getContentPane().add(mPortField);

		JLabel blockLabel = new JLabel("块大小");
		blockLabel.setBounds(10, 197, 50, 20);
		blockLabel.setFont(font);
		getContentPane().add(blockLabel);

		mBlockField = new JTextField();
		mBlockField.setBounds(60, 197, 80, 25);
		mBlockField.setFont(font);
		mBlockField.setColumns(10);
		mBlockField.setText(String.valueOf(mDownloadItem.getBlock()));
		getContentPane().add(mBlockField);

		mBrowseBtn.setText("浏览");
		mBrowseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser(mDownloadItem.getDir());
				c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (c.showSaveDialog(NewTaskDialog.this) == JFileChooser.APPROVE_OPTION) {
					mDirField.setText(c.getSelectedFile().getAbsolutePath());
				}
			}
		});
		mBrowseBtn.setBounds(305, 135, 80, 25);
		mBrowseBtn.setFont(font);
		getContentPane().add(mBrowseBtn);

		mConfirmBtn.setText("确定");
		mConfirmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!collectInfo()) {
					return;
				}
				dismiss(RESULT_OK);
			}
		});
		mConfirmBtn.setBounds(80, 235, 90, 25);
		mConfirmBtn.setFont(font);
		getContentPane().add(mConfirmBtn);

		mCancelBtn.setText("取消");
		mCancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dismiss(RESULT_CANCEL);
			}
		});
		mCancelBtn.setBounds(220, 235, 90, 25);
		mCancelBtn.setFont(font);
		getContentPane().add(mCancelBtn);

		setVisible(true);
	}

	private void dismiss(int result) {
		mResult = result;
		dispose();
	}

	private boolean collectInfo() {
		String url = mUrlArea.getText();
		String filename = mNameField.getText();
		String dir = mDirField.getText();
		String proxy = mProxyField.getText();
		String port = mPortField.getText();
		String block = mBlockField.getText();

		if (checkInputNull(new String[] { url, filename, dir, block })) {
			return false;
		}

		mDownloadItem.setUrl(url);
		mDownloadItem.setName(filename);
		mDownloadItem.setDir(dir);
		if ((proxy != null) && (proxy.length() > 0)) {
			mDownloadItem.setProxy(proxy);
		} else {
			mDownloadItem.setProxy(null);
		}
		if ((port != null) && (port.length() > 0)) {
			mDownloadItem.setPort(Integer.parseInt(port));
		} else {
			mDownloadItem.setPort(0);
		}
		mDownloadItem.setBlock(Long.parseLong(block));
		return true;
	}

	private boolean checkInputNull(String[] input) {
		for (String s : input) {
			if ((s == null) || (s.length() <= 0)) {
				return true;
			}
		}
		return false;
	}

	public int getResult() {
		return mResult;
	}

	public DownloadItem getDownloadItem() {
		return mDownloadItem;
	}
	
}