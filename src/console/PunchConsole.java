/* Copyright 2009 James C. Jones, All Rights Reserved */
/* Copyright 2009 James C. Jones, All Rights Reserved */
package console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import engine.Punch;
import engine.PunchEngine;

public class PunchConsole {
	public static void main(String[] args) {
		new PunchConsole().go();
	}

	PunchEngine engine;
	
	public PunchConsole() {
		engine = new PunchEngine();		
	}
	
	private void go() {
		PrintStream out = System.out;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		out.println("PunchEngine");
		
		while (true)
		{
			try {
				out.print(getPrompt() + "> ");
				String cmd = in.readLine();
				parse(cmd, in, out);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getPrompt() {
		return PunchEngine.formatMinutes(engine.minutesWorkedToday());
	}

	private void parse(String cmd, BufferedReader in, PrintStream out) throws Exception {
		if (cmd.startsWith("p"))
		{
			String[] parts = cmd.split(" ", 3);
			if (parts.length < 2)
			{
				out.println("Must say in/out");
				return;
			}
			
			Date date;
			if (parts.length == 3)
			{
				date = new SimpleDateFormat("yyyyMMdd h:mm a").parse(parts[2]);
			} else {
				date = new Date();
			}
			
			if (parts[1].equalsIgnoreCase("in"))
			{
				engine.enterPunch(date, true);
			} else if (parts[1].equalsIgnoreCase("out"))
			{
				engine.enterPunch(date, false);
			} else {
				out.println("Not understood: " + parts[1]);
			}
		}
		
		if (cmd.startsWith("l"))
		{
			Iterator<Punch> it = engine.getAllPunches().iterator();
			for (int i=0; it.hasNext(); i++)
			{
				out.println(i+":\t"+it.next());
				
				if (i%10 == 9)
				{
					out.print("More? [Y/n]: ");
					String line = in.readLine();
					if (line.equals("n"))
						break;
				}
			}
		}
		
		if (cmd.startsWith("e"))
		{
			String[] parts = cmd.split(" ");
			try {
				int idx = Integer.parseInt(parts[1]);
				Punch p = engine.getAllPunches().get(idx);
				
				edit(p, in, out);
				
				engine.updatePunch(p);				
			} catch (Exception e) {
				out.println("Must provide a punch ID to edit");
			}
		}
		
	      if (cmd.startsWith("r"))
	        {
	            String[] parts = cmd.split(" ");
	            try {
	                int idx = Integer.parseInt(parts[1]);
	                Punch p = engine.getAllPunches().get(idx);
	                
	                out.print("Are you sure you want to delete " + p + " [y/N]:");
	                String line = in.readLine();
	                if (line.toLowerCase().startsWith("y")) 
                    {
	                    engine.removePunch(p);              
	                    out.println("Removed " + p);
                    }
	            } catch (Exception e) {
	                out.println("Must provide a punch ID to remove");
	            }
	        }

		
		if (cmd.startsWith("q"))
		{
			System.exit(0);
		}
		
		if (cmd.startsWith("d"))
		{
			String[] parts = cmd.split(" ");
			try {
				Calendar start = Calendar.getInstance();
				
				start.set(Calendar.MONTH, Integer.parseInt(parts[1])-1);
				start.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
				start.set(Calendar.HOUR_OF_DAY, 8);
				start.set(Calendar.MINUTE, 0);
				
				Calendar end = (Calendar)start.clone();
				double hours = Double.parseDouble(parts[3]);
				end.add(Calendar.HOUR_OF_DAY, (int)hours);
				int remainder = (int)(4 * (double)(hours - (int)hours));
								
				end.add(Calendar.MINUTE, remainder*15);
				
				out.println("In: " + new SimpleDateFormat("yyyyMMdd h:mm a").format(start.getTime()));
				out.println("Out: " + new SimpleDateFormat("yyyyMMdd h:mm a").format(end.getTime()));
				
				out.print("Okay? [Y/n]:");
				String line = in.readLine();
				if (line.toLowerCase().startsWith("n")) 
					return;
				
				engine.enterPunch(start.getTime(), true);
				engine.enterPunch(end.getTime(), false);
			} catch (Exception e) {
				e.printStackTrace(out);
			}
		}
	}

	private void edit(Punch p, BufferedReader in, PrintStream out) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd h:mm:ss a");
		
		out.println("Existing timestamp: " + sdf.format(p.date));
		out.print("New timestamp: ");
		try {
			String dateString = in.readLine();
			p.date = sdf.parse(dateString);
		} catch (Exception e) {
			out.println("Aborted.");
		}
	}
}
