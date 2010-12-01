/* Copyright 2009 James C. Jones, All Rights Reserved */
package engine;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import db.FlatDatabase;

public class PunchEngine {
	FlatDatabase db;
	
	public PunchEngine() {
		db = new FlatDatabase(new File(System.getenv("APPDATA") + System.getProperty("file.separator") + "punchData.dat"));
	}
	
	public void enterPunch(Date date, boolean inPunch)
	{
		enterPunch(date, inPunch, null);
	}
	
	public void enterPunch(Date date, boolean inPunch, String description)
	{
		try {
			if (inPunch == getLastPunch().inPunch)
			{
				throw new RuntimeException("Last punch was of the same type!");
			}
		} catch (RuntimeException e) {}
		
		db.write(new Punch(date, inPunch, description));
	}
	
	public void updatePunch(Punch punch)
	{
		db.update();
	}
	
	public Punch getLastPunch() {
		Punch punch = null;
		
		try {
			List<Punch> punches = getAllPunches();
			punch = punches.get(0);
		} catch (Throwable t) {}
		
		return punch;
	}

	public List<Punch> getPunchesSince(Date date)
	{
		return getPunchesBetween(date, new Date());
	}
	
	public List<Punch> getPunchesBetween(Date start, Date end)
	{
	    ArrayList<Punch> nList = new ArrayList<Punch>();
        for (Punch p : getAllPunches())
        {
            if (p.date.before(start))
                break;
            
            if (p.date.after(end) == false)
                nList.add(p);
        }
        return nList;
	}
	
	public List<Punch> getAllPunches()
	{
		return new ArrayList<Punch>(db.read());
	}
	
	public void removePunch(Punch p) {
	    db.read().remove(p);
	    db.update();
	}
	
	public long minutesWorkedToday() {
		long minutesWorked = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startOfDay = cal.getTime();
		
		Date outTime = null;
		
		for (Punch p : getPunchesSince(startOfDay))
		{
			assert !p.date.before(startOfDay);				
			
			if (p.inPunch && outTime == null)
			{
				outTime = new Date();
			}
			
			if (p.inPunch) {
				minutesWorked += (outTime.getTime() - p.date.getTime());
			} else {
				outTime = p.date;				
			}
		}
		return minutesWorked / 1000 / 60;
	}
	
	public static float minutesToDecimalHours(long rawMinutes) {
        long hours = rawMinutes / 60;
        long min = rawMinutes - (hours * 60);
        return hours + Math.round((min / 60f) * 4f) / 4f;
    }

	public static String formatMinutes(long minutes)
	{
	    return formatMinutes(minutes, minutesToDecimalHours(minutes));
	}
	
    public static String formatMinutes(long minutes, float decimalHours) 
	{
		long hours = minutes / 60;
		long min = minutes - (hours*60);
		DecimalFormat f = new DecimalFormat("0.00");
		
		return hours + "hr " + min + "min ("+f.format(decimalHours)+")";
	}

	public static String timeWillReach(long minutesTarget, long minutesToday) {
		long delta = minutesTarget - minutesToday;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, (int)delta);
		
		return new SimpleDateFormat("h:mm a").format(cal.getTime());
	}

    public static Date getStartOfWeek() {
        Calendar cal = getZeroedCalendar(new Date());
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return cal.getTime();
    }
    
    public static Calendar getZeroedCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
    
}
