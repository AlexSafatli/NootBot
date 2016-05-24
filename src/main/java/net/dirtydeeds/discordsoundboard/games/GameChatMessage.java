package net.dirtydeeds.discordsoundboard.games;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.MessageEmbed;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class GameChatMessage implements Message {

	private MessageChannel channel;
	private String message;
	private GameContext context;
	private OffsetDateTime timestamp;
	
	public GameChatMessage(MessageChannel channel, String message, GameContext ctx) {
		this.channel = channel;
		this.message = message;
		this.context = ctx;
		this.timestamp = OffsetDateTime.now();
	}
	
	public String getId() {
		return null;
	}

	public List<User> getMentionedUsers() {
		return new LinkedList<User>();
	}

	public List<TextChannel> getMentionedChannels() {
		return new LinkedList<TextChannel>();
	}

	public boolean mentionsEveryone() {
		return false;
	}

	public OffsetDateTime getTime() {
		return timestamp;
	}

	public boolean isEdited() {
		return false;
	}

	public OffsetDateTime getEditedTimestamp() {
		return timestamp;
	}

	public User getAuthor() {
		return context.getUser();
	}

	public String getContent() {
		return message;
	}

	public String getRawContent() {
		return message;
	}

	public String getStrippedContent() {
		return message;
	}

	public boolean isPrivate() {
		return true;
	}

	public String getChannelId() {
		return null;
	}

	//TODO Implement channel interface for games.
	public MessageChannel getChannel() {
		return channel;
	}

	public List<Attachment> getAttachments() {
		return new LinkedList<Attachment>();
	}

	public List<MessageEmbed> getEmbeds() {
		return new LinkedList<MessageEmbed>();
	}

	public boolean isTTS() {
		return false;
	}

	public Message updateMessage(String newContent) {
		return null;
	}

	public void updateMessageAsync(String newContent, Consumer<Message> callback) {

	}

	public void deleteMessage() {

	}

	public JDA getJDA() {
		return null;
	}

}
