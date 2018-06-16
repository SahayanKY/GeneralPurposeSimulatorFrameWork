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
		while(true) {
			int selected = chooser.showOpenDialog(c);
			if(selected == JFileChooser.APPROVE_OPTION) {
				if((selectedFile = chooser.getSelectedFile()) == null) {
					continue;
				}
				if(target.equals(ChooseTarget.DirectoryOnly) && !selectedFile.isDirectory()) {
					continue;
				}
				if(!chooser.getFileFilter().accept(selectedFile)) {
					if(purpose.equals(ChoosePurpose.ToSave)) {
						selectedFile = new File(selectedFile.toString() +".jpg");
					}else {
						continue;
					}
				}
				break;
			}else {
				selectedFile = null;
				break;
			}
		}

		return selectedFile;

	}
}
