package net.dirtydeeds.discordsoundboard.games.leagueoflegends;

import java.io.File;
import java.util.function.Consumer;

import net.dirtydeeds.discordsoundboard.games.GameChatMessage;
import net.dirtydeeds.discordsoundboard.games.GameContext;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

public class LeagueOfLegendsChatChannel implements MessageChannel {

	//private Friend recipient;
	private GameContext context;
	
	public LeagueOfLegendsChatChannel(User user) {
		//recipient = friend;
		//friend.getStatus().getGameStatus().equals(GameStatus.IN_GAME)
		context = new GameContext(user, "League of Legends", true);
	}
	
	public Message sendMessage(String text) {
		Message msg = new GameChatMessage(this, text, context);
		//recipient.sendMessage(text.replace("\n", " ").replace("**", "*"));
		return msg;
	}

	public Message sendMessage(Message msg) {
		return sendMessage(msg.getContent());
	}

	public void sendMessageAsync(String msg, Consumer<Message> callback) {
		sendMessage(msg);
	}

	public void sendMessageAsync(Message msg, Consumer<Message> callback) {
		sendMessage(msg.getContent());
	}

	public Message sendFile(File file, Message message) {
		return null;
	}

	public void sendFileAsync(File file, Message message,
			Consumer<Message> callback) {
	}

	public void sendTyping() {
	}
	
	public GameContext getContext() {
		return context;
	}

}
