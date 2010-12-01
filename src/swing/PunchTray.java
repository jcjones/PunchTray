/* Copyright 2009 James C. Jones, All Rights Reserved */
package swing;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;

import engine.Punch;
import engine.PunchEngine;
import engine.PunchInformation;
import engine.SingleInstance;

public class PunchTray {

	public static void main(String[] args) {
		new PunchTray().create();
	}

	final PunchEngine engine;
	final MenuItem exitItem;
	final MenuItem inOutItem;
    final MenuItem analysisItem;
	final MenuItem appendItem;
	final MenuItem listItem;
	final Preferences prefs;


	final Timer swingTimer;

	public PunchTray() {
		engine = new PunchEngine();
		prefs = Preferences.userNodeForPackage(getClass());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			/*Ignore*/
		}

		exitItem = new MenuItem("Exit");
		inOutItem = new MenuItem("Punch In/Out");
		appendItem = new MenuItem("Append Details");
		listItem = new MenuItem("History");
		analysisItem = new MenuItem("Analysis");

		swingTimer = new Timer(1000, null);
		swingTimer.start();
	}

	public void create() {
		final TrayIcon trayIcon;
		
		SingleInstance si = new SingleInstance("PunchTray");

		if (SystemTray.isSupported()) {
			final PunchIn inPanel = new PunchIn(engine);
			final PunchOut outPanel = new PunchOut(engine);
			final Append appendPanel = new Append(engine);
			final PunchList listPanel = new PunchList(engine);
			final Analysis analysisPanel = new Analysis(engine);

			SystemTray tray = SystemTray.getSystemTray();
			
			Image image = null;
			URL url = getClass().getClassLoader().getResource("stopwatch_128x128.png");
			if (url != null)
			{
				image = Toolkit.getDefaultToolkit().getImage(url);
			} else {
				image = Toolkit.getDefaultToolkit().getImage("stopwatch_128x128.png");
			}
			

			PopupMenu popup = new PopupMenu();
			trayIcon = new TrayIcon(image, "Tray Demo", popup);

			ActionListener tooltipListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (engine.getLastPunch() == null) {
						trayIcon.setToolTip("You haven't punched in yet.");
					} else {
						trayIcon.setToolTip("You are " + (engine.getLastPunch().inPunch ? "on the clock" : "off the clock") + " and have worked "
								+ PunchEngine.formatMinutes(engine.minutesWorkedToday()) + " today.");
					}
				}
			};

			ActionListener helpListener = new ActionListener() {
				boolean doneOnce = false;

				public void actionPerformed(ActionEvent e) {

					if (engine.getLastPunch() != null && engine.getLastPunch().inPunch == false) {
						if (doneOnce == false) {
							doneOnce = true;
							trayIcon.displayMessage("Not punched in!", "You haven't punched in yet", TrayIcon.MessageType.WARNING);
						}
					} else {
						doneOnce = false;
					}

				}
			};

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Exiting...");
					System.exit(0);
				}
			};

			ActionListener inOutListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (engine.getLastPunch() == null || engine.getLastPunch().inPunch == false)
                        punchIn();
                    else
                        punchOut();
                    menuStateUpdate();
                }

                public void punchIn() {
                    int result = JOptionPane.showConfirmDialog(null, inPanel, "Punch In", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        engine.enterPunch(new Date(), true, inPanel.getMemo());
                    }
                }

                public void punchOut() {
                    outPanel.setMemo(engine.getLastPunch().description);

                    int result = JOptionPane.showConfirmDialog(null, outPanel, "Punch Out", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        engine.enterPunch(new Date(), false, outPanel.getMemo());
                    }
                }
            };

			ActionListener appendListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Punch punch = engine.getLastPunch();
					if (punch == null)
					{
						trayIcon.displayMessage("Never punched in", "You've haven't punched in yet.", TrayIcon.MessageType.WARNING);
						return;
					}
					appendPanel.setMemo(punch.description);

					int result = JOptionPane.showConfirmDialog(null, appendPanel, "Append More Details", JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						punch.description = appendPanel.getMemo();
						engine.updatePunch(punch);
					}
					menuStateUpdate();
				}
			};

			ActionListener listListener = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					listPanel.update();
					JOptionPane pane = new JOptionPane(listPanel, JOptionPane.PLAIN_MESSAGE);
					JDialog dialog = pane.createDialog(null, "History");
									
					int W = prefs.getInt("History_W", 500);
					int H = prefs.getInt("History_H", 400);
					
					dialog.setSize(W, H);
					dialog.setLocationByPlatform(true);
					dialog.setResizable(true);
				    dialog.setVisible(true);
				    
				    // wait on the user to click okay
				    pane.getValue();
				    
				    prefs.putInt("History_W", dialog.getSize().width);
				    prefs.putInt("History_H", dialog.getSize().height);
				    try {
				    	prefs.flush();
				    } catch (Exception e) { e.printStackTrace(); }
				}
			};
			
			ActionListener analysisListener = new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
			        analysisPanel.update();
			        JOptionPane.showMessageDialog(null, analysisPanel, "Analysis", JOptionPane.INFORMATION_MESSAGE);
			    }
			};

			/* Tool-tip and help timers */
			swingTimer.addActionListener(helpListener);
			swingTimer.addActionListener(tooltipListener);

			/* History */
			listItem.addActionListener(listListener);
			swingTimer.addActionListener(listPanel.getUpdateListener());
			popup.add(listItem);

	         /* Analysis */
            analysisItem.addActionListener(analysisListener);
//            swingTimer.addActionListener(analysisPanel.getUpdateListener());
            popup.add(analysisItem);

			popup.addSeparator();

			/* In */
			inOutItem.addActionListener(inOutListener);
			swingTimer.addActionListener(inPanel.getUpdateListener());
            swingTimer.addActionListener(outPanel.getUpdateListener());
			popup.add(inOutItem);

			popup.addSeparator();

			/* Append */
			appendItem.addActionListener(appendListener);
			swingTimer.addActionListener(appendPanel.getUpdateListener());
			popup.add(appendItem);

			popup.addSeparator();

			/* Exit */
			exitItem.addActionListener(exitListener);
			popup.add(exitItem);


			final TrayDisplay trayDisplay = new TrayDisplay();
			
			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				    trayDisplay.displayMessage(trayIcon, engine);
				}
			};

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(actionListener);
			si.addAnotherStartNotifier(actionListener);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}

			menuStateUpdate();

		} else {
			// System Tray is not supported
		    System.err.println("omgwaffles!");
		}
	}

	private void menuStateUpdate() {
		if (engine.getLastPunch() == null || engine.getLastPunch().inPunch == false) {
			inOutItem.setLabel("Punch In");
		} else {
            inOutItem.setLabel("Punch Out");
		}
	}
}
