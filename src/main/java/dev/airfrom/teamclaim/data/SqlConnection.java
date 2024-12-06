package dev.airfrom.teamclaim.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import dev.airfrom.teamclaim.Main;

public class SqlConnection {

	public Connection connection;
	private String url, host, database, username, password;
	private int port;
	private Main main;
	
	public SqlConnection(String url, String host, int port, String database, String username, String password, Main main) {
		this.url = url;
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		this.main = main;
	}
	
	public void connection() {
		if(isConnected()) return;
		 try {
			connection = DriverManager.getConnection(url + host + ":" + port + "/" + database, username, password);
			Bukkit.getLogger().info(main.console_pre+"["+main.ANSI_GREEN+"+"+main.ANSI_RESET+"]"+" Successfully connected to database");
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet table = dbm.getTables(null, null, "PlayerData", null);
			if (!table.next()) createTablePlayerData();
			ResultSet table2 = dbm.getTables(null, null, "TeamData", null);
			if (!table2.next()) createTableTeamData();
			ResultSet table3 = dbm.getTables(null, null, "ClaimedLand", null);
			if (!table3.next()) createTableClaimedLand();
		 } catch (SQLException e) {
			Bukkit.getLogger().severe("Connection failed: " + e.getMessage());
			e.printStackTrace();
			createTablePlayerData();
			createTableTeamData();
			createTableClaimedLand();
			connection2();
		}
	}
	
	public void disconnect() {
		if(!isConnected()) return;
		try {
			connection.close();
			Bukkit.getLogger().info(main.console_pre+"["+main.ANSI_RED+"-"+main.ANSI_RESET+"]"+" Successfully disconnected from database");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		return connection != null;
	}
	
	private void createTablePlayerData() {
		String PlayerData = "CREATE TABLE IF NOT EXISTS PlayerData (uuid varchar(36) PRIMARY KEY, team varchar(36), claim_blocks int, play_time bigint);"; 
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(PlayerData);
            Bukkit.getLogger().info(main.console_pre+main.ANSI_GREEN+"PlayerData Table Created"+main.ANSI_RESET);
        }
        catch (SQLException e ) {
        	Bukkit.getLogger().severe("An error has occurred on PlayerData Table Creation");
            e.printStackTrace();
        }
    }
	
	private void createTableTeamData() {
		String TeamData = "CREATE TABLE IF NOT EXISTS TeamData (uuid varchar(36) PRIMARY KEY, name text, leader varchar(36), members text, claim_blocks int, created_date int);"; 
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(TeamData);
            Bukkit.getLogger().info(main.console_pre+main.ANSI_GREEN+"TeamData Table Created"+main.ANSI_RESET);
        }
        catch (SQLException e ) {
        	Bukkit.getLogger().severe("An error has occurred on TeamData Table Creation");
            e.printStackTrace();
        }
    }
	
	private void createTableClaimedLand() {
		String landData = "CREATE TABLE IF NOT EXISTS ClaimedLand (x int, z int, team varchar(36), world_name text, PRIMARY KEY (x, z));"; 
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(landData);
            Bukkit.getLogger().info(main.console_pre+main.ANSI_GREEN+"ClaimedLand Table Created"+main.ANSI_RESET);
        }
        catch (SQLException e ) {
        	Bukkit.getLogger().severe("An error has occurred on ClaimedLand Table Creation");
            e.printStackTrace();
        }
    }
	
	public void connection2() {
		if(isConnected()) return;
		 try {
			connection = DriverManager.getConnection(url + host + ":" + port + "/" + database, username, password);
			Bukkit.getLogger().info(main.console_pre+"["+main.ANSI_GREEN+"+"+main.ANSI_RESET+"]"+" Successfully connected to database");
		 } catch (SQLException e) {
			 Bukkit.getLogger().severe("Failed for the second time trying to connect to the database");
			e.printStackTrace();
		}
	}
	
	public List<UUID> fetchAllPlayerData() throws SQLException {
        String query = "SELECT uuid FROM PlayerData";
        List<UUID> playerDataList = new ArrayList<UUID>();

        try(PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while(resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                playerDataList.add(uuid);
            }
        }
        return playerDataList;
    }
	
	public void incrementPlayTime(UUID playerUUID, int incrementValue) throws SQLException {
	    String query = "UPDATE PlayerData SET play_time = play_time + ? WHERE uuid = ?";

	    try(PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, incrementValue);
	        stmt.setString(2, playerUUID.toString());
	        stmt.executeUpdate();
	    }
	}

	
}
