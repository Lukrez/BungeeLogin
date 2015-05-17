package me.markus.bungeelogin;

import java.util.HashMap;

import net.md_5.bungee.api.plugin.Plugin;


public class BungeeLogin extends Plugin{
	
	private HashMap<String,PlayerInfo> players;
	public static BungeeLogin instance;
	public MySQLDataSource database;
	
    @Override
    public void onEnable() {
    	instance = this;
    	// setup pluginfolder
    	if (!this.getDataFolder().exists())
			this.getDataFolder().mkdir();
    	
    	// Load setting
    	Settings.loadSettings();
    	
    	// link listeners
    	this.getProxy().getPluginManager().registerListener(this, new EventListeners());
    	
    	
    	try {
    		database = new MySQLDataSource();
		} catch (Exception ex) {
			this.getLogger().severe(ex.getMessage());
			this.getLogger().severe("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
			this.shutdown();
		}
    	
    	// register Pluginchannel
    	this.getProxy().registerChannel("LoginFoo");
    	this.players = new HashMap<String,PlayerInfo>();
        getLogger().info("Finished setup!");
    	
    }
    
    @Override
    public void onDisable() {
    	database.close();
    }
    
    public void shutdown(){
    	if (Settings.isStopEnabled) {
			this.getProxy().stop();
		}
		if (!Settings.isStopEnabled){
		//TODO: Disable plugin
		}
    }
    
    
    public void onPlayerJoin(String playername){
    	if (this.players.containsKey(playername)){
    		return; // Double login handled by server
    	}
    	// Get playerinfo
    	playername = playername.toLowerCase();
    	PlayerInfo pi = database.getPlayerInfo(playername);
    	if (pi == null){ // Guest
    		pi = new PlayerInfo(playername,0,Playerstatus.Guest);
    	} else {
    		pi.status = Playerstatus.Unloggedin;
    		database.updatePlayerData(pi);
    	}
    	this.players.put(playername, pi);
    	System.out.println("finished update playerdata");
    	
    }
    
    public void onPlayerLeave(String playername){
    	playername = playername.toLowerCase();
    	PlayerInfo pi = this.players.get(playername);
    	if (pi == null){
    		return;
    	}
    	
    	pi.status = Playerstatus.Offline;
    	database.updatePlayerData(pi);
    	
    }
    
    public PlayerInfo getPlayer(String playername){
    	playername = playername.toLowerCase();
    	PlayerInfo pi = this.players.get(playername);
    	return pi;
    }
    
    public void verifyPlayerLogin(String playername){
    	// check if player is currently set as unloggedin
    	PlayerInfo current = this.getPlayer(playername);
    	if (current == null)
    		return;
    	if (current.status != Playerstatus.Unloggedin)
    		return;
    	// get new PlayerInfo from database
    	PlayerInfo pi = database.getPlayerInfo(playername);
    	if (pi.status != Playerstatus.Unloggedin){
    		current.status = pi.status;
    	}
    }

    
    
}
