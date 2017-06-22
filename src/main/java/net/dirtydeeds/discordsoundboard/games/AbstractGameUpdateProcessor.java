package net.dirtydeeds.discordsoundboard.games;

import java.util.function.Consumer;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.requests.RestAction;

public abstract class AbstractGameUpdateProcessor implements GameUpdateProcessor {

	protected SoundboardBot bot;
	
	public AbstractGameUpdateProcessor(SoundboardBot bot) {
		this.bot = bot;
	}
	
	protected abstract void handleEvent(UserGameUpdateEvent event, User user);
	public abstract boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user);
	
	public void process(UserGameUpdateEvent event) {
		handleEvent(event, event.getUser());
	}
	
	protected String lookupString(String key) {
		String value = bot.getDispatcher().getStringService().lookup(key);
		return (value != null) ? value : "<String Not Found: " + key + ">";
	}
	
	protected String formatString(String key, Object... args) {
		return String.format(lookupString(key),args);
	}
	
	protected void embed(TextChannel channel, StyledEmbedMessage embed) {
		channel.sendMessage(embed.getMessage()).queue();
	}
	
	protected void embed(TextChannel channel, StyledEmbedMessage embed, Consumer<Message> m) {
		RestAction<Message> ra = channel.sendMessage(embed.getMessage());
		ra.queue(m);
	}

	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}