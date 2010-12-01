/* Copyright 2009 James C. Jones, All Rights Reserved */
package swing;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import swing.components.TextAreaRenderer;

import com.atticlabs.zonelayout.swing.ZoneLayout;
import com.atticlabs.zonelayout.swing.ZoneLayoutFactory;

import engine.PunchEngine;
import engine.PunchInformation;
import engine.PunchInformation.PunchDay;
import engine.PunchInformation.PunchPeriod;

public class PunchList extends JPanel {
	public class DurationRenderer implements TableCellRenderer {
		TableCellRenderer renderer;

		public DurationRenderer(TableCellRenderer defaultRenderer) {
			renderer = defaultRenderer;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			String s = PunchEngine.formatMinutes((Long) value);
			Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (c instanceof JLabel)
				((JLabel) c).setText(s);

			return c;
		}
	}

	public class PunchListModel extends AbstractTableModel {
		private static final long serialVersionUID = 7838020429600396254L;
		private PunchEngine engine;
		private TimeUnit timeUnit;
		private PunchInformation currentInformation;

		public PunchListModel(PunchEngine engine, TimeUnit unit) {
			this.engine = engine;
			this.timeUnit = unit;
			this.currentInformation = null;
			update();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "In";
			case 1:
				return "Out";
			case 2:
				return "Duration";
			case 3:
				return "Memo";
			default:
				return "??";
			}
		}

		@Override
		public int getRowCount() {
		    return currentInformation.getPeriodCount();
		}

		public String getStatusText(int rows[]) {
			long minutesShown = 0;
			float hoursShown = 0f;
			long minutesSelected = 0;
            float hoursSelected = 0f;
            
            List<PunchPeriod> periods = currentInformation.getPeriods();
            List<PunchPeriod> selectedPeriods = new ArrayList<PunchPeriod>();
            
            if (rows != null)
                for (int row : rows)
                    selectedPeriods.add(periods.get(row));
            
            List<PunchDay> days = currentInformation.getDays();
            for (PunchDay day : days)
            {
                minutesSelected += day.getMinutesWorked(selectedPeriods);
                hoursSelected += day.getHoursWorked(selectedPeriods);
                minutesShown += day.getMinutesWorked(null);
                hoursShown += day.getHoursWorked(null);
            }
            
            String shown = PunchEngine.formatMinutes(minutesShown, Math.max(PunchEngine.minutesToDecimalHours(minutesShown), hoursShown));
            String selected = PunchEngine.formatMinutes(minutesSelected, Math.max(PunchEngine.minutesToDecimalHours(minutesSelected), hoursSelected));
			
			return "Hours shown: " + shown + ", Hours selected: " + selected;
		}
		
        @Override
		public Object getValueAt(int row, int col) {
            PunchPeriod p = currentInformation.getPeriods().get(row);
            
			switch (col) {
			case 0:
			    return p.getDateIn();
			case 1:
				if (p.getDateOut() == null)
					return "(ongoing)";
				return p.getDateOut();
			case 2:
			    return p.getMinutesWorked();
			case 3:
			    return p.getDescription();
			default:
				return "???";
			}
		}

		public void setTimeUnit(TimeUnit unit) {
			this.timeUnit = unit;
		}

		public void update() {
            currentInformation = PunchInformation.create(engine.getPunchesBetween(timeUnit.getStartDate(), timeUnit.getEndDate()));
			fireTableDataChanged();
		}
	}

	private enum TimeUnit {
		All("All"), Last_Month("Last Month"), This_Month("This Month"), Last_Week("Last Week"), This_Week("This Week"), Today("Today"), ;
		private final String name;

		private TimeUnit(String name) {
			this.name = name;
		}

		public Date getStartDate() {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			switch (this) {
			case All:
				return new Date(0);
			case This_Week:
				cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
				return cal.getTime();
			case Last_Week:
			    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
			    cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR)-1);
                return cal.getTime();
			case Last_Month:
			    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)-1);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                return cal.getTime();
			case This_Month:
				cal.set(Calendar.DAY_OF_MONTH, 0);
				return cal.getTime();
			case Today:
				return cal.getTime();
			default:
				return cal.getTime();
			}
		}
		
		public Date getEndDate() {
		    Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            
		    switch (this) {
            case Last_Month:
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                return cal.getTime();
            case Last_Week:
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                return cal.getTime();
            default:
                return cal.getTime();
            }
        }

		public String toString() {
			return name;
		}
	}

	private static final long serialVersionUID = 6087332088256016891L;
	private final JTable listTable;
	private final JComboBox timeUnitsComboBox;
	private final PunchListModel listModel;

	private final ActionListener update;

	private final JLabel totalsLabel;

	public PunchList(final PunchEngine engine) {
		TimeUnit defaultTimeUnit = TimeUnit.This_Week;

		listModel = new PunchListModel(engine, defaultTimeUnit);
		listTable = new JTable(listModel);

		timeUnitsComboBox = new JComboBox(TimeUnit.values());
		timeUnitsComboBox.setSelectedItem(defaultTimeUnit);

		totalsLabel = new JLabel(listModel.getStatusText(null));

		update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};

		createAndAddComponents();
	}

	private void createAndAddComponents() {
		ZoneLayout layout = ZoneLayoutFactory.newZoneLayout();

		layout.addRow("a2b..");
		layout.addRow("6....");
		layout.addRow("c+*.c");
		layout.addRow("d-~.d");

		setLayout(layout);

		ActionListener comboBoxListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				TimeUnit period = (TimeUnit) timeUnitsComboBox.getSelectedItem();
				listModel.setTimeUnit(period);
				update();
				totalsLabel.setText(listModel.getStatusText(null));
			}
		};

		ActionListener copyToClipboard = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuffer data = new StringBuffer();
				int[] rows = listTable.getSelectedRows();
				for (int row : rows) {
					data.append(listModel.getValueAt(row, 3) + "\n");
				}

				Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable transferableText = new StringSelection(data.toString());
				systemClipboard.setContents(transferableText, null);
			}
		};

		ListSelectionListener selectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				totalsLabel.setText(listModel.getStatusText(listTable.getSelectedRows()));
			}
		};

		timeUnitsComboBox.setEditable(false);
		timeUnitsComboBox.addActionListener(comboBoxListener);

		listTable.registerKeyboardAction(copyToClipboard, "Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
		listTable.getColumnModel().getColumn(3).setCellRenderer(new TextAreaRenderer());
		listTable.getColumnModel().getColumn(2).setCellRenderer(new DurationRenderer(listTable.getDefaultRenderer(String.class)));

		listTable.getSelectionModel().addListSelectionListener(selectionListener);

		add(new JLabel("Time period to show:"), "a");
		add(timeUnitsComboBox, "b");
		add(new JScrollPane(listTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), "c");
		add(totalsLabel, "d");
	}

	public ActionListener getUpdateListener() {
		return update;
	}

	public void update() {
		listModel.update();
		int colWidth = listTable.getColumnModel().getColumn(3).getWidth();
		JTextArea jText = new JTextArea();
		jText.setSize(colWidth, 9999);
		int defHeight = listTable.getRowHeight();

		for (int i = 0; i < listTable.getRowCount(); i++) {
			Object data = listTable.getModel().getValueAt(i, 3);
			if (data != null) {
				jText.setText(data.toString());
				int lines = jText.getLineCount();
				listTable.setRowHeight(i, lines * defHeight);
			}
		}
	}
}
