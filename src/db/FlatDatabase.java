/* 
 * Copyright 2009 James C. Jones, licensed under the terms of the GNU GPL v2 
 * See the COPYING file for details. 
 */
package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.Punch;

public class FlatDatabase {
	File databaseFile;
	List<Punch> data;

	public FlatDatabase(File db) {
		databaseFile = db;
	}

	public void write(Punch obj) {
		try {
			if (data == null) {
				try {
					read();
				} catch (Throwable t) {
				}
				if (data == null) {
					data = new ArrayList<Punch>();
				}
			}
			
			data.add(obj);
			update();
			
		} catch (Exception e) {
			throw new RuntimeException("Could not write", e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Punch> read() {
		try {
			if (data == null) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(databaseFile));
				try {
					Object obj = ois.readObject();
					if (obj instanceof List) {
						data = (List<Punch>) obj;
					}
				} finally {
					ois.close();
				}
			}
		} catch (FileNotFoundException e) {
			data = new ArrayList<Punch>();
		} catch (Exception e) {
			throw new RuntimeException("Could not read", e);
		}
		return data;
	}

	public void update() {
		try {
			Collections.sort(data);

			if (databaseFile.exists()) {
				File bakFile = new File(databaseFile.getAbsolutePath() + ".bak");
				
				if (bakFile.exists()) 
					bakFile.delete();
				
				databaseFile.renameTo(bakFile);
			}

			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(databaseFile));

			try {
				oos.writeObject(data);
			} finally {
				oos.flush();
				oos.close();
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Could not write to disk", e);
		}
	}
}
