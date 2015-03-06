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
		BungeeLogin.instance.getLogger().info("Spieler " + event.getConnection().getName()+" login");
		//getLogger().info("Yay! It loads!");
	}
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event){
		BungeeLogin.instance.getLogger().info("Spieler " + event.getPlayer()+" disconnect");
		//getLogger().info("Yay! It loads!");
	}
	
	@EventHandler
    public void onChatEvent(ChatEvent event) {
    	
    	if (!event.isCommand()){
    		return;
    	}
    	String cmd = event.getMessage().toLowerCase();
    	if (cmd.startsWith("/l") || cmd.startsWith("/login"))
    		return;
    	System.out.println("cancel command!");
    	event.setCancelled(true);
    }
    
    
    @EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {
    	
        if (!ev.getTag().equals("LoginFoo")) {
            return;
        }
        
        if (!(ev.getSender() instanceof Server)) {
            return;
        }
        System.out.println("this is it");
        
        ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
        	BungeeLogin.instance.getLogger().info(in.readUTF());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void update(){
    	
    }
}
