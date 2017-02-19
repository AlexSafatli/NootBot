package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SetNicknameProcessor extends
		OwnerSingleArgumentChatCommandProcessor {
	
	public SetNicknameProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Set Nickname", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String m = event.getMessage().getContent(), newNickName = m.substring(getPrefix().length() + 1).trim();
		if (event.getGuild() == null) { pm(event, lookupString(Strings.NOT_ALLOWED)); return; }
		Member botAsMember = event.getGuild().getMemberById(bot.getAPI().getSelfUser().getId());
		event.getGuild().getController().setNickname(botAsMember, newNickName);
		pm(event, "Changed nickname in server **" + event.getGuild().getName() + "** to **" + newNickName + "** successfully.");
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <nickname>` (`*`) - set a new nickname for this bot in this server";
	}

}
