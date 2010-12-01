/* Copyright 2009 James C. Jones, All Rights Reserved */
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

public class PunchIn extends JPanel {
	private static final long serialVersionUID = -6213407582068927481L;
	
	private final JTextField timeElapsedField;
	private final JTextField timeInField;
	private final ActionListener update;
	
	public PunchIn(final PunchEngine engine) {
		timeElapsedField = new JTextField(40);
		timeInField = new JTextField(40);

		createAndAddComponents();
		
		update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeInField.setText(new Date().toString());
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
		layout.addRow("e+...e");
		
		setLayout(layout);
		
		timeInField.setEditable(false);
		timeElapsedField.setEditable(false);
		
		add(new JLabel("Time In:"), "a");
		add(timeInField, "b");
		add(timeElapsedField, "e");
	}

	public String getMemo() {
		return "";
	}
}
