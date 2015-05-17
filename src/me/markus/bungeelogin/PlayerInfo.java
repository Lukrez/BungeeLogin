package me.markus.bungeelogin;

import java.util.GregorianCalendar;

public class PlayerInfo {

	public String playername;
	public Playerstatus status;
	public int playtime;
	public GregorianCalendar joinedAt;
	
	public PlayerInfo(String playername,int playtime,Playerstatus status){
		this.playername = playername;
		this.playtime = playtime;
		this.status = status;
		this.joinedAt = new GregorianCalendar();
	}
	
}
