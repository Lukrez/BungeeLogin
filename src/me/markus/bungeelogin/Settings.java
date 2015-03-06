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
	public static String getMySQLColumnName;
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
		getMySQLColumnName = "username";
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
			getMySQLColumnName = yaml.getString("Datasource.mySQLColumnName");
			getMySQLColumnLoginStatus = yaml.getString("Datasource.mySQLColumnLoginStatus"); // loginStatus
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
		
		Configuration yaml = new Configuration();

		yaml.set("Datasource.mySQLHost", getMySQLHost);
		yaml.set("Datasource.mySQLPort", getMySQLPort);
		yaml.set("Datasource.mySQLUsername", getMySQLUsername);
		yaml.set("Datasource.mySQLPassword", getMySQLPassword);
		yaml.set("Datasource.mySQLTablename", getMySQLTablename);
		yaml.set("Datasource.mySQLDatabase", getMySQLDatabase);
		yaml.set("Datasource.mySQLColumnName", getMySQLColumnName);
		yaml.set("Datasource.mySQLColumnPlaytime", getMySQLColumnPlaytime);
		
		yaml.set("Security.SQLProblem.stopServer", isStopEnabled);


		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(yaml, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
