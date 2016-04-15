package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;

public abstract class AbstractGameUpdateProcessor implements GameUpdateProcessor {

	protected SoundboardBot bot;
	
	public AbstractGameUpdateProcessor(SoundboardBot bot) {
		this.bot = bot;
	}
	
	protected abstract void handleEvent(UserGameUpdateEvent event, User user);
	
	public void process(UserGameUpdateEvent event) {
		handleEvent(event, event.getUser());
	}
	
}