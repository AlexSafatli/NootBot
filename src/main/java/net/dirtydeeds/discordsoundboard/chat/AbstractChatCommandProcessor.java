package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;
import java.io.*;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public abstract class AbstractChatCommandProcessor implements
	ChatCommandProcessor {

	private final String prefix;
	private final String title;
	protected List<Message> buffer;
	protected SoundboardBot bot;

	public AbstractChatCommandProcessor(String prefix, String title,
	                                    SoundboardBot bot) {
		this.prefix = prefix;
		this.title = title;
		this.bot = bot;
		this.buffer = new LinkedList<>();
	}

	public void process(MessageReceivedEvent event) {
		if (!isApplicableCommand(event)) return;
		clearBuffer();
		try {
			handleEvent(event, event.getMessage().getContent().toLowerCase());
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			String err = sw.toString();
			e(event, "```" + StringUtils.truncate(err, 256) + "```");
			System.err.println(err);
		}
		deleteOriginalMessage(event);
	}

	protected void deleteOriginalMessage(MessageReceivedEvent event) {
		if (!event.isFromType(ChannelType.PRIVATE)) delete(event.getMessage());
	}

	protected abstract void handleEvent(MessageReceivedEvent event,
	                                    String message);

	private boolean isApplicableCommand(String cmd) {
		return (cmd.toLowerCase().startsWith(prefix) && cmd.length() > 1);
	}

	public boolean isApplicableCommand(MessageReceivedEvent event) {
		return isApplicableCommand(event.getMessage().getContent()) &&
		       !bot.isUser(event.getAuthor()); // Never run a command for the bot.
	}

	public boolean canBeRunByAnyone() {
		return true;
	}

	public boolean canBeRunBy(User user, Guild guild) {
		return true;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getTitle() {
		return this.title;
	}

	private void delete(Message m) {
		if (bot.hasPermissionInChannel(
		      m.getTextChannel(), Permission.MESSAGE_MANAGE))
			m.delete().queue();
	}

	protected void clearBuffer() {
		for (Message m : buffer) delete(m);
		buffer.clear();
	}

	protected StyledEmbedMessage buildStyledEmbedMessage(MessageReceivedEvent
	    event) {
		return StyledEmbedMessage.forUser(bot, event.getAuthor(), getTitle(), "");
	}

	protected void pm(MessageReceivedEvent event, String message) {
		bot.sendMessageToUser(message, event.getAuthor());
	}

	protected void pm(MessageReceivedEvent event, StyledEmbedMessage message) {
		event.getAuthor().openPrivateChannel().queue((PrivateChannel c)-> {
			c.sendMessage(message.getMessage()).queue();
		});
	}

	private StyledEmbedMessage makeEmbed(String message, User user) {
		return StyledEmbedMessage.forUser(bot, user, getTitle(), message);
	}

	private void sendEmbed(MessageReceivedEvent event, String message,
	                       boolean error, boolean warning) {
		TextChannel channel;
		if (event.isFromType(ChannelType.PRIVATE)) {
			pm(event, message);
			return;
		} else if (!bot.hasPermissionInChannel(
		             event.getTextChannel(), Permission.MESSAGE_WRITE)) {
			if (bot.getBotChannel(event.getGuild()) != null) {
				channel = bot.getBotChannel(event.getGuild());
			} else {
				pm(event, message);
				return;
			}
		} else {
			channel = event.getTextChannel();
		}
		channel.sendMessage(
		  makeEmbed(message, event.getAuthor()).isWarning(warning).isError(
		    error).getMessage()
		).queue((Message msg)-> {
			buffer.add(msg);
		});
	}

	protected void m(MessageReceivedEvent event, String message) {
		sendEmbed(event, message, false, false);
	}

	protected void w(MessageReceivedEvent event, String message) {
		sendEmbed(event, message, false, true);
	}

	protected void e(MessageReceivedEvent event, String message) {
		StyledEmbedMessage msg = makeEmbed(message, event.getAuthor());
		msg.addContent("Message", "`" +
		               event.getMessage().getContent() + "`", false);
		msg.addContent("Processor", "`" +
		               getClass().getSimpleName() + "`", true);
		embed(event, msg.isError(true));
	}

	protected void embed(MessageReceivedEvent event, StyledEmbedMessage em) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			pm(event, em);
			return;
		}
		TextChannel channel = (bot.hasPermissionInChannel(
		     event.getTextChannel(), Permission.MESSAGE_WRITE)) ?
		  event.getTextChannel() : bot.getBotChannel(event.getGuild());
		if (channel != null && em != null) {
			channel.sendMessage(em.getMessage()).queue(
			  (Message msg)-> buffer.add(msg));
		}
	}

	protected void embedForUser(MessageReceivedEvent event,
	                            StyledEmbedMessage em) {
		//em.addFooterText(StyledEmbedMessage.FOR_USER_FOOTER_PREFIX +
		//                 event.getAuthor().getName());
		em.setFooterIcon(event.getAuthor().getEffectiveAvatarUrl());
		embed(event, em);
	}

	protected String lookupString(String key) {
		String value = bot.getDispatcher().getStringService().lookup(key);
		return (value != null) ? value : "<String Not Found: " + key + ">";
	}

	protected String formatString(String key, Object... args) {
		return String.format(lookupString(key), args);
	}

	public String getCommandHelpString() {
		return getPrefix();
	}

}
