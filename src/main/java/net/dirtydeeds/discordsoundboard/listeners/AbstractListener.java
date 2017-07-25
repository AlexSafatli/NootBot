package net.dirtydeeds.discordsoundboard.listeners;

import java.util.function.Consumer;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class AbstractListener extends ListenerAdapter {

	protected SoundboardBot bot;

	protected String lookupString(String key) {
		String value = bot.getDispatcher().getStringService().lookup(key);
		return (value != null) ? value : "<String Not Found: " + key + ">";
	}

	protected String formatString(String key, Object... args) {
		return String.format(lookupString(key), args);
	}

	protected void embed(TextChannel channel, StyledEmbedMessage embed) {
		if (bot.hasPermissionInChannel(channel, Permission.MESSAGE_WRITE)) {
			channel.sendMessage(embed.getMessage()).queue();
		}
	}

	protected void embed(TextChannel channel, StyledEmbedMessage embed, Consumer<Message> m) {
		if (bot.hasPermissionInChannel(channel, Permission.MESSAGE_WRITE)) {
			channel.sendMessage(embed.getMessage()).queue(m);
		}
	}

}