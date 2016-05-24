package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class VersionProcessor extends AbstractChatCommandProcessor {

	public static final String VERSION = "1.7.5";
	public static final String AUTHOR  = "Asaph";
	
	public VersionProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("This bot is currently running version " + VERSION + " of DiscordSoundboard.\n");
		sb.append("This is a hobby software project by **" +  AUTHOR + "**.");
		event.getChannel().sendMessageAsync(sb.toString(), null);
	}
	
	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - prints the running version of the bot";
	}

}
