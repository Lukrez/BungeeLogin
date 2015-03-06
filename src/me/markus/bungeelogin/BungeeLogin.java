package me.markus.bungeelogin;

import java.util.logging.Logger;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;


public class BungeeLogin extends Plugin implements Listener{
	
	//private HashMap<String,PlayerInfo> players;
	public static BungeeLogin instance;
	public static Logger logger;
	public MySQLDataSource database;
	
    @Override
    public void onEnable() {
    	instance = this;
    	logger = this.getProxy().getLogger();
        // You should not put an enable message in your plugin.
        // BungeeCord already does so
    	this.getProxy().getPluginManager().registerListener(this, new EventListeners());
    	
    	/*
    	try {
    		MySQLDataSource database = new MySQLDataSource();
		} catch (Exception ex) {
			this.getLogger().severe(ex.getMessage());
			this.getLogger().severe("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
			this.shutdown();
		}*/
    	
    	// register Pluginchannel
    	this.getProxy().registerChannel("LoginFoo");
    	this.getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("Yay! It loads!");
    	
    }
    
    @Override
    public void onDisable() {
    }
    
    public void shutdown(){
    	if (Settings.isStopEnabled) {
			this.getProxy().stop();
		}
		if (!Settings.isStopEnabled){
		//TODO: Disable plugin
		}
    }

    
    
}
