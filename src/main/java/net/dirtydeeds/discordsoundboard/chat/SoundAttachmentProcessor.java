package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message.Attachment;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class SoundAttachmentProcessor implements ChatCommandProcessor {

	protected SoundboardBot bot;
	private static final int MAX_FILE_SIZE_IN_BYTES = 1500000; // 1.5 MB
	
	public SoundAttachmentProcessor(SoundboardBot bot) {
		this.bot = bot;
	}
	
	public void process(MessageReceivedEvent event) {
		if (isApplicableCommand(event)) {
			List<Attachment> attachments = event.getMessage().getAttachments();
			for (Attachment attachment : attachments) {
				String name = attachment.getFileName().toLowerCase(); // Ensure is lowercase.
				String shortName = name.substring(0, name.indexOf("."));
				String extension = name.substring(name.indexOf(".") + 1);
				String category = event.getMessage().getContent();
				if (extension.equals("wav") || extension.equals("mp3")) {
					if (attachment.getSize() < MAX_FILE_SIZE_IN_BYTES) {
						Path downloadPath = bot.getSoundsPath();;
						if (bot.getSoundCategories().contains(category)) {
							downloadPath = bot.getSoundsPath().resolve(category);
						} else {
							boolean notFound = true;
							for (String _category : bot.getSoundCategories()) {
								if (category.equalsIgnoreCase(_category)) {
									downloadPath = bot.getSoundsPath().resolve(_category);
									category = _category; notFound = false; break;
								}
							}
							if (notFound && bot.getSoundCategories().size() > 1) {
								event.getAuthor().getPrivateChannel().sendMessageAsync("*No category was provided " +
										"(or it did not match an existing one)!* " +
										"You can use the **Comment** field when you attach a file to specify a category.",null);
							}
						}
						if (attachment.download(new File(downloadPath.toString(), name))) {
							event.getAuthor().getPrivateChannel().sendMessage(
									"`" + name + "` downloaded and added to list of sounds. Play it with `?" + shortName + "`.\n" + 
									"**Category**: `" + category + "` / **File Size**: " + attachment.getSize() + " bytes");
							String publishMessage = "";
							if (!event.isPrivate()) {
								publishMessage = sendPublishMessage(category, shortName, event.getAuthor(), event.getTextChannel());
							} else {
								for (Guild guild : bot.getGuildsWithUser(event.getAuthor()))
									publishMessage = sendPublishMessage(category, shortName, event.getAuthor(), guild.getPublicChannel());
							}
							if (!event.getAuthor().getUsername().equals(bot.getOwner())) // Alert bot owner as well.
								bot.sendMessageToUser(publishMessage, bot.getOwner());
							bot.getDispatcher().updateFileList(); // Updates file list.
						} else {
							event.getAuthor().getPrivateChannel().sendMessage("Download of file `" + name + "` **failed**!");
						}
						if (!event.isPrivate() && bot.hasPermissionInChannel(event.getTextChannel(), Permission.MESSAGE_MANAGE))
							event.getMessage().deleteMessage();
					} else {
						String end = (event.isPrivate()) ? "" : " *Was this for me? This bot detects any sound upload as a possible entry.*";
						event.getAuthor().getPrivateChannel().sendMessage("File `" + name + "` is too large to add to sound list." + end);
					}
				}
			}
		}
	}

	private String sendPublishMessage(String category, String name, User author, TextChannel channel) {
		String end = (category != null && !category.isEmpty()) ? " to **" + category + "**" : "";
		String msg = "New sound `" + name + "` added" + end + " by " + author.getAsMention() + "!";
		channel.sendMessageAsync(msg, null);
		return msg;
	}
	
	public boolean isApplicableCommand(MessageReceivedEvent event) {
		String author = event.getAuthor().getUsername();
		return (!event.getMessage().getAttachments().isEmpty() && 
				canBeRunBy(event.getAuthor(), event.getGuild()));
	}
	
	public boolean canBeRunByAnyone() {
		return false;
	}

	public boolean canBeRunBy(User user, Guild guild) {
		return bot.isAuthenticated(user, guild);
	}
	
	public String getCommandHelpString() {
		return "**Upload** an `.mp3` or `.wav` file to add it to list of sounds. *Use the Comment field to specify a category.*";
	}

}
