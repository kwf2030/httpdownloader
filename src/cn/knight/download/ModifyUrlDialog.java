package cn.knight.download;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ModifyUrlDialog extends JDialog {
	
	private static final long serialVersionUID = 8758812434942680611L;
	
	public static final int RESULT_OK = 1;
	public static final int RESULT_CANCEL = 2;
	
	private String mNewUrl;

	private int mResult;

	public ModifyUrlDialog(Frame frame, String url) {
		super(frame, true);
		Font font = Utils.FONT;

		setBounds(new Rectangle(0, 0, 400, 150));
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		getContentPane().setLayout(null);
		
		final JTextArea mUrlArea = new JTextArea();
		mUrlArea.setBounds(10, 15, 370, 60);
		mUrlArea.setFont(font);
		mUrlArea.setLineWrap(true);
		mUrlArea.setText(url);

		JScrollPane scrollPane = new JScrollPane(mUrlArea);
		scrollPane.setBounds(10, 15, 370, 60);
		getContentPane().add(scrollPane);

		JButton mConfirmBtn = new JButton("确定");
		mConfirmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mNewUrl = mUrlArea.getText();
				dismiss(RESULT_OK);
			}
		});
		mConfirmBtn.setBounds(90, 90, 90, 25);
		mConfirmBtn.setFont(font);
		getContentPane().add(mConfirmBtn);

		JButton mCancelBtn = new JButton("取消");
		mCancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dismiss(RESULT_CANCEL);
			}
		});
		mCancelBtn.setBounds(210, 90, 90, 25);
		mCancelBtn.setFont(font);
		getContentPane().add(mCancelBtn);

		setVisible(true);
	}
	
	private void dismiss(int result) {
		mResult = result;
		dispose();
	}
	
	public int getResult() {
		return mResult;
	}
	
	public String getNewUrl() {
		return mNewUrl;
	}
	
}