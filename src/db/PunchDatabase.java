package db;

import java.util.List;

import dbo.Punch;


public interface PunchDatabase {

	public abstract void write(Punch obj);

	@SuppressWarnings("unchecked")
	public abstract List<Punch> read();

	public abstract void update();

}