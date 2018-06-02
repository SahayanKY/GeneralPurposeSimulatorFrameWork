package icg;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

/*
 * ファイル、またはディレクトリを選択させるダイアログクラス
 * */
public class ChooseFileDialog {
	enum ChooseTarget{
		DirectoryOnly,ThrustFileOnly,PropertiesFileOnly;
	}

	private ChooseFileDialog(){	}


	/*
	 * コンストラクタで指定した通りのダイアログを表示し、選択結果をFileインスタンスとして返す。
	 * @return 選択されたFileインスタンス
	 * */
	public static File choose(Component c,ChooseTarget target, String currentPath, String title) {
		JFileChooser chooser = new JFileChooser();

		File file = new File(currentPath);
		chooser.setCurrentDirectory(file);
		chooser.setFileView(new FileView() {
			public Icon getIcon(File f) {
				return FileSystemView.getFileSystemView().getSystemIcon(f);
			}
		});

		if(target.equals(ChooseTarget.DirectoryOnly)) {
			//ディレクトリのみを選べるようにする
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle(title);
		}else {
			String label=null,extension=null;
			switch(target) {
				case ThrustFileOnly:
					label = "THRUSTファイル";
					extension = "thrust";
					break;

				case PropertiesFileOnly:
					label = "PROPERTIESファイル";
					extension = "properties";
					break;
				default:
			}
			//テキストファイルのみを選べるようにする
			FileFilter filter = new FileNameExtensionFilter(label, extension);
			chooser.addChoosableFileFilter(filter);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setDialogTitle(title);
		}


		int selected = chooser.showOpenDialog(c);
		if(selected == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}else {
			return null;
		}

	}
}
