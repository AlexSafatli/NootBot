package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;

import net.dirtydeeds.discordsoundboard.async.DeleteMessageJob;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public abstract class AbstractChatCommandProcessor implements ChatCommandProcessor {

	private final String prefix;
	private final String title;
	private List<Message> buffer;
	protected SoundboardBot bot;
	
	public AbstractChatCommandProcessor(String prefix, String title, SoundboardBot bot) {
		this.prefix = prefix;
		this.title = title;
		this.bot = bot;
		this.buffer = new LinkedList<>();
	}
	
	public void process(MessageReceivedEvent event) {
		if (!isApplicableCommand(event)) return;
		String message = event.getMessage().getContent().toLowerCase();
		clearBuffer();
		handleEvent(event, message);
		if (!event.isFromType(ChannelType.PRIVATE) && bot.hasPermissionInChannel(event.getTextChannel(), Permission.MESSAGE_MANAGE))
			event.getMessage().deleteMessage().queue();
	}

	protected abstract void handleEvent(MessageReceivedEvent event, String message);
	
	private boolean isApplicableCommand(String cmd) {
		return (cmd.toLowerCase().startsWith(prefix) && cmd.length() > 1);
	}
	
	public boolean isApplicableCommand(MessageReceivedEvent event) {
		return isApplicableCommand(event.getMessage().getContent());
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
	
	protected void clearBuffer() {
		for (Message m : buffer) {
			if (bot.hasPermissionInChannel(m.getTextChannel(), Permission.MESSAGE_MANAGE))
				bot.getDispatcher().getAsyncService().runJob(new DeleteMessageJob(m));
		}
		buffer.clear();
	}
	
	protected StyledEmbedMessage buildStyledEmbedMessage() {
		StyledEmbedMessage msg = new StyledEmbedMessage(this.getTitle());
		return msg;
	}
	
	protected void pm(MessageReceivedEvent event, String message) {
		bot.sendMessageToUser(message, event.getAuthor());
	}

	protected void pm(MessageReceivedEvent event, StyledEmbedMessage message) {
		if (!event.getAuthor().hasPrivateChannel())
			try {
				event.getAuthor().openPrivateChannel().block();
			} catch (RateLimitedException e) {
				e.printStackTrace();
			}
		event.getAuthor().getPrivateChannel().sendMessage(message.getMessage()).queue();
	}
	
	private StyledEmbedMessage makeEmbed(String message) {
		StyledEmbedMessage em = new StyledEmbedMessage(getTitle());
		em.addDescription(message);
		return em;
	}

	private void sendEmbed(MessageReceivedEvent event, String message, boolean error, boolean warning) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			pm(event, message);
		} else {
			StyledEmbedMessage em = makeEmbed(message).isWarning(warning).isError(error);
			event.getChannel().sendMessage(em.getMessage()).queue((Message msg)-> {
				buffer.add(msg);
			});
		}
	}

	protected void m(MessageReceivedEvent event, String message) {
		sendEmbed(event, message, false, false);
	}

	protected void w(MessageReceivedEvent event, String message) {
		sendEmbed(event, message, false, true);
	}

	protected void e(MessageReceivedEvent event, String message) {
		sendEmbed(event, message, true, false);
	}
	
	protected void embed(MessageReceivedEvent event, StyledEmbedMessage embed) {
		event.getChannel().sendMessage(embed.getMessage()).queue((Message msg)-> buffer.add(msg));
	}
	
	protected String lookupString(String key) {
		String value = bot.getDispatcher().getStringService().lookup(key);
		return (value != null) ? value : "<String Not Found: " + key + ">";
	}
	
	protected String formatString(String key, Object... args) {
		return String.format(lookupString(key),args);
	}
	
	public String getCommandHelpString() {
		return "`" + getPrefix() + "`"; 
	}

}
