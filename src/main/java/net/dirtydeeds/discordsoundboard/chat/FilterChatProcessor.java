package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;
import java.util.regex.*;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class FilterChatProcessor implements ChatCommandProcessor {

	private final Pattern regexp;
	private final String channelname;
	protected SoundboardBot bot;

	public FilterChatProcessor(Pattern regexp, String channelname, SoundboardBot bot) {
		this.regexp = regexp;
		this.channelname = channelname;
		this.bot = bot;
	}

	public void process(MessageReceivedEvent event) {
		if (!isApplicableCommand(event)) return;
		try {
			copyMessage(event, event.getMessage().getContent());
			deleteOriginalMessage(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void copyMessage(MessageReceivedEvent event, String message) {
		List<TextChannel> textChannels = event.getGuild().getTextChannelsByName(channelname, true);
		String modified = String.format("%s\n\u2014\nOriginally posted by %s.\n*This was filtered to this channel because it contained a certain pattern.*", message, event.getAuthor().getAsMention());
		for (TextChannel channel : textChannels) {
			channel.sendMessage(modified).queue();
		}
	}

	protected void deleteOriginalMessage(MessageReceivedEvent event) {
		if (!event.isFromType(ChannelType.PRIVATE)) delete(event.getMessage());
	}

	public boolean isApplicableCommand(MessageReceivedEvent event) {
		List<TextChannel> textChannels = event.getGuild().getTextChannelsByName(channelname, true);
		Matcher m = regexp.matcher(event.getMessage().getContent());
		return !textChannels.isEmpty()
		       && !event.isFromType(ChannelType.PRIVATE)
		       && !textChannels.contains(event.getTextChannel())
		       && m.matches();
	}

	public boolean canBeRunByAnyone() {
		return true;
	}

	public boolean canBeRunBy(User user, Guild guild) {
		return true;
	}

	private void delete(Message m) {
		if (bot.hasPermissionInChannel(m.getTextChannel(), Permission.MESSAGE_MANAGE))
			m.deleteMessage().queue();
	}

	public String getCommandHelpString() {
		return "";
	}

}
