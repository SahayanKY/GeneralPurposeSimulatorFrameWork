package simulation.frame;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import icg.ComponentSetter;

public class ProgressInformFrame extends JFrame {
	private boolean CalcurationHadFinished = false;
	private JLabel progressRateLabel=new JLabel("100%");

	public ProgressInformFrame(){
		//フレームの設定
		setTitle("計算進行状況");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String message;
				if(CalcurationHadFinished) {
					message = "このままシミュレーションを終了しますか?";
				}else {
					message = "計算が終了していません。\nこのまま終了しますか?";
				}
				int ans = JOptionPane.showConfirmDialog(ProgressInformFrame.this, message, "計算終了の確認", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				System.out.println(ans);
				if(ans == JOptionPane.YES_OPTION) {
					System.out.println("プログラムによる終了処理の実行");
					System.exit(0);
				}
			}
		});
		setBounds(200,150,400,200);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		setComponent();
		setVisible(true);
	}


	/*
	 * このフレームインスタンスに貼り付けるコンポーネントの設定を行う
	 * */
	private void setComponent() {
		ComponentSetter layout = new ComponentSetter();
		setLayout(layout);

		Font f = new Font(Font.SERIF, Font.BOLD, 24);


		JLabel label = new JLabel("進行状況");
		label.setFont(f);
		label.setHorizontalAlignment(JLabel.CENTER);
		layout.setComponent(label, 0,0,2,1,1,0.5,GridBagConstraints.SOUTH);
		add(label);



		progressRateLabel.setFont(f);
		progressRateLabel.setHorizontalAlignment(JLabel.CENTER);
		layout.setComponent(progressRateLabel, 0,1,2,1,1,0,GridBagConstraints.CENTER);
		add(progressRateLabel);



		JButton button = new JButton("計算停止");
		layout.setComponent(button, 0,2,1,1,0.5,0,GridBagConstraints.NORTHEAST);
		add(button);

		button = new JButton("新規計算");
		button.setEnabled(false);
		layout.setComponent(button, 1,2,1,1,0.5,0,GridBagConstraints.NORTHWEST);
		add(button);

		button = new JButton("計算結果を見る");
		button.setEnabled(true);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showResult();
			}
		});
		layout.setComponent(button, 0,3,2,1,0.5,0.2,GridBagConstraints.NORTH);
		add(button);
	}

	/*
	 * 計算結果を保存してあるディレクトリを表示
	 * */
	private void showResult() {
		try {
		    Runtime rt = Runtime.getRuntime();
		    String cmd = "explorer D:\\ゆうき";
		    rt.exec(cmd);
		} catch (IOException exc) {
			//例外処理
		}
	}
}
