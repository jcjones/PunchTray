package db;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import dbo.Punch;


public class SqliteDatabase implements PunchDatabase {
	private static final String TABLE_PUNCHES = "Punches";
	private static final String TABLE_PROJECTS = "Projects";
	private static final String TABLE_TASKS = "Tasks";
	
	private static final String COLUMN_PUNCHES_IDNUM = "Id";
	private static final String COLUMN_PUNCHES_PUNCH_IN = "PunchIn";
	private static final String COLUMN_PUNCHES_PUNCH_OUT = "PunchOut";
	private static final String COLUMN_PUNCHES_PUNCH_DESCRIPTION = "Description";
	private static final String COLUMN_PUNCHES_PUNCH_EXTRA = "Extra";
	private static final String COLUMN_PUNCHES_PUNCH_PROJECT = "ProjectId";
	private static final String COLUMN_PUNCHES_PUNCH_TASK = "TaskId";
	
	private static final String[] PUNCHES_COLUMNS = {
		COLUMN_PUNCHES_IDNUM, COLUMN_PUNCHES_PUNCH_IN, COLUMN_PUNCHES_PUNCH_OUT,
		COLUMN_PUNCHES_PUNCH_DESCRIPTION, COLUMN_PUNCHES_PUNCH_EXTRA,
		COLUMN_PUNCHES_PUNCH_PROJECT, COLUMN_PUNCHES_PUNCH_TASK
	};
	
	private static final String COLUMN_PROJECTS_IDNUM = "Id";
	private static final String COLUMN_PROJECTS_DESCRIPTION = "Description";
	private static final String COLUMN_PROJECTS_URI = "Uri";
	
	private static final String[] PROJECTS_COLUMNS = {
		COLUMN_PROJECTS_IDNUM, COLUMN_PROJECTS_DESCRIPTION, COLUMN_PROJECTS_URI
	};
	
	private static final String COLUMN_TASKS_IDNUM = "Id";
	private static final String COLUMN_TASKS_DESCRIPTION = "Description";
	private static final String COLUMN_TASKS_URI = "Uri";

	private static final String[] TASKS_COLUMNS = {
		COLUMN_TASKS_IDNUM, COLUMN_TASKS_DESCRIPTION, COLUMN_TASKS_URI
	};
	
	private Connection conn;
	
	public SqliteDatabase(File db) {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+db.getAbsolutePath());
	
			initialize();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}
	
	private Serializable decodeObject(InputStream binaryStream) {
		try {
			ObjectInputStream ois = new ObjectInputStream(binaryStream);
			return (Serializable) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private InputStream encodeObject(Serializable extra) {
		try {
			PipedInputStream in = new PipedInputStream();
			PipedOutputStream out = new PipedOutputStream(in);

			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(extra);
			return in;
		} catch (Exception e) {
			return null;
		}
	}

	public void write(Punch punch) {
		PreparedStatement prep = null;

		StringBuilder statement = new StringBuilder("insert into "+TABLE_PUNCHES+" (");
		for (String colName : PUNCHES_COLUMNS) {
			statement.append(colName);
			statement.append(",");
		}
		statement.setLength(statement.length()-1);
		statement.append(") values (");
		for (String colName : PUNCHES_COLUMNS) {
			statement.append("?,");
		}
		statement.setLength(statement.length()-1);
		statement.append(");");
		
		try {
			prep = conn.prepareStatement(statement.toString());
			prep.setInt(1, punch.id);
			prep.setDate(2, punch.dateIn);
			prep.setDate(3, punch.dateOut);
			prep.setString(4, punch.description);
			prep.setBinaryStream(5, encodeObject(punch.extra));
			prep.setInt(6, punch.projectId);
			prep.setInt(7, punch.taskId);
			prep.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (prep != null) {
				try {
					prep.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<Punch> read() {
		Vector<Punch> punches = new Vector<Punch>();
		PreparedStatement prep = null;
		try {
			prep = conn.prepareStatement("select * from " + TABLE_PUNCHES + ";");
			
			ResultSet results = prep.executeQuery();

			while (results.next()) {
				Punch template = new Punch();
				punches.add(template);
				
				template.dateIn = results.getDate(COLUMN_PUNCHES_PUNCH_IN);
				template.dateOut = results.getDate(COLUMN_PUNCHES_PUNCH_OUT);
				template.description = results.getString(COLUMN_PUNCHES_PUNCH_DESCRIPTION);
				template.id = results.getInt(COLUMN_PUNCHES_IDNUM);
				template.extra = decodeObject(results.getBinaryStream(COLUMN_PUNCHES_PUNCH_EXTRA));
				template.projectId = results.getInt(COLUMN_PUNCHES_PUNCH_PROJECT);
				template.taskId = results.getInt(COLUMN_PUNCHES_PUNCH_TASK);
			}
			results.close();

		} catch (SQLException s) {
			s.printStackTrace();
		} finally {
			if (prep != null) {
				try {
					prep.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return punches;
	}

	public void update() {
		// TODO Auto-generated method stub

	}
	
	public void initialize() throws SQLException {
		Statement stat = conn.createStatement();
		
		stat.executeUpdate("create table if not exists "+TABLE_PUNCHES+" (" + COLUMN_PUNCHES_IDNUM
				+ " INTEGER PRIMARY KEY ASC, " + COLUMN_PUNCHES_PUNCH_IN + " DATE, " + COLUMN_PUNCHES_PUNCH_OUT + " DATE, "
				+ COLUMN_PUNCHES_PUNCH_DESCRIPTION + ", " + COLUMN_PUNCHES_PUNCH_EXTRA + " BINARY, " + COLUMN_PUNCHES_PUNCH_PROJECT + " INTEGER, "
				+ COLUMN_PUNCHES_PUNCH_TASK + " INTEGER" + ");");

		stat.close();
	}

}
