package me.markus.bungeelogin;

import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLogin extends Plugin {
	public static BungeeLogin instance;
    @Override
    public void onEnable() {
    	instance = this;
        // You should not put an enable message in your plugin.
        // BungeeCord already does so
    	this.getProxy().getPluginManager().registerListener(this, new EventListeners());
    	
    	try {
			database = new MySQLDataSource();
		} catch (Exception ex) {
			this.getLogger().severe(ex.getMessage());
			if (Settings.isStopEnabled) {
				this.getLogger().severe("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
				this.getServer().shutdown();
			}
			if (!Settings.isStopEnabled)
				this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
    	
    }
}

