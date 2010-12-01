/* Copyright 2009 James C. Jones, All Rights Reserved */
package swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.atticlabs.zonelayout.swing.ZoneLayout;
import com.atticlabs.zonelayout.swing.ZoneLayoutFactory;

import engine.PunchEngine;
import engine.PunchInformation;
import engine.PunchInformation.PunchDay;

public class Analysis extends JPanel {
    private final JLabel details;
    private final ActionListener update;
    private static float hoursPerDay = 8.00f;
    private static float hoursPerWeek = hoursPerDay * 5;

    public Analysis(final PunchEngine engine) {
        details = new JLabel();

        createAndAddComponents();

        update = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                details.setText(getDetails(engine));
            }
        };
    }
    
    public void update() {
        update.actionPerformed(null);
    }

    protected String getDetails(PunchEngine engine) {
        Calendar cal = Calendar.getInstance();
        StringBuffer buf = new StringBuffer("<html>");
        DateFormat timeFmt = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        NumberFormat numFmt = NumberFormat.getNumberInstance();
        
        Date weekStart = PunchEngine.getStartOfWeek();
        
        PunchInformation pi = PunchInformation.create(engine.getPunchesSince(weekStart));
        
        float totalHours = 0;
        long totalMinOfDayStart = 0;
        int todaysDayStart = 0;
        
        float fractionOfToday = engine.minutesWorkedToday()/(hoursPerDay*60);
        
        float totalDays = (pi.getDays().size()-1)+fractionOfToday;
        
        for (PunchDay day : pi.getDays())
        {
            cal.setTime(day.getPeriods().get(0).getDateIn());
//            if (isToday(cal)) continue;

            totalHours += day.getHoursWorked(null);
            
            todaysDayStart = cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR_OF_DAY)*60);
            totalMinOfDayStart += cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR_OF_DAY)*60); 
        }
        float avgHours = totalHours / totalDays;
        int avgMinOfDayStart = (int)(totalMinOfDayStart / Math.ceil(totalDays));
        
        cal = PunchEngine.getZeroedCalendar(new Date());
        float daysLeft = Calendar.FRIDAY - cal.get(Calendar.DAY_OF_WEEK) + (1-fractionOfToday);

        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        cal.set(Calendar.MINUTE, avgMinOfDayStart);
        Date expectedStartTime = cal.getTime();
        
        float normalHoursExpected = totalHours + (hoursPerDay*daysLeft);
        float normalHoursLeftFriday = hoursPerWeek - normalHoursExpected;
        
        cal = PunchEngine.getZeroedCalendar(new Date());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        cal.set(Calendar.MINUTE, avgMinOfDayStart);
        cal.add(Calendar.MINUTE, (int)(normalHoursLeftFriday*60));
        Date curQuitTime = cal.getTime();
        
        float predictedHoursExpected = totalHours + (avgHours*daysLeft);
        float predictedHoursLeftFriday = hoursPerWeek - predictedHoursExpected;
        
        if (daysLeft >= 1.0f)
        {
            cal = PunchEngine.getZeroedCalendar(new Date());
            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            cal.set(Calendar.MINUTE, avgMinOfDayStart);
            cal.add(Calendar.MINUTE, (int)(predictedHoursLeftFriday*60));
            Date predQuitTime = cal.getTime();

            printItem(buf, "Expected Friday Start-Time", timeFmt.format(expectedStartTime));
            printItem(buf, "Current (8 hr/day) Friday Quit-Time", timeFmt.format(curQuitTime));
            if (avgHours > 1)
                printItem(buf, "Predicted ("+numFmt.format(avgHours)+" hr/day) Friday Quit-Time", timeFmt.format(predQuitTime));
            
        } else {
            /* Today's Friday */
            
            /* Get today's start time */
            PunchDay day = pi.getDays().get(pi.getDayCount()-1);
            cal.setTime(day.getPeriods().get(0).getDateIn());
            todaysDayStart = cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR_OF_DAY)*60);
            Date startTime = cal.getTime();
            
            /* Determine the target hour mark time */
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, (int)((hoursPerWeek - totalHours)*60));
            Date endTime = cal.getTime();
            
            printItem(buf, "Start-Time", timeFmt.format(startTime));
            printItem(buf, "Weekend Begins", timeFmt.format(endTime));
        }
        
        
        return buf.append("</html>").toString();
    }
    
    private boolean isToday(Calendar sub) {
        Calendar today = Calendar.getInstance();
        
        return (today.get(Calendar.DAY_OF_YEAR) == sub.get(Calendar.DAY_OF_YEAR)) && 
               (today.get(Calendar.YEAR) == sub.get(Calendar.YEAR));
    }

    private void printItem(StringBuffer buf, String label, String text)
    {
        buf.append("<p><b>");
        buf.append(label);
        buf.append(":</b>&nbsp;&nbsp;");
        buf.append(text);
        buf.append("</p>");
    }

    private void createAndAddComponents() {
        ZoneLayout layout = ZoneLayoutFactory.newZoneLayout();
        layout.addRow("a*+a");
        setLayout(layout);
        
        add(new JScrollPane(details, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "a");
    }

    public ActionListener getUpdateListener() {
        return update;
    }
    
    public static void main(String[] args) {
        PunchEngine engine = new PunchEngine();
        Analysis analysis = new Analysis(engine);
        System.out.println(analysis.getDetails(engine));
    }
}

