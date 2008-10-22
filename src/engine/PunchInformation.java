package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PunchInformation {
    private List<PunchDay> days;
    
    private PunchInformation() {
        days = new ArrayList<PunchDay>();
    }
    
    public static PunchInformation create(PunchEngine engine) {
        return create(engine.getAllPunches());
    }
    
    public static PunchInformation create(List<Punch> punches) {
        PunchInformation pi = new PunchInformation();
        PunchDay curDay = new PunchDay();
        Calendar c = Calendar.getInstance();
        
        Collections.reverse(punches);
        
        for (int i=0; i<punches.size(); i+=2)
        {
            boolean partialRow = punches.size() <= i+1;
            PunchPeriod period = new PunchPeriod();

            Punch start = punches.get(i);
            
            period.dateIn = start.date;
            period.description =  start.description;
            period.extra = start.extra;
            if (!partialRow) {
                Punch end = punches.get(i+1);
                period.dateOut = end.date;
                period.description =  end.description;
                period.extra = end.extra;
            }
            
            c.setTime(start.date);
            int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
            
            if (dayOfYear != curDay.getDayOfYear())
            {
                Collections.sort(curDay.periods);
                curDay = new PunchDay();
                pi.days.add(curDay);
            }
            curDay.periods.add(period);
            curDay.minutesWorked += period.getMinutesWorked(); 
        
        }
        
        Collections.sort(pi.days);
        
        return pi;
    }
    
    public long getMinutesWorked() {
        long result = 0;
        for (PunchDay d : days)
            result += d.getMinutesWorked(null);
        
        return result;
    }
    
    public List<PunchDay> getDays() {
        return days;
    }
    
    public List<PunchPeriod> getPeriods() {
        ArrayList<PunchPeriod> list = new ArrayList<PunchPeriod>();
        for (PunchDay day : days)
            list.addAll(day.periods);
        Collections.sort(list);
        return list;
    }
    
    public int getDayCount() {
        return days.size();
    }
    
    public int getPeriodCount() {
        int cnt = 0;
        for (PunchDay d : days)
            cnt+= d.periods.size();
        return cnt;
    }
    
    public static class PunchPeriod implements Comparable<PunchPeriod> {
        private Date dateIn;
        private Date dateOut;
        private String description;
        private Serializable extra;
        
        public long getMinutesWorked() {
            if (dateOut == null)
                return (System.currentTimeMillis() - dateIn.getTime()) / 1000 / 60;
            return (dateOut.getTime() - dateIn.getTime()) / 1000 / 60;
        }
        
        public Date getDateIn() {
            return dateIn;
        }
        
        public Date getDateOut() {
            return dateOut;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Serializable getExtra() {
            return extra;
        }

        @Override
        public int compareTo(PunchPeriod o) {
            return dateIn.compareTo(o.dateIn);
        }
        
        
    }
    
    public static class PunchDay implements Comparable<PunchDay> {
        private List<PunchPeriod> periods;
        private long minutesWorked;
        public PunchDay() {
            periods = new ArrayList<PunchPeriod>();
        }
        private int getDayOfYear() {
            if (periods.isEmpty()) return -1;
            Calendar c = Calendar.getInstance();
            c.setTime(periods.get(0).dateIn);
            return c.get(Calendar.DAY_OF_YEAR);
        }
        @Override
        public int compareTo(PunchDay o) {
            if (getDayOfYear() < o.getDayOfYear())
                return -1;
            if (getDayOfYear() > o.getDayOfYear())
                return 1;
            return 0;
        }
        
        public List<PunchPeriod> getPeriods() {
            return periods;
        }

        public long getMinutesWorked(List<PunchPeriod> selectedPeriods) {
            long min = 0;
            for (PunchPeriod p : periods)
            {
                if (selectedPeriods == null || selectedPeriods.contains(p))
                    min += p.getMinutesWorked();
            }
            return min;
        }
        
        public float getHoursWorked(List<PunchPeriod> selectedPeriods) {
            return PunchEngine.minutesToDecimalHours(getMinutesWorked(selectedPeriods));
        }
    }
}
