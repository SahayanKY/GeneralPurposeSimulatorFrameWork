package simulation.frame;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import icg.ChooseFileDialog;
import icg.ChooseFileDialog.ChoosePurpose;
import icg.ChooseFileDialog.ChooseTarget;
import icg.ComponentSetter;
import simulation.Simulator;
import simulation.param.Parameter;
import simulation.param.ParameterManager;

public class DataInputFrame extends JFrame implements ActionListener,FocusListener{
	private Simulator simulater;
	private ParameterManager paramManager;
	private CardLayout LayoutOfPanel = new CardLayout();
	private JPanel CardPanel = new JPanel();

	private int contentPaneH,contentPaneW;

	private LinkedHashMap<String, LinkedHashMap<String,JTextField>> dataField = new LinkedHashMap<>();

	private static final String ShowCard = "showCard",
			StartCalculation = "StartCalculation",
			ChangeTextFieldColor = "ChangeTextFieldColor",
			SetExistingInputData = "SetInputData";

	public DataInputFrame(Simulator simulator, int width, int height){
		this.simulater = simulator;
		this.paramManager = simulator.getParameterManager();
		//フレームの設定
		setTitle(simulator.getThisName());
		//内側のサイズをコンストラクタの引数で指定
		this.contentPaneW = width;
		this.contentPaneH = height;
		getContentPane().setPreferredSize(new Dimension(contentPaneW,contentPaneH));
		pack();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		setComponent();
		setVisible(true);
	}

