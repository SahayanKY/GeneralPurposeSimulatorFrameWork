package icg;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

/*
 * ファイル、またはディレクトリを選択させるダイアログクラス
 * */
public abstract class ChooseFileDialog {
	public enum ChooseTarget{
		DirectoryOnly,ThrustFileOnly,PropertiesFileOnly,ImageFileOnly;
	}

	public enum ChoosePurpose {
		ToSave,ToSelect;
	}

	/*
	 * コンストラクタで指定した通りのダイアログを表示し、選択結果をFileインスタンスとして返す。
	 * @return 選択されたFileインスタンス
	 * */
	public static File choose(Component c, ChooseTarget target, ChoosePurpose purpose, String currentPath, String title) {
		JFileChooser chooser = new JFileChooser();

		File file = new File(currentPath);
		chooser.setCurrentDirectory(file);
		chooser.setFileView(new FileView() {
			public Icon getIcon(File f) {
				return FileSystemView.getFileSystemView().getSystemIcon(f);
			}
		});
		chooser.setDialogTitle(title);
		chooser.setAcceptAllFileFilterUsed(false);

		switch(target) {
			case DirectoryOnly:
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				break;
			case ThrustFileOnly:
				chooser.addChoosableFileFilter(new FileNameExtensionFilter("thrustファイル","thrust"));
				break;
			case PropertiesFileOnly:
				chooser.addChoosableFileFilter(new FileNameExtensionFilter("propertiesファイル", "properties"));
				break;
			case ImageFileOnly:
				chooser.addChoosableFileFilter(new FileNameExtensionFilter("画像ファイル", "png", "jpg", "Jpeg", "GIF", "bmp"));
				break;
		}

		File selectedFile;
		chooseLoop: while(true) {
			int selected = chooser.showOpenDialog(c);
			if(selected == JFileChooser.APPROVE_OPTION) {
				//選択したときの対応
				if((selectedFile = chooser.getSelectedFile()) == null) {
					//nullなら当然もう一回
					continue chooseLoop;
				}
				if(target.equals(ChooseTarget.DirectoryOnly) && !selectedFile.isDirectory()) {
					//ディレクトリ選択なのにディレクトリを選択してなかったらもう一回
					continue chooseLoop;
				}
				switch(purpose) {
					case ToSave:
						if(!chooser.getFileFilter().accept(selectedFile)) {
							//拡張子がついていなかった場合
							if(target.equals(ChooseTarget.ImageFileOnly)) {
								selectedFile = new File(selectedFile.toString() +".jpg");
							}
						}
						break chooseLoop;

					case ToSelect:
						if(chooser.getFileFilter().accept(selectedFile) && selectedFile.exists()) {
							//選んだファイルがフィルターに即していて、存在すれば終了
							break chooseLoop;
						}
				}
			}else {
				//「×」ボタンや取消ボタンへの対応
				selectedFile = null;
				break chooseLoop;
			}
		}

		return selectedFile;

	}
}
