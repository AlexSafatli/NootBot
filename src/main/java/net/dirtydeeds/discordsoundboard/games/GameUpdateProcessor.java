package net.dirtydeeds.discordsoundboard.games;

import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;

public interface GameUpdateProcessor {

	public abstract boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user);
	public abstract void process(UserGameUpdateEvent event);
	public abstract boolean isMutuallyExclusive();
	
}