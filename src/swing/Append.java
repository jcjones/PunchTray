package swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.atticlabs.zonelayout.swing.ZoneLayout;
import com.atticlabs.zonelayout.swing.ZoneLayoutFactory;

import engine.PunchEngine;

public class Append extends JPanel {
	private static final long serialVersionUID = -9009320840236434719L;
	
	private final JTextArea memoField;
	private final JTextField timeElapsedField;
	private final ActionListener update;

	public Append(final PunchEngine engine) {
		memoField = new JTextArea(4, 40);
		timeElapsedField = new JTextField(40);

		createAndAddComponents();

		update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeElapsedField.setText("Time worked today: " + PunchEngine.formatMinutes(engine.minutesWorkedToday()));
			}
		};
	}
	
	public ActionListener getUpdateListener() {
		return update;
	}

	private void createAndAddComponents() {
		ZoneLayout layout = ZoneLayoutFactory.newZoneLayout();

		layout.addRow("cc2d~d");
		layout.addRow("6.....");
		layout.addRow("e+...e");

		setLayout(layout);

		timeElapsedField.setEditable(false);

		add(new JLabel("Memo:"), "c");
		add(new JScrollPane(memoField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "d");
		add(timeElapsedField, "e");
	}

	public String getMemo() {
		return memoField.getText();
	}

	public void setMemo(String memo) {
		memoField.setText(memo);
	}
}
