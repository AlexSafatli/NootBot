package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dirtydeeds.discordsoundboard.utils.ChatUtils;

public class DeleteBotMessagesProcessor extends SingleArgumentChatCommandProcessor {
	
	public DeleteBotMessagesProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Delete Messages", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		ChatUtils.clearBotMessagesInChannel(bot, event.getChannel());
	}

}