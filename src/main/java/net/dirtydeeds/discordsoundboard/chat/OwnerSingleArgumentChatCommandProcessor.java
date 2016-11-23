package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

public abstract class OwnerSingleArgumentChatCommandProcessor extends
		AuthenticatedSingleArgumentChatCommandProcessor {

	public OwnerSingleArgumentChatCommandProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	@Override
	public boolean canBeRunBy(User user, Guild guild) {
		return bot.getOwner().equals(user.getUsername());
	}
	
}
