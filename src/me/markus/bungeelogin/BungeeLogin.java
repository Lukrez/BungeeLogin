package me.markus.bungeelogin;

import java.util.Date;
import java.util.HashMap;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;


public class BungeeLogin extends Plugin  {
	
	private HashMap<String,PlayerInfo> players; // Keeps information about all current players on the server, Key is the lowercase playername
	public static BungeeLogin instance;
	public MySQLDataSource database;
	public boolean spamBotAttack;
	private int nrLogins;
	private long lastLoginCycle;
	
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
    	this.getProxy().getPluginManager().registerCommand(this, new BungeGuestBlacklist(this));
    	
    	try {
    		database = new MySQLDataSource();
		} catch (Exception ex) {
			this.getLogger().severe(ex.getMessage());
			this.getLogger().severe("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
			this.shutdown();
		}
    	
    	// register Pluginchannels
    	this.getProxy().registerChannel("LoginFoo");
    	this.getProxy().registerChannel("Register");
    	
    	this.players = new HashMap<String,PlayerInfo>();
    	this.spamBotAttack = false;
    	this.nrLogins = 0;
    	this.lastLoginCycle = 0;
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
    
    
    public boolean onPlayerJoin(String playername){
    	
    	// Get playerinfo
    	String lwcplayername = playername.toLowerCase();
    	if (this.players.containsKey(lwcplayername)){
    		return false; // Double login handled by server
    	}
    	PlayerInfo pi = database.getPlayerInfo(playername);
    	
    	checkSpamBotStatus();
    	if (pi == null){ // Guest
    		this.nrLogins++; // TODO: Maybe move into Guest-Detection?
    		if (Settings.areGuestsBlacklisted == true)
    			return false;
    		if (this.spamBotAttack == true) 
    			return false;
    		this.sendBroadcastToAllPlayers("§f Der Gast §e" + playername + "§f hat die Spielewiese betreten!");
    		pi = new PlayerInfo(playername,0,Playerstatus.Guest);
    		
    	} else {
    		pi.status = Playerstatus.Unloggedin;
    		database.updatePlayerData(pi);
    	}
    	this.players.put(lwcplayername, pi);
    	return true;
    }
    
    public void onPlayerLeave(String playername){
    	
    	String lwrplayername = playername.toLowerCase();
    	PlayerInfo pi = this.players.remove(lwrplayername);
    	if (pi == null){
    		return;
    	}
    	
    	if (pi.status == Playerstatus.Guest){
    		if (this.spamBotAttack == false) 
    			this.sendBroadcastToAllPlayers("§e" + playername + "§f hat die Spielewiese verlassen!");
    		return;
    	}
    	if (pi.status != Playerstatus.Unloggedin) {
    		this.sendBroadcastToAllPlayers("§e" + playername + "§f hat die Spielewiese verlassen!");	
    	}
    	pi.status = Playerstatus.Offline;
    	// TODO: Set playertime
    	database.updatePlayerData(pi);
    }
    
    public PlayerInfo getPlayer(String playername){
    	playername = playername.toLowerCase();
    	PlayerInfo pi = this.players.get(playername);
    	return pi;
    }
    
    public void sendBroadcastToAllPlayers(String message) {
    	for (PlayerInfo pi : this.players.values()){
    		if (pi.isRegistering == true)
    			continue;
    		ProxiedPlayer pp = this.getProxy().getPlayer(pi.playername);
            if (pp != null){
                pp.sendMessage(new TextComponent(message));
            }
    	}   
    }
    
    public void kickGuests(TextComponent message){
    	for (PlayerInfo pi : this.players.values()){
    		if (pi.status == Playerstatus.Guest){
    			this.getProxy().getPlayer(pi.playername).disconnect(message);
    		}
    	}
    }
    
	private void checkSpamBotStatus() {
		if (Settings.getLoginsPerTenSeconds > 0 && this.nrLogins > Settings.getLoginsPerTenSeconds) {
			this.nrLogins = 0;
			this.lastLoginCycle = new Date().getTime();
		}
		boolean newValue = Settings.getLoginsPerTenSeconds > 0 && new Date().getTime() - this.lastLoginCycle < 10000;
		// check if value changed
		if (newValue != this.spamBotAttack){
			if (newValue == true){
				this.getLogger().info("Spambotattacke erkannt, leite Gegenmaßnahmen ein!");
			} else {
				this.getLogger().info("Spambotattacke hat aufgehört, stelle Gegenmaßnahmen ein!");
			}
		}
		
		this.spamBotAttack = newValue;
	}
}

class BungeGuestBlacklist extends Command{
	private BungeeLogin plugin;
	
	public BungeGuestBlacklist(BungeeLogin plugin) {
	      super("guestblacklist");
	      this.plugin = plugin;
	  }

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("bungeelogin.guestwhitelist")){
			return;
		}
		if (args.length < 1) {
			sender.sendMessage(new TextComponent("/guestblacklist ON|OFF"));
			return;
		}
		if (args[0].toLowerCase().equals("off")){
			Settings.areGuestsBlacklisted = false;
			sender.sendMessage(new TextComponent("Gäste können nun joinen!"));
			return;
		}
		if (args[0].toLowerCase().equals("on")){
			Settings.areGuestsBlacklisted = true;
			this.plugin.kickGuests(new TextComponent("Registriere dich bitte auf www.minecraft-spielewiese.de!"));
			sender.sendMessage(new TextComponent("Gäste werden geblockt!"));
			return;	
		}
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