package icg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class CardPanel extends JPanel implements ActionListener {
	protected ArrayList<JComponent> compList = new ArrayList<>();
	public abstract void actionPerformed(ActionEvent e);
	public void addComponents() {
		for(JComponent c:compList) {
			add(c);
		}
	}
}
