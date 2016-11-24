package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.hooks.ListenerAdapter;

public abstract class AbstractListener extends ListenerAdapter {

	protected SoundboardBot bot;
	
	protected String lookupString(String key) {
		String value = bot.getDispatcher().getStringService().lookup(key);
		return (value != null) ? value : "<String Not Found: " + key + ">";
	}
	
	protected String formatString(String key, Object... args) {
		return String.format(lookupString(key),args);
	}
	
}