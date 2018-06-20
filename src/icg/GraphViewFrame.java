package icg;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import icg.ChooseFileDialog.ChoosePurpose;
import icg.ChooseFileDialog.ChooseTarget;
import icg.frame.GraphLabel;

/*
 *
 * */
public class GraphViewFrame extends JFrame {
	private GraphLabel graphLb;
	private CardLayout LayoutOfPanel = new CardLayout();
	private JPanel cardPanel = new JPanel();

	private GraphPainter currentPainter;

	public GraphViewFrame(String title){
		setTitle(title);
		setBounds(50,50,1000,620);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		setComponent();
		setVisible(true);
	}

	private void setComponent() {
		ComponentSetter LayoutOfFrame = new ComponentSetter();
		setLayout(LayoutOfFrame);

		//カードレイアウトのパネルの追加
		cardPanel.setPreferredSize(new Dimension(380,280));
		LayoutOfFrame.setComponent(cardPanel, 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
		add(cardPanel);
		//グラフ(画像)を描画するラベルの追加
		graphLb = new GraphLabel();
		graphLb.setOpaque(true);
		graphLb.setBackground(Color.white);
		graphLb.setReactiveToClick(true);
		graphLb.setPreferredSize(new Dimension(580,580));
		LayoutOfFrame.setComponent(graphLb, 1, 0, 1, 1, 0, 1, GridBagConstraints.CENTER);
		add(graphLb);


		cardPanel.setLayout(LayoutOfPanel);
		ComponentSetter LayoutOfCardsPanel = new ComponentSetter();
		//------------------------------------------------------------------------
		//--------------------カードパネルその1--初期画面-------------------------
		//------------------------------------------------------------------------
		JPanel startCard = new JPanel();
		startCard.setLayout(LayoutOfCardsPanel);
		cardPanel.add(startCard, "card1");

		JButton startDispertionGraphBtn = new JButton("落下分散の図を作成");
		startDispertionGraphBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LayoutOfPanel.show(cardPanel, "card2");
				setPainter(new Dispersion_GraphPainter(graphLb));
				graphLb.repaint();
			}
		});
		LayoutOfCardsPanel.setComponent(startDispertionGraphBtn, 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
		startCard.add(startDispertionGraphBtn);

		JButton start2DGraphBtn = new JButton("数値データのグラフを作成");
		start2DGraphBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		LayoutOfCardsPanel.setComponent(start2DGraphBtn, 0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER);
		startCard.add(start2DGraphBtn);

		JButton exitBtn = new JButton("終了");
		exitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GraphViewFrame.this.dispose();
			}
		});
		LayoutOfCardsPanel.setComponent(exitBtn, 0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER);
		startCard.add(exitBtn);


		//------------------------------------------------------------------------
		//--------------------カードパネルその2--落下分散画面---------------------
		//------------------------------------------------------------------------
		JPanel dispersion_setConfigCard = new CardPanel() {
			{
				JButton dispersion_nextBtn = new JButton("次に進む");
				JButton dispersion_backBtn = new JButton("やり直す");
				JButton setDispersionDataBtn = new JButton("落下分散データファイルを選択");
				JButton saveBtn = new JButton("保存");
				JTextField mapScaleInputText = new JTextField();

				JTextField MapFilePathText = new JTextField();
				MapFilePathText.setEditable(false);
				MapFilePathText.setPreferredSize(new Dimension(170,30));
				LayoutOfCardsPanel.setFill(GridBagConstraints.HORIZONTAL);
				LayoutOfCardsPanel.setComponent(MapFilePathText, 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
				compList.add(MapFilePathText);

				JButton selectMapImageBtn = new JButton("画像を選択");
				selectMapImageBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						File selectedFile = ChooseFileDialog.choose(GraphViewFrame.this, ChooseTarget.ImageFileOnly, ChoosePurpose.ToSelect, ".", "地図画像の選択");
						if(selectedFile == null) {
							return;
						}
						MapFilePathText.setText(selectedFile.toString());
						((Dispersion_GraphPainter) currentPainter).setMapImage(selectedFile);
						graphLb.repaint();
						JOptionPane.showConfirmDialog(GraphViewFrame.this, "画像の縮尺の端2点をクリックで選択し、その長さを入力してください。", "確認", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
						dispersion_nextBtn.setEnabled(true);
						dispersion_backBtn.setEnabled(true);
					}
				});
				LayoutOfCardsPanel.setComponent(selectMapImageBtn, 1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
				compList.add(selectMapImageBtn);


				mapScaleInputText.setPreferredSize(new Dimension(100,30));
				LayoutOfCardsPanel.setComponent(mapScaleInputText, 0, 1, 2, 1, 0, 0, GridBagConstraints.CENTER);
				compList.add(mapScaleInputText);


				dispersion_backBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int ans = JOptionPane.showConfirmDialog(GraphViewFrame.this, "画像上の点を消去し、やり直しますか？", "確認", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(ans == JOptionPane.OK_OPTION) {
							((Dispersion_GraphPainter)currentPainter).resetTemporaryImage();
							graphLb.repaint();
						}
					}
				});
				dispersion_backBtn.setEnabled(false);
				LayoutOfCardsPanel.setComponent(dispersion_backBtn, 0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER);
				compList.add(dispersion_backBtn);


				dispersion_nextBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							String mapScaleStr = mapScaleInputText.getText();
							if(mapScaleStr == null) {
								throw new IllegalArgumentException("縮尺が未入力です。");
							}
							PhysicalQuantity scaleQ = new PhysicalQuantity(mapScaleStr);
							if(!scaleQ.equalsDimension(new PhysicalQuantity("m"))) {
								//入力された次元がメートルでない
								throw new IllegalArgumentException("縮尺の入力値が異常です。");
							}
						}catch(IllegalArgumentException exc) {
							JOptionPane.showMessageDialog(GraphViewFrame.this, exc , "エラー", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				dispersion_nextBtn.setEnabled(false);
				LayoutOfCardsPanel.setComponent(dispersion_nextBtn, 1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER);
				compList.add(dispersion_nextBtn);


				setDispersionDataBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

					}
				});
				setDispersionDataBtn.setEnabled(false);
				LayoutOfCardsPanel.setComponent(setDispersionDataBtn, 0, 3, 1, 1, 0, 0, GridBagConstraints.CENTER);
				compList.add(setDispersionDataBtn);

				saveBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						File selectedFile = ChooseFileDialog.choose(GraphViewFrame.this, ChooseTarget.ImageFileOnly, ChoosePurpose.ToSave, ".", "保存する");
						if(selectedFile == null) {
							return;
						}
						if(selectedFile.exists()) {
							JOptionPane.showMessageDialog(GraphViewFrame.this, "同名のファイルが既に存在します" , "エラー", JOptionPane.ERROR_MESSAGE);
							return;
						}
						currentPainter.savePaint(selectedFile);
					}

				});
				saveBtn.setEnabled(false);
				LayoutOfCardsPanel.setComponent(saveBtn, 0, 4, 1, 1, 0, 0, GridBagConstraints.CENTER);
				compList.add(saveBtn);

			}

			@Override
			public void actionPerformed(ActionEvent e) {

			}
		};
		dispersion_setConfigCard.setLayout(LayoutOfCardsPanel);
		((CardPanel)dispersion_setConfigCard).addComponents();
		cardPanel.add(dispersion_setConfigCard, "card2");




	}

	private void setPainter(GraphPainter painter) {
		currentPainter = painter;
		graphLb.setGraphPainter(painter);
	}

}
