package me.markus.bungeelogin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
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
	public void onLogin(LoginEvent event){
		String playername = event.getConnection().getName();
		BungeeLogin.instance.onPlayerJoin(playername);
	}
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event){
		BungeeLogin.instance.onPlayerLeave(event.getPlayer().getName());
	}
	
	@EventHandler
    public void onChatEvent(ChatEvent event) {
    	
    	if (!event.isCommand()){
    		return;
    	}
    	String playername = event.getSender().toString();
    	// get playerinfo
    	PlayerInfo pi = BungeeLogin.instance.getPlayer(playername);
    	if (pi == null)
    		return;
    	
    	// check if player is not loggedin
    	if (pi.status != Playerstatus.Unloggedin)
    		return;
    	String cmd = event.getMessage().toLowerCase();
    	if (cmd.startsWith("/l") || cmd.startsWith("/login"))
    		return;
    	System.out.println();
    	BungeeLogin.instance.getLogger().info("cancel command from "+playername);
    	event.setCancelled(true);
    }
    
    
    @EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {
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
        	// get playername
        	String playername = message.split("#")[2];
        	this.loginPlayer(playername);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void loginPlayer(String playername){
    	/**
    	 *  Function called if a player has logged in, needs verification over SQL database (to prevent PluginChannel hacking)
    	 *  
    	 */
    	System.out.println("update player "+playername);
    	BungeeLogin plugin = BungeeLogin.instance;
    	
    	PlayerInfo pi = plugin.getPlayer(playername);
    	if (pi.status == Playerstatus.Unloggedin) {
    		pi.status = Playerstatus.Loggedin;
    		plugin.setPlayerHashMapValue(playername, pi);
    	}
    	
    }
}
