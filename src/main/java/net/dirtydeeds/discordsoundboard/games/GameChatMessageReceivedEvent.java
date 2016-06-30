package net.dirtydeeds.discordsoundboard.games;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class GameChatMessageReceivedEvent extends MessageReceivedEvent {

	public GameChatMessageReceivedEvent(JDA api, int responseNumber, Message message) {
		super(api, responseNumber, message);
	}
	
	@Override
	public MessageChannel getChannel() {
		return getMessage().getChannel();
	}

}