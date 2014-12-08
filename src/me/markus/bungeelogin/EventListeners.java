package me.markus.bungeelogin;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
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
}
