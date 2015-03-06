package me.markus.bungeelogin;

public class PlayerInfo {

	public String playername;
	public Playerstatus status;
	public int playtime;
	
	public PlayerInfo(String playername,int playtime,Playerstatus status){
		this.playername = playername;
		this.playtime = playtime;
		this.status = status;
	}
	
}
