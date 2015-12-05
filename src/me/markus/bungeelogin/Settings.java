package me.markus.bungeelogin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;


public class Settings {

	public static String getMySQLHost;
	public static String getMySQLPort;
	public static String getMySQLUsername;
	public static String getMySQLDatabase;
	public static String getMySQLTablename;
	public static String getMySQLPassword;
	public static boolean isStopEnabled;
	public static String getMySQLColumnPlayerName;
	public static String getMySQLColumnLoginStatus;
	public static String getMySQLColumnPlaytime;
	

	public static void loadSettings() {

		// Default settings
		getMySQLHost = "foo.server.com";
		getMySQLPort = "1234";
		getMySQLUsername = "sqlAdmin";
		getMySQLDatabase = "forumDB";
		getMySQLTablename = "all_users";
		getMySQLPassword = "foobar";
		isStopEnabled = true;
		getMySQLColumnPlayerName = "username";
		getMySQLColumnLoginStatus = "loginStatus";
		getMySQLColumnPlaytime = "playtime";

		File file = new File(BungeeLogin.instance.getDataFolder(), "config.yml");
		if (!file.exists())
			saveSettings();
		
		try {
			Configuration yaml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
			
			getMySQLHost = yaml.getString("Datasource.mySQLHost");
			getMySQLPort = yaml.getString("Datasource.mySQLPort");
			getMySQLUsername = yaml.getString("Datasource.mySQLUsername");
			getMySQLDatabase = yaml.getString("Datasource.mySQLDatabase");
			getMySQLTablename = yaml.getString("Datasource.mySQLTablename");
			getMySQLPassword = yaml.getString("Datasource.mySQLPassword");
			getMySQLColumnPlayerName = yaml.getString("Datasource.mySQLColumnPlayerName");
			getMySQLColumnLoginStatus = yaml.getString("Datasource.mySQLColumnLoginStatus");
			getMySQLColumnPlaytime = yaml.getString("Datasource.mySQLColumnPlaytime");
			
			isStopEnabled = yaml.getBoolean("Security.SQLProblem.stopServer");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveSettings();
	}

	public static void saveSettings() {
		File file = new File(BungeeLogin.instance.getDataFolder(), "config.yml");
		try {
			if (!file.exists()){
				file.createNewFile();
			}
				
			Configuration yaml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
			yaml.set("Datasource.mySQLHost", getMySQLHost);
			yaml.set("Datasource.mySQLPort", getMySQLPort);
			yaml.set("Datasource.mySQLUsername", getMySQLUsername);
			yaml.set("Datasource.mySQLPassword", getMySQLPassword);
			yaml.set("Datasource.mySQLTablename", getMySQLTablename);
			yaml.set("Datasource.mySQLDatabase", getMySQLDatabase);
			yaml.set("Datasource.mySQLColumnPlayerName", getMySQLColumnPlayerName);
			yaml.set("Datasource.mySQLColumnPlaytime", getMySQLColumnPlaytime);
			yaml.set("Datasource.mySQLColumnLoginStatus", getMySQLColumnLoginStatus);
			
			yaml.set("Security.SQLProblem.stopServer", isStopEnabled);
			
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(yaml, file);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
}
