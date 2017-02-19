package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AlternateHandleProcessor extends SingleArgumentChatCommandProcessor {
	
	public AlternateHandleProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Alternate Handles", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String handle = getArgument();
		User u = bot.getUser(event.getAuthor());
		if (handle == null) {
			pm(event, "You didn't give me a name. *Twirls thumbs*.");
		} else {
			u.addAlternateHandle(handle);
			bot.getDispatcher().saveUser(u);
			pm(event, "Updated your handles to include `" + handle + "`!");
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <handle>` - let the bot know about another handle you go by on the internet \u2014 used for some optional functionality";
	}

}
