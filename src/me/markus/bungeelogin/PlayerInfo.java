package me.markus.bungeelogin;

import java.util.GregorianCalendar;

public class PlayerInfo {

	public String playername;
	public Playerstatus status;
	public int playtime;
	public GregorianCalendar joinedAt;
	
	public PlayerInfo(String playername,int playtime,Playerstatus status){
		this.playername = playername; // Playername with lower and upper Case
		this.playtime = playtime;
		this.status = status;
		this.joinedAt = new GregorianCalendar();
	}
	
	public int calcPlayTime(){
		GregorianCalendar now = new GregorianCalendar();
    	int minutes = (int)((now.getTimeInMillis() - this.joinedAt.getTimeInMillis())/1000/60);
    	return this.playtime + minutes;
	}
	
}
