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
import javax.swing.JPanel;
import javax.swing.JTextField;

import icg.ChooseFileDialog.ChooseTarget;
import icg.frame.GraphLabel;

/*
 *
 * */
public class GraphViewFrame extends JFrame {
	public enum GraphKind{
		TwoD_Graph(new TwoD_GraphPainter()),
		Dispersion_Graph(new Dispersion_GraphPainter());

		GraphKind(GraphPainter painter){
			this.painter = painter;
		}
		public GraphPainter painter;
	}

	private GraphLabel graphLb;
	private CardLayout LayoutOfPanel = new CardLayout();
	private JPanel cardPanel = new JPanel();

	private GraphPainter currentPainter;

	public GraphViewFrame(String title){
		setTitle(title);
		setBounds(50,50,600,600);
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
		//setPainter(GraphKind.TwoD_Graph.painter);
		graphLb.setOpaque(true);
		graphLb.setBackground(Color.red);
		graphLb.setReactiveToClick(true);
		graphLb.setPreferredSize(new Dimension(250,250));
		LayoutOfFrame.setComponent(graphLb, 1, 0, 1, 1, 0, 1, GridBagConstraints.CENTER);
		add(graphLb);


		cardPanel.setLayout(LayoutOfPanel);
		ComponentSetter LayoutOfCardsPanel = new ComponentSetter();
		//------------------------------------------------------------------------
		//--------------------カードパネルその1-----------------------------------
		//------------------------------------------------------------------------
		JPanel startCard = new JPanel();
		cardPanel.add(startCard, "card1");

		JButton startDispertionGraphBtn = new JButton("落下分散の図を作成");
		startDispertionGraphBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LayoutOfPanel.show(cardPanel, "card2");
				setPainter(GraphKind.Dispersion_Graph.painter);
				graphLb.repaint();
			}
		});
		LayoutOfCardsPanel.setComponent(startDispertionGraphBtn, 0, 0, 1, 1, 0, 1, GridBagConstraints.CENTER);
		startCard.add(startDispertionGraphBtn);

		JButton start2DGraphBtn = new JButton("数値データのグラフを作成");
		start2DGraphBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				try (FileImageOutputStream output = new FileImageOutputStream(new File("D:/ゆうき/AAA.jpg"))) {

					BufferedImage readImage = ImageIO.read(new File("D:/ゆうき/キャプチャ10.jpg"));

					Graphics graphics = readImage.createGraphics();

					currentPainter.graphPaint(graphics);

					ImageWriter writeImage = ImageIO.getImageWritersByFormatName("jpeg").next();
					ImageWriteParam writeParam = writeImage.getDefaultWriteParam();
					writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					writeParam.setCompressionQuality(1.0f);
					writeImage.setOutput(output);
					writeImage.write(null, new IIOImage(readImage, null, null), writeParam);
					writeImage.dispose();
				} catch (IOException exc) {
					exc.printStackTrace();
				}
				*/
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
		//--------------------カードパネルその1-----------------------------------
		//------------------------------------------------------------------------
		JPanel dispersion_setConfigCard = new JPanel();
		cardPanel.add(dispersion_setConfigCard, "card2");

		JTextField MapFilePathText = new JTextField();
		MapFilePathText.setEditable(false);
		LayoutOfCardsPanel.setFill(GridBagConstraints.HORIZONTAL);
		LayoutOfCardsPanel.setComponent(MapFilePathText, 0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
		dispersion_setConfigCard.add(MapFilePathText);

		JButton MapFileSelectBtn = new JButton("選択");
		MapFileSelectBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedFile = ChooseFileDialog.choose(GraphViewFrame.this, ChooseTarget.ImageFileOnly, ".", "地図画像の選択");

			}
		});
		LayoutOfCardsPanel.setComponent(MapFileSelectBtn, 1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER);
		dispersion_setConfigCard.add(MapFileSelectBtn);



	}

	private void setPainter(GraphPainter painter) {
		currentPainter = painter;
		graphLb.setGraphPainter(painter);
	}

}
