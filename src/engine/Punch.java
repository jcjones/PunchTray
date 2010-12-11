/* 
 * Copyright 2009 James C. Jones, licensed under the terms of the GNU GPL v2 
 * See the COPYING file for details. 
 */
/**
 * 
 */
package engine;

import java.io.Serializable;
import java.util.Date;

public class Punch implements Serializable, Comparable<Punch> {
	private static final long serialVersionUID = 837373729219302L;
	public Date date;
	public boolean inPunch;
	public String description;
	public Serializable extra;

	public Punch(Date date, boolean inPunch) {
		this.date = date;
		this.inPunch = inPunch;
	}

	public Punch(Date date, boolean inPunch, String description) {
		this.date = date;
		this.inPunch = inPunch;
		this.description = description;
	}

	public Punch(Date date, boolean inPunch, String description,
			Serializable extra) {
		this.date = date;
		this.inPunch = inPunch;
		this.description = description;
		this.extra = extra;
	}

	@Override
	public String toString() {
		return "Punch " + (inPunch ? "IN" : "out") + " at " + date;
	}

	@Override
	public int compareTo(Punch target) {
		return target.date.compareTo(date);
	}
}