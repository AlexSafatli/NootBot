package net.dirtydeeds.discordsoundboard.games;

import net.dv8tion.jda.events.user.UserGameUpdateEvent;

public interface GameUpdateProcessor {

	public abstract void process(UserGameUpdateEvent event);
	
}