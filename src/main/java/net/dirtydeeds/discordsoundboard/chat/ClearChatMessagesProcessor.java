package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.async.LambdaJob;
import net.dirtydeeds.discordsoundboard.async.SoundboardJob;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class ClearChatMessagesProcessor extends
		AuthenticatedSingleArgumentChatCommandProcessor {
	
	public ClearChatMessagesProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		Guild guild = event.getGuild();
		if (event.isPrivate() || guild == null) {
			pm(event, lookupString(Strings.SERVER_ONLY));
			return;
		}
		MessageChannel channel = event.getChannel();
		SoundboardJob job = new LambdaJob((SoundboardBot b)-> b.deleteHistoryForChannel(channel));
		bot.getDispatcher().getAsyncService().runJob(job);
		pm(event, "Messages will be deleted soon. Please wait.");
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` (`*`) - clear a channel of *all* messages";
	}

}
