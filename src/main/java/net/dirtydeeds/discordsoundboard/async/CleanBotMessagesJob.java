package net.dirtydeeds.discordsoundboard.async;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.utils.SimpleLog;

public class CleanBotMessagesJob extends AbstractAsyncJob {

    public static final SimpleLog LOG = SimpleLog.getLog("CleanBotMessagesJob");
    private static final long MINUTES = 60000; // 1 minute in ms
    private static final String SERVER_MESSAGE_PREFIX = "**FYI**";
	
	public void handle(SoundboardDispatcher dispatcher) {
		MessageHistory history;
		List<Message> messages;
		List<Guild> completedServers = new LinkedList<>();
		OffsetDateTime lastCheckedTime = (pastEvent != null) ? OffsetDateTime.ofInstant(pastEvent.time.toInstant(), ZoneId.systemDefault()) : null;
		OffsetDateTime tooRecent = OffsetDateTime.ofInstant(new Date(System.currentTimeMillis() - 5*MINUTES).toInstant(), ZoneId.systemDefault()); // 5 minutes ago
		for (SoundboardBot bot : dispatcher.getBots()) {
			long deleted = 0;
			JDA api = bot.getAPI();
			if (api == null) continue;
			for (Guild guild : bot.getGuilds()) {
				if (!completedServers.contains(guild)) {
					// Clean bot's messages from this guild's channels.
					for (MessageChannel channel : guild.getTextChannels()) {
						history = new MessageHistory(channel);
						messages = history.retrieveAll();
						channelloop: while (messages != null && !messages.isEmpty()) {
							for (Message msg : messages) {
								if (lastCheckedTime != null && msg.getTime().isBefore(lastCheckedTime)) {
									break channelloop;
								}
								if (msg.getAuthor() != null && bot.isUser(msg.getAuthor()) && 
										(msg.getEmbeds() == null || msg.getEmbeds().isEmpty()) &&
										!msg.getContent().startsWith(SERVER_MESSAGE_PREFIX) &&
										msg.getTime().isBefore(tooRecent)) {
									// This is a bot message and contains no embeds, is not a server message, and is not a recent message.
									try {
										msg.deleteMessage(); ++deleted;
									} catch (Exception e) {
										LOG.fatal("While deleting message " + msg + " ran into exception.");
										e.printStackTrace();
									}
								}
							}
							try { messages = history.retrieve(); }
							catch (Exception e) { continue; }
						}
					}
					completedServers.add(guild);
				}
			}
			if (deleted > 0) LOG.info("Deleted " + deleted + " messages for bot " + bot.getBotName());
		}
	}

}
