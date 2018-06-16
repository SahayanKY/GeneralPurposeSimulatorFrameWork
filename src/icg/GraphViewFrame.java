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
		setBounds(50,50,1000,900);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setComponent();
		setVisible(true);
	}

	private void setComponent() {
		ComponentSetter LayoutOfFrame = new ComponentSetter();
		setLayout(LayoutOfFrame);

		//カードレイアウトのパネルの追加
		cardPanel.setPreferredSize(new Dimension(250,250));
		LayoutOfFrame.setComponent(cardPanel, 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
		add(cardPanel);
		//グラフ(画像)を描画するラベルの追加
		graphLb = new GraphLabel();
		graphLb.setOpaque(true);
		graphLb.setBackground(Color.white);
		graphLb.setReactiveToClick(true);
		graphLb.setPreferredSize(new Dimension(750,750));
		LayoutOfFrame.setComponent(graphLb, 1, 0, 1, 1, 0, 1, GridBagConstraints.CENTER);
		add(graphLb);


		cardPanel.setLayout(LayoutOfPanel);
		ComponentSetter LayoutOfCardsPanel = new ComponentSetter();
		//------------------------------------------------------------------------
		//--------------------カードパネルその1--初期画面-------------------------
		//------------------------------------------------------------------------
		JPanel startCard = new JPanel();
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
		LayoutOfCardsPanel.setComponent(startDispertionGraphBtn, 0, 0, 1, 1, 0, 1, GridBagConstraints.CENTER);
		startCard.add(startDispertionGraphBtn);

		JButton start2DGraphBtn = new JButton("数値データのグラフを作成");
		start2DGraphBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		LayoutOfCardsPanel.setComponent(start2DGraphBtn, 0, 1, 1, 1, 0, 1, GridBagConstraints.CENTER);
		startCard.add(start2DGraphBtn);

		JButton exitBtn = new JButton("終了");
		exitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GraphViewFrame.this.dispose();
			}
		});
		LayoutOfCardsPanel.setComponent(exitBtn, 0, 2, 1, 1, 0, 1, GridBagConstraints.CENTER);
		startCard.add(exitBtn);


		//------------------------------------------------------------------------
		//--------------------カードパネルその2--落下分散画面---------------------
		//------------------------------------------------------------------------
		JPanel dispersion_setConfigCard = new JPanel();
		cardPanel.add(dispersion_setConfigCard, "card2");

		JTextField MapFilePathText = new JTextField();
		MapFilePathText.setEditable(false);
		MapFilePathText.setPreferredSize(new Dimension(170,30));
		LayoutOfCardsPanel.setFill(GridBagConstraints.HORIZONTAL);
		LayoutOfCardsPanel.setComponent(MapFilePathText, 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
		dispersion_setConfigCard.add(MapFilePathText);

		JButton MapFileSelectBtn = new JButton("選択");
		JButton saveBtn = new JButton("保存");

		MapFileSelectBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedFile = ChooseFileDialog.choose(GraphViewFrame.this, ChooseTarget.ImageFileOnly, ".", "地図画像の選択");
				if(selectedFile == null) {
					return;
				}
				MapFilePathText.setText(selectedFile.toString());
				((Dispersion_GraphPainter) currentPainter).setMapImage(selectedFile);
				graphLb.repaint();
				saveBtn.setEnabled(true);
			}
		});
		LayoutOfCardsPanel.setComponent(MapFileSelectBtn, 1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
		dispersion_setConfigCard.add(MapFileSelectBtn);


		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedFile = ChooseFileDialog.choose(GraphViewFrame.this, ChooseTarget.ImageFileOnly, ".", "保存する");
				if(selectedFile.exists()) {
					JOptionPane.showMessageDialog(GraphViewFrame.this, "同名のファイルが既に存在します" , "エラー", JOptionPane.ERROR_MESSAGE);
					return;
				}
				currentPainter.savePaint(selectedFile);
			}

		});
		saveBtn.setEnabled(false);
		LayoutOfCardsPanel.setComponent(saveBtn, 0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER);
		dispersion_setConfigCard.add(saveBtn);

	}

	private void setPainter(GraphPainter painter) {
		currentPainter = painter;
		graphLb.setGraphPainter(painter);
	}

}
