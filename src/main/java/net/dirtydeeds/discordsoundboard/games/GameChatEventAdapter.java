package net.dirtydeeds.discordsoundboard.games;

public interface GameChatEventAdapter {

	public abstract void process(String message, GameContext context);
	public abstract boolean isApplicableCommand(String message, GameContext context);
	
}
