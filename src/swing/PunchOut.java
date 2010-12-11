/* 
 * Copyright 2009 James C. Jones, licensed under the terms of the GNU GPL v2 
 * See the COPYING file for details. 
 */
package swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.atticlabs.zonelayout.swing.ZoneLayout;
import com.atticlabs.zonelayout.swing.ZoneLayoutFactory;

import engine.PunchEngine;

public class PunchOut extends JPanel {
	private static final long serialVersionUID = -1045321363259356924L;
	
	private final JTextArea memoField;
	private final JTextField timeElapsedField;
	private final JTextField timeOutField;
	private final ActionListener update;

	public PunchOut(final PunchEngine engine) {
		memoField = new JTextArea(4, 40);
		timeElapsedField = new JTextField(40);
		timeOutField = new JTextField(40);

		createAndAddComponents();

		update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeOutField.setText(new Date().toString());
				timeElapsedField.setText("Time worked today: " + PunchEngine.formatMinutes(engine.minutesWorkedToday()));
			}
		};
	}

	public ActionListener getUpdateListener() {
		return update;
	}

	private void createAndAddComponents() {
		ZoneLayout layout = ZoneLayoutFactory.newZoneLayout();
		layout.addRow("aa2b~b");
		layout.addRow("6.....");
		layout.addRow("cc2d~d");
		layout.addRow("6.....");
		layout.addRow("e+...e");

		setLayout(layout);

		timeOutField.setEditable(false);
		timeElapsedField.setEditable(false);

		add(new JLabel("Time Out:"), "a");
		add(timeOutField, "b");
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
