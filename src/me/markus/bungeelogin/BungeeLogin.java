package me.markus.bungeelogin;

import java.util.HashMap;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;


public class BungeeLogin extends Plugin  {
	
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
    	
    	// link commands
    	this.getProxy().getPluginManager().registerCommand(this, new BungeCheckTime(this));
    	
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
    	// save playerdata
    	for (PlayerInfo pi : this.players.values()){
    		database.updatePlayerData(pi);
    	}
    	getLogger().info("Stored playerdata!");
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
    	String lowerplayername = playername.toLowerCase();
    	PlayerInfo pi = database.getPlayerInfo(lowerplayername);
    	if (pi == null){ // Guest
    		pi = new PlayerInfo(playername,0,Playerstatus.Guest);
    		this.sendBroadcastToAllPlayers("§f Der Gast §e" + playername + "§f hat die Spielewiese betreten!");
    	} else {
    		pi.status = Playerstatus.Unloggedin;
    		database.updatePlayerData(pi);
    	}
    	this.players.put(lowerplayername, pi);
    }
    
    public void onPlayerLeave(String playername){
    	this.sendBroadcastToAllPlayers("§e" + playername + "§f hat die Spielewiese verlassen!");
    	playername = playername.toLowerCase();
    	PlayerInfo pi = this.players.get(playername);
    	if (pi == null){
    		return;
    	}
    	this.players.remove(playername);
    	if (pi.status == Playerstatus.Guest)
    		return;
    	pi.status = Playerstatus.Offline;
    	// TODO: Set playertime
    	database.updatePlayerData(pi);
    }
    
    public PlayerInfo getPlayer(String playername){
    	playername = playername.toLowerCase();
    	PlayerInfo pi = this.players.get(playername);
    	return pi;
    }
    
    /** Public setter-method for players-HashMap  
     * 
     * @param player	key
     * @param pi		value
     */    
    public void setPlayerHashMapValue(String player, PlayerInfo pi) {
    	player = player.toLowerCase();
    	this.players.put(player, pi);
    }
    
    public void sendBroadcastToAllPlayers(String message) {
    	for (PlayerInfo pi : this.players.values()){
    		ProxiedPlayer pp = this.getProxy().getPlayer(pi.playername);
            if (pp != null) 
                pp.sendMessage(new TextComponent(message));
            else
                BungeeLogin.instance.getLogger().info("Could not send broadcast-message to player " + pi.playername);
    	}   
    }
    
    public void senMessageToAll(String message){
    	
    }
    
}

class BungeCheckTime extends Command{
	private BungeeLogin plugin;
	
	public BungeCheckTime(BungeeLogin plugin) {
	      super("playtime");
	      this.plugin = plugin;
	  }

	@Override
	public void execute(CommandSender sender, String[] args) {
		/*if (!sender.hasPermission("easylogin.manage")){
			return;
		}*/
		String playername;
		String s;
		if (args.length == 0) {
			playername = sender.getName().toLowerCase();
			s = "Du hast bisher ";
		} else {
			playername = args[0].toLowerCase();
			s = "Der Spieler "+ playername + " hat bisher ";
		}
		PlayerInfo pi = this.plugin.getPlayer(playername);
		if (pi == null){
			sender.sendMessage(new TextComponent("Spieler ist momentan nicht eingeloggt!"));
			return;
		}
		int minutes = pi.calcPlayTime();
		// calculate time format
		int weeks = minutes/(60*24*7);
		int days = (minutes/(60*24))%7;
		int hours = (minutes/60)%24;
		int remminutes = minutes%60;
		
		if (weeks == 1){
			s += weeks + " Woche ";
		} else if (weeks > 1){
			s += weeks + " Wochen ";
		}
		if (days == 1){
			s += days + " Tag ";
		} else if (days > 1){
			s += days + " Tage ";
		}
		
		if (hours == 1){
			s += hours + " Stunde ";
		} else if (hours > 1){
			s += hours + " Stunden ";
		}
		if (remminutes == 1){
			s += remminutes + " Minute auf dem Server gespielt!";
		} else {
			s += remminutes + " Minuten auf dem Server gespielt!";
		}
		
		sender.sendMessage(new TextComponent(s));
	}
}