package swing;

import java.awt.TrayIcon;

import engine.PunchEngine;
import engine.PunchInformation;

public class TrayDisplay {

    public void displayMessage(TrayIcon trayIcon, PunchEngine engine) {
        PunchInformation pi = PunchInformation.create(engine.getPunchesSince(PunchEngine.getStartOfWeek()));
        
        if (engine.getLastPunch() != null) {
            StringBuffer buf = new StringBuffer(trayIcon.getToolTip() + "\n");
            
            buf.append("At this rate you'll have 8 hours at ");
            buf.append(PunchEngine.timeWillReach(8 * 60, engine.minutesWorkedToday()));

            if (pi.getDays().size() > 1)
                buf.append("\nHours worked this week: " + engine.formatMinutes(pi.getMinutesWorked()));
            
            trayIcon.displayMessage("Time Elapsed", buf.toString(), TrayIcon.MessageType.INFO);
        } else {
            trayIcon.displayMessage("No Punches", "You haven't punched in yet", TrayIcon.MessageType.INFO);
        }
    }
    
}
