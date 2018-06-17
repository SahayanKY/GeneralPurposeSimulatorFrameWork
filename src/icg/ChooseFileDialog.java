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
	 * @param
	 * c このComponentインスタンスの上にダイアログを表示する
	 * target ダイアログにて選ぶ対象をEnum型ChooseTargetに従って指定する。
	 * purpose 選択の目的をEnum型ChoosePurposeに従って指定する。
	 * currentPath ダイアログの初期位置をString型で指定する。"."でカレントディレクトリを指定できる。
	 * title このダイアログに付けるタイトルを指定する。
	 * @return 選択されたFileインスタンス。選択されずにダイアログが閉じられた場合、nullが返される。
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
				if(purpose.equals(ChoosePurpose.ToSelect)) {
					chooser.addChoosableFileFilter(new FileNameExtensionFilter("画像ファイル", "png", "jpg", "Jpeg", "GIF"));
				}else {
					chooser.addChoosableFileFilter(new FileNameExtensionFilter("Portable Network Graphics File(PNG)(*.png)","png"));
					chooser.addChoosableFileFilter(new FileNameExtensionFilter("Graphics Interchange Format File(*.GIF)","GIF"));
					chooser.addChoosableFileFilter(new FileNameExtensionFilter("Joint Photographic Experts Group File(*.JPG)","jpg","jpeg"));
				}
				break;
		}

		//ApproveButtonのテキストを変える
		if(purpose.equals(ChoosePurpose.ToSave)) {
			chooser.setApproveButtonText("保存");
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
				FileNameExtensionFilter filter = (FileNameExtensionFilter) chooser.getFileFilter();
				switch(purpose) {
					case ToSave:
						if(!filter.accept(selectedFile)) {
							//拡張子がついていなかった場合
							String extensions[] = filter.getExtensions();
							selectedFile = new File(selectedFile.toString()+"."+extensions[0]);
						}
						break chooseLoop;

					case ToSelect:
						if(filter.accept(selectedFile) && selectedFile.exists()) {
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
