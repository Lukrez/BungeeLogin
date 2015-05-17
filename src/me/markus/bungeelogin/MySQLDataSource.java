package me.markus.bungeelogin;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;

import me.markus.bungeelogin.MiniConnectionPoolManager.TimeoutException;

public class MySQLDataSource {

	private String host;
	private String port;
	private String username;
	private String password;
	private String database;
	private String tableName;
	private String columnName;
	private String columnLoginstatus;
	private String columnPlaytime;
	
	private MiniConnectionPoolManager conPool;

	public MySQLDataSource() throws ClassNotFoundException, SQLException {
		this.host = Settings.getMySQLHost;
		this.port = Settings.getMySQLPort;
		this.username = Settings.getMySQLUsername;
		this.password = Settings.getMySQLPassword;
		this.database = Settings.getMySQLDatabase;
		this.tableName = Settings.getMySQLTablename;
		this.columnName = Settings.getMySQLColumnPlayerName;
		this.columnPlaytime = Settings.getMySQLColumnPlaytime;
		this.columnLoginstatus = Settings.getMySQLColumnLoginStatus;


		connect();
	}

	private synchronized void connect() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		BungeeLogin.instance.getLogger().info("MySQL driver loaded");
		MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
		dataSource.setDatabaseName(database);
		dataSource.setServerName(host);
		dataSource.setPort(Integer.parseInt(port));
		dataSource.setUser(username);
		dataSource.setPassword(password);
		conPool = new MiniConnectionPoolManager(dataSource, 20);
		BungeeLogin.instance.getLogger().info("Connection pool ready");
		// check if database exists
		//this.setup();
	}
	
	
	public synchronized PlayerInfo getPlayerInfo(String user) {
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			con = makeSureConnectionIsReady();
			pst = con.prepareStatement("SELECT * FROM " + tableName + " WHERE lower(" + columnName + ")=?;");
			pst.setString(1, user);
			rs = pst.executeQuery();
			if (rs.next()) {
				String playername = rs.getString(columnName);
				int playtime = rs.getInt(columnPlaytime);
				Playerstatus status = Playerstatus.valueOf(rs.getString(columnLoginstatus));
				return new PlayerInfo(playername,playtime,status);
			} else {
				return null;
			}
		} catch (SQLException ex) {
			BungeeLogin.instance.getLogger().severe(ex.getMessage());
			return null;
		} catch (TimeoutException ex) {
			BungeeLogin.instance.getLogger().severe(ex.getMessage());
			return null;
		} finally {
			close(rs);
			close(pst);
			close(con);
		}
	}
	
	public synchronized void updatePlayerData(PlayerInfo playerinfo) {
		// calculate new playtime
    	GregorianCalendar now = new GregorianCalendar();
    	int minutes = (int)((now.getTimeInMillis() - playerinfo.joinedAt.getTimeInMillis())/1000/60);
    	playerinfo.playtime += minutes;
    	
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = makeSureConnectionIsReady();

			String statement = String.format("INSERT INTO %s (%s,%s,%s) VALUES(?,?,?) ON DUPLICATE KEY UPDATE %s=VALUES(%s), %s=VALUES(%s);",
								this.tableName,
								this.columnName,this.columnLoginstatus,this.columnPlaytime,
								this.columnLoginstatus,this.columnLoginstatus,
								this.columnPlaytime,this.columnPlaytime);
			
			
			pst = con.prepareStatement(statement);
			pst.setString(1, playerinfo.playername);
			pst.setString(2, playerinfo.status.toString());
			pst.setInt(3, playerinfo.playtime);
			boolean result = pst.execute();
			BungeeLogin.instance.getLogger().info("Update sucessful? "+result);
		} catch (SQLException ex) {
			BungeeLogin.instance.getLogger().severe(ex.getMessage());
			return;
		} catch (TimeoutException ex) {
			BungeeLogin.instance.getLogger().severe(ex.getMessage());
			return;
		} finally {
			close(pst);
			close(con);
		}
	}


	public synchronized void close() {
		try {
			conPool.dispose();
		} catch (SQLException ex) {
			BungeeLogin.instance.getLogger().severe(ex.getMessage());
		}
	}

	private void close(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException ex) {
				BungeeLogin.instance.getLogger().severe(ex.getMessage());
			}
		}
	}

	private void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				BungeeLogin.instance.getLogger().severe(ex.getMessage());
			}
		}
	}

	private void close(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException ex) {
				BungeeLogin.instance.getLogger().severe(ex.getMessage());
			}
		}
	}

	private synchronized Connection makeSureConnectionIsReady() {
		Connection con = null;
		try {
			con = conPool.getValidConnection();
		} catch (Exception te) {
			try {
				con = null;
				reconnect();
			} catch (Exception e) {
				BungeeLogin.instance.getLogger().severe(e.getMessage());
				BungeeLogin.instance.getLogger().severe("Can't reconnect to MySQL database... Please check your MySQL informations ! SHUTDOWN...");
				BungeeLogin.instance.shutdown();
			}
		} catch (AssertionError ae) {
			// Make sure assertionerror is caused by the connectionpoolmanager, else re-throw it
			if (!ae.getMessage().equalsIgnoreCase("BungeeDatabaseError"))
				throw new AssertionError(ae.getMessage());
			try {
				con = null;
				reconnect();
			} catch (Exception e) {
				BungeeLogin.instance.getLogger().severe(e.getMessage());
				BungeeLogin.instance.getLogger().severe("Can't reconnect to MySQL database... Please check your MySQL informations ! SHUTDOWN...");
				BungeeLogin.instance.shutdown();
			}
		}
		if (con == null)
			con = conPool.getValidConnection();
		return con;
	}

	private synchronized void reconnect() throws ClassNotFoundException, SQLException, TimeoutException {
		conPool.dispose();
		Class.forName("com.mysql.jdbc.Driver");
		MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
		dataSource.setDatabaseName(database);
		dataSource.setServerName(host);
		dataSource.setPort(Integer.parseInt(port));
		dataSource.setUser(username);
		dataSource.setPassword(password);
		conPool = new MiniConnectionPoolManager(dataSource, 10);
		BungeeLogin.instance.getLogger().info("ConnectionPool was unavailable... Reconnected!");
	}
	
	private synchronized void setup() throws SQLException {
	    Connection con = null;
	    Statement st = null;
	    try {
	        con = makeSureConnectionIsReady();
	        st = con.createStatement();
	        st.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " ("
	                + "id" + " INTEGER AUTO_INCREMENT,"
	                + columnName + " VARCHAR(255) NOT NULL UNIQUE,"
	                + columnLoginstatus + " VARCHAR(255) NOT NULL,"
	                + columnPlaytime + " INTEGER NOT NULL,"
	                + "CONSTRAINT table_const_prim PRIMARY KEY (id));");
	    } finally {
	        close(st);
	        close(con);
	    }
	}

	/*public synchronized void registerUser(String username, String passwordHash, String salt) throws SQLException {
		Connection con = null;
		Statement st = null;
		try {
			con = makeSureConnectionIsReady();
			st = con.createStatement();
			String state = "INSERT INTO " + tableName + " (" + columnName + "," + columnPassword + "," + columnSalt + ")" + " VALUES (" + "'" + username + "','" + passwordHash + "','" + salt + "');";
			System.out.println(state);
			st.executeUpdate(state);
		} finally {
			close(st);
			close(con);
		}

	}*/

}