	/*
	 * このフレームインスタンスに貼り付けるコンポーネントの設定
	 * */
	private void setComponent() {
		ComponentSetter LayoutOfFrame = new ComponentSetter();
		setLayout(LayoutOfFrame);

		LinkedHashMap<String,LinkedHashMap<String,Parameter>> map = paramManager.getInputParamMap(true);

		//入力値を既存のファイルから取得し、セットする
		JButton setInputDataButton = new JButton("既存のデータファイルからパラメータをセットする");
		setInputDataButton.addActionListener(this);
		setInputDataButton.setActionCommand(SetExistingInputData);
		LayoutOfFrame.setFill(GridBagConstraints.HORIZONTAL);
		LayoutOfFrame.setComponent(setInputDataButton, 0, 0, 1, 1 , 0, 0, GridBagConstraints.CENTER);
		add(setInputDataButton);

		//パネル変更用のcomboboxの作成
		JComboBox combo = new JComboBox();
		combo.addActionListener(this);
		combo.setActionCommand(ShowCard);
		LayoutOfFrame.setFill(GridBagConstraints.HORIZONTAL);
		LayoutOfFrame.setComponent(combo, 0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER);
		add(combo);

		JButton calcStartButton = new JButton("計算開始");
		calcStartButton.addActionListener(this);
		calcStartButton.setActionCommand(StartCalculation);
		LayoutOfFrame.setComponent(calcStartButton, 0, 3, 1, 1, 0, 0, GridBagConstraints.CENTER);
		add(calcStartButton);


		//カードレイアウトパネルを追加
		CardPanel.setLayout(LayoutOfPanel);
		LayoutOfFrame.setComponent(CardPanel, 0, 2, 1, 1, 0, 1.0d, GridBagConstraints.CENTER);
		add(CardPanel);

		int minCardPanelH = contentPaneH
							-setInputDataButton.getPreferredSize().height
							-combo.getPreferredSize().height
							-calcStartButton.getPreferredSize().height;

		//カードレイアウトパネルに追加するパネルを作成
		//同時にtextFieldをメンバ変数のhashmapに登録しておく
		ComponentSetter LayoutOfCards = new ComponentSetter();
		Dimension d = new Dimension(90,30);
		for(String Name:map.keySet()) {
			//ひとつのparentLabelのパラメータの数が多い場合、
			//複数のパネルに分割する
			//適切な枚数を計算
			int NumberOfPanelForOneParent = map.get(Name).size()*30;
			NumberOfPanelForOneParent = NumberOfPanelForOneParent/minCardPanelH +((NumberOfPanelForOneParent%minCardPanelH == 0)? 0:1);

			LinkedHashMap<String,JTextField> TextFieldMap = new LinkedHashMap<>();
			dataField.put(Name, TextFieldMap);

			int line = minCardPanelH/30;

			int x=0,y=0;
			//各パラメータをその下に貼り付けていく
			JPanel card = null;
			for(String parameterName:map.get(Name).keySet()) {
				if((x==0)||(y == line)) {
					x++;
					y=0;
					card = new JPanel();
					card.setLayout(LayoutOfCards);
					String n = (NumberOfPanelForOneParent == 1)? "":String.valueOf(x);
					CardPanel.add(card, Name+n);
					combo.addItem(Name+n);
				}

				JLabel paramLabel = new JLabel(parameterName);
				LayoutOfCards.setFill(GridBagConstraints.HORIZONTAL);
				LayoutOfCards.setComponent(paramLabel, 0, y, 1, 1, 0.7, 0.1,GridBagConstraints.EAST);
				card.add(paramLabel);

				JTextField text = new JTextField();
				text.setPreferredSize(d);
				text.setBackground(Color.RED);
				text.addFocusListener(this);
				text.setActionCommand(ChangeTextFieldColor);
				LayoutOfCards.setComponent(text, 1, y, 1, 1, 0.3, 0.2, GridBagConstraints.WEST);
				card.add(text);
				TextFieldMap.put(parameterName, text);

				if(map.get(Name).get(parameterName).isNeedFileChooser()) {
					JButton selectFileButton = new JButton("選択");
					selectFileButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							File choosedFile = ChooseFileDialog.choose(DataInputFrame.this, ChooseTarget.ThrustFileOnly, ChoosePurpose.ToSelect, ".", "燃焼データファイルを選択");
							//選択に失敗した場合
							if(choosedFile != null) {
								text.setText(choosedFile.toString().replace("\\", "/"));
								changeTextFieldColor(text);
							}
						}
					});
					LayoutOfCards.setComponent(selectFileButton, 2, y, 1, 1, 0, 0.1, GridBagConstraints.WEST);
					card.add(selectFileButton);
				}
				y++;
			}
		}

	}


	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		switch(cmd) {
			case ShowCard:
				String itemLabel = (String) ((JComboBox)(e.getSource())).getSelectedItem();
				LayoutOfPanel.show(CardPanel,itemLabel);
				break;

			case StartCalculation:
				try {
					//TextFieldにセットされている文字列をParameterに渡す
					LinkedHashMap<String,LinkedHashMap<String,String>> StringDataMap = new LinkedHashMap<>();
					for(String key:dataField.keySet()) {
						LinkedHashMap<String,String> deepMap = new LinkedHashMap<>();
						for(String deepKey:dataField.get(key).keySet()) {
							deepMap.put(deepKey, dataField.get(key).get(deepKey).getText());
						}
						StringDataMap.put(key, deepMap);
					}
					//データのチェックを行い、結果文字列を得る
					String message = paramManager.checkAllInputDataFormat(StringDataMap);
					//受け取った文字列に対応してダイアログを表示、処理を終了
					if(message != null) {
						if(message.startsWith("エラー")) {
							JOptionPane.showMessageDialog(this, message, "続行不能なエラーを検出", JOptionPane.ERROR_MESSAGE);
							break;
						}else if(message.startsWith("要検証")){
							int ans = JOptionPane.showOptionDialog(this, message, "入力値に不備を検出", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"続行", "取消"}, null);
							if(ans == 1 | ans == -1) {
								//計算取消の場合
								break;
							}
						}
					}
					//保存先のディレクトリを選択、nullの場合は処理を終了
					File choosedDirectory = ChooseFileDialog.choose(this, ChooseFileDialog.ChooseTarget.DirectoryOnly, ChoosePurpose.ToSelect, "D:\\ゆうき", "保存先のフォルダを選択");
					if(choosedDirectory == null) {
						break;
					}
					//このフレームの操作を不能にし、次のステージに進める
					this.setEnabled(false);

					simulater.execute(choosedDirectory);
				}catch(NullPointerException | IllegalArgumentException | IOException exc) {
					JOptionPane.showMessageDialog(this, exc, "エラー", JOptionPane.ERROR_MESSAGE);
				}
				break;

			case SetExistingInputData:
				File choosedFile = ChooseFileDialog.choose(this, ChooseTarget.PropertiesFileOnly, ChoosePurpose.ToSelect, "D:\\ゆうき\\大学\\プログラム\\Eclipse\\ICG_Simulation", "既存のプロパティファイルを選択");
				//選択に失敗した場合
				if(choosedFile == null) {
					break;
				}
				try {
					//ファイルを渡し、パラメータをセットさせ、その値をTextFieldにセットする
					paramManager.setData_by(choosedFile);
					LinkedHashMap<String,LinkedHashMap<String,Parameter>> paramMap = paramManager.getInputParamMap(true);
					for(String key:dataField.keySet()) {
						LinkedHashMap<String,JTextField> deepMap = dataField.get(key);
						for(String deepKey:deepMap.keySet()) {
							String data = paramMap.get(key).get(deepKey).getValue();
							JTextField targetTF = deepMap.get(deepKey);
							targetTF.setText(data);
							changeTextFieldColor(targetTF);
						}
					}
				}catch (IOException exc) {
					JOptionPane.showMessageDialog(this, exc, "ファイル操作のエラー", JOptionPane.ERROR_MESSAGE);
				}

				break;

		}
	}

	public void focusGained(FocusEvent e) {
	}
	public void focusLost(FocusEvent e){
		changeTextFieldColor((JTextField) e.getSource());
	}

	/*
	 * 指定されたTextFieldの入力値を取得し、nullまたは空文字なら赤に、
	 * そうでなければ白にJTextFieldの色を変える
	 * */
	private void changeTextFieldColor(JTextField tf) {
		String inputStr = tf.getText();
		if(inputStr == null | inputStr.equals("") | inputStr.matches("[ 　]+")) {
			tf.setBackground(Color.RED);
		}else {
			tf.setBackground(Color.WHITE);
		}
	}
}
