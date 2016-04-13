package me.markus.bungeelogin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import me.markus.bungeechat.GlobalChatEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListeners implements Listener{

	/**
	 * Status: 
	 * Offline
	 * Guest
	 * Unloggedin
	 * Loggedin
	 * 
	 * @param event
	 */
	
	@EventHandler
	public void onLogin(PreLoginEvent event){
		if (event.isCancelled())
			return;
		String playername = event.getConnection().getName();
		if (!BungeeLogin.instance.onPlayerJoin(playername)) {
			event.setCancelled(true);
			event.setCancelReason("Der Server ist momentan überlastet, bitte versuche es später noch einmal!");
		}
	}
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event){
		BungeeLogin.instance.onPlayerLeave(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onBungeeLoginServerConnectEvent(ServerConnectEvent event) {

		// all players are allowed to join the lobby-server
		if (event.getTarget().getName().equalsIgnoreCase("lobby"))
			return;
	
		// get the playerinfo of the player
		ProxiedPlayer player = event.getPlayer();
		PlayerInfo pi = BungeeLogin.instance.getPlayer(player.getName());
		
	
		//check the status
		if (pi == null) {
			event.setCancelled(true);
			return;
		}
		pi.isRegistering = false; // Remove from registering, should only happen during server shutdown/crash
		if (pi.status == Playerstatus.Offline) {
				event.setCancelled(true);			
				return;
		} else if (pi.status == Playerstatus.Unloggedin) {
			event.setCancelled(true);
			player.sendMessage(new TextComponent("Bitte logge dich ein, um den Server wechseln zu können."));
			return;
		} else if (pi.status == Playerstatus.Guest) {
			event.setCancelled(true);
			player.sendMessage(new TextComponent("Du kannst als Gast den Lobby-Server nicht verlassen!"));
			return;
		} 
		
		BungeeLogin.instance.getLogger().info("cancel ServerConnectEvent from " + player.getName());
	}
	
	@EventHandler
    public void onCommandEvent(ChatEvent event) {                
                
        String cmd = event.getMessage().toLowerCase();
        //check for valid commands
        if (cmd.startsWith("/l ") || cmd.startsWith("/login "))
            return;
            
        String playername = event.getSender().toString();
        ProxiedPlayer player = BungeeLogin.instance.getProxy().getPlayer(playername);
        
        PlayerInfo pi = BungeeLogin.instance.getPlayer(playername);
        if (pi == null || pi.status == Playerstatus.Guest) {
        	if (cmd.startsWith("/server")){
	            //Guest
	        	player.sendMessage(new TextComponent("Du kannst als Gast den Lobby-Server nicht verlassen!"));
	            BungeeLogin.instance.getLogger().info("cancel command from "+playername);
	            event.setCancelled(true);
	            return;
        	}
        }
        else if (pi.status == Playerstatus.Unloggedin) {
        	player.sendMessage(new TextComponent("Du musst dich einloggen um chatten oder Befehle eingeben zu können!"));
            BungeeLogin.instance.getLogger().info("cancel command ChatEvent from unloggedin "+playername);
            event.setCancelled(true);
        } 
        if (pi.isRegistering == true && cmd.startsWith("/")) {
        	event.setCancelled(true);
        }
    }
	
	@EventHandler
    public void onGlobalChatEvent(GlobalChatEvent event) { 
	        if (event.getMessage().startsWith("/")){
	        	return;
	        }
	        String playername = event.getSender().toString();
	        
	        PlayerInfo pi = BungeeLogin.instance.getPlayer(playername);     
	        if (pi.status == Playerstatus.Unloggedin) {
	            BungeeLogin.instance.getLogger().info("cancel global chat from "+playername);
	            event.setCancelled(true);
	        }      
	}
	
    @EventHandler
    public void onPluginMessagePlayerLogin(PluginMessageEvent ev) {
    	/**
    	 * PluginChannel, used to get a update from the server that a player has logged in
    	 */
    	
        if (!ev.getTag().equals("LoginFoo")) {
            return;
        }
        
        if (!(ev.getSender() instanceof Server)) {
            return;
        }
        
        ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
        	String message = in.readUTF();
        	if (!message.matches("#Playerlogin#.+#"))
        		return;

        	String lwcplayername = message.split("#")[2];
        	PlayerInfo pi = BungeeLogin.instance.getPlayer(lwcplayername);
        	// Verify if player is logged in via EasyLogin
        	if (this.loginPlayer(lwcplayername)){
        		// get playername
        		BungeeLogin.instance.sendBroadcastToAllPlayers("§e" + pi.playername + "§f hat die Spielewiese betreten!");
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    private boolean loginPlayer(String lwcplayername){
    	/**
    	 *  Function called if a player has logged in, needs verification over SQL database (to prevent PluginChannel hacking)
    	 *  
    	 */
    	BungeeLogin plugin = BungeeLogin.instance;
    	PlayerInfo unloggedinUser = plugin.getPlayer(lwcplayername);
    	if (unloggedinUser.status != Playerstatus.Unloggedin)
    		return false;
    	// get verification from database
    	PlayerInfo checkPlayer = plugin.database.getPlayerInfo(lwcplayername);
    	
    	if (checkPlayer.status == Playerstatus.Loggedin){
    		unloggedinUser.status = Playerstatus.Loggedin;
    		return true;
    	}
    	return false;
    	
    }
    
    @EventHandler
    public void onPluginMessagePlayerRegister(PluginMessageEvent ev) {   	
        if (!ev.getTag().equals("Register")) {
            return;
        }
        
        if (!(ev.getSender() instanceof Server)) {
            return;
        }
        
        ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
        	String message = in.readUTF();
        	boolean isRegistering;
        	if (message.matches("#start#.+#")) {
        		isRegistering = true;
        	} else if (message.matches("#exit#.+#")) {
        		isRegistering = false;
        	} else {
        		return;
        	}
        	String playername = message.split("#")[2];
        	ProxiedPlayer player = BungeeLogin.instance.getProxy().getPlayer(playername);
        	if (player == null) {
        		return;
        	}
        	PlayerInfo pi = BungeeLogin.instance.getPlayer(playername);
        	if (pi == null)
        		return;
        	pi.isRegistering = isRegistering;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
