/* 
 * Copyright 2009 James C. Jones, licensed under the terms of the GNU GPL v2 
 * See the COPYING file for details. 
 */
/**
 * 
 */
package dbo;

import java.io.Serializable;
import java.sql.Date;

public class Punch implements Comparable<Punch> {
	public Date dateIn;
	public Date dateOut;
	public String description;
	public Serializable extra;
	public int id;
	public int taskId;
	public int projectId;
	
	public Punch() {
	}

	@Override
	public String toString() {
		return "Punch in at " + dateIn + ", out at " + dateOut + " desc " + description + " task " + taskId + " project " + projectId;
	}

	@Override
	public int compareTo(Punch target) {
		return target.dateIn.compareTo(dateIn);
	}
}