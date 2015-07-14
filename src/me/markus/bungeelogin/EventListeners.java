package me.markus.bungeelogin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
                
        String cmd = event.getMessage().toLowerCase();
        //check for valid commands
        if (cmd.startsWith("/l") || cmd.startsWith("/login"))
            return;
            
        String playername = event.getSender().toString();
        ProxiedPlayer player = BungeeLogin.instance.getProxy().getPlayer(playername);
        
        PlayerInfo pi = BungeeLogin.instance.getPlayer(playername);     
        if (pi == null || pi.status == Playerstatus.Guest) {
            //Guest
        	player.sendMessage(new TextComponent("Diesen Befehl kannst du als Gast nicht verwenden!"));
            BungeeLogin.instance.getLogger().info("cancel command from "+playername);
            event.setCancelled(true);
            return;
        }
        else if (pi.status == Playerstatus.Unloggedin) {
        	player.sendMessage(new TextComponent("Du musst dich einloggen um chatten oder Befehle eingeben zu können!"));
            BungeeLogin.instance.getLogger().info("cancel command from "+playername);
            event.setCancelled(true);
        }       
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
    	BungeeLogin plugin = BungeeLogin.instance;
    	
    	PlayerInfo pi = plugin.getPlayer(playername);
    	if (pi.status == Playerstatus.Unloggedin) {
    		pi.status = Playerstatus.Loggedin;
    		plugin.setPlayerHashMapValue(playername, pi);
    	}
    	
    }
}
