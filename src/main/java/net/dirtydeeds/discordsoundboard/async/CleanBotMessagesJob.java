package net.dirtydeeds.discordsoundboard.async;

import java.time.OffsetDateTime;
import java.time.ZoneId;
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
	
	public void handle(SoundboardDispatcher dispatcher) {
		LOG.info("Initiating job.");
		MessageHistory history;
		List<Message> messages;
		List<Guild> completedServers = new LinkedList<>();
		OffsetDateTime lastCheckedTime = (pastEvent != null) ? OffsetDateTime.ofInstant(pastEvent.time.toInstant(), ZoneId.systemDefault()) : null;
		for (SoundboardBot bot : dispatcher.getBots()) {
			long deleted = 0;
			JDA api = bot.getAPI();
			if (api == null) continue;
			for (Guild guild : bot.getGuilds()) {
				if (!completedServers.contains(guild)) {
					LOG.info("Starting to clean messages for server " + guild.getName());
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
										(msg.getEmbeds() == null || msg.getEmbeds().isEmpty())) {
									// This is a bot message and contains no embeds.
									try {
										msg.deleteMessage(); ++deleted;
										LOG.info("Deleted message with content: \"" + msg.getContent().substring(0,Math.min(msg.getContent().length(), 10)) + "...\".");
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
					LOG.info("Finished server " + guild.getName());
					completedServers.add(guild);
				}
			}
			LOG.info("Deleted " + deleted + " messages for bot " + bot.getName());
		}
	}

}
