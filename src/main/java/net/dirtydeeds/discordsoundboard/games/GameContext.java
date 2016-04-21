package net.dirtydeeds.discordsoundboard.games;

public class GameContext {

	private String gameName;
	private boolean ingame = false;
	
	public GameContext(String gameName) {
		this.gameName = gameName;
	}
	
	public GameContext(String gameName, boolean ingame) {
		this(gameName);
		this.ingame = ingame;
	}
	
	public String getName() {
		return this.gameName;
	}
	
	public boolean isInGame() {
		return this.ingame;
	}
	
}
