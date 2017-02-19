package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

public class DeleteBotMessagesProcessor extends SingleArgumentChatCommandProcessor {

	public static final int NUM_MESSAGES_TO_GO_BACK = 1000;
	private static final int MAX_NUM_MESSAGES_TO_GO_BACK = 99;
	
	public DeleteBotMessagesProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Delete Messages", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		MessageHistory history = new MessageHistory(event.getChannel());
		for (int i = 0; i < NUM_MESSAGES_TO_GO_BACK; i += MAX_NUM_MESSAGES_TO_GO_BACK) {
			RestAction<List<Message>> a = history.retrievePast(MAX_NUM_MESSAGES_TO_GO_BACK);
			a.queue(msgs -> {
				for (Message msg : msgs) {
					if (msg.getAuthor().equals(bot.getAPI().getSelfUser())) msg.deleteMessage().queue();
				}
			});
		}
	}

}
