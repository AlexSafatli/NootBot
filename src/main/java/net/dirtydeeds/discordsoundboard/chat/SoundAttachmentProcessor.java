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
				String name = attachment.getFileName();
				String shortName = name.substring(0, name.indexOf("."));
				String extension = name.substring(name.indexOf(".") + 1);
				String category = event.getMessage().getContent();
				if (extension.equals("wav") || extension.equals("mp3")) {
					if (attachment.getSize() < MAX_FILE_SIZE_IN_BYTES) {
						Path downloadPath;
						if (bot.getSoundCategories().contains(category)) {
							downloadPath = bot.getSoundsPath().resolve(category);
						} else downloadPath = bot.getSoundsPath();
						if (attachment.download(new File(downloadPath.toString(), name))) {
							event.getAuthor().getPrivateChannel().sendMessage(
									"Downloaded file `" + name + "` and added to list of sounds.\n" + 
									"**Category**: `" + category + "` / **File Size**: " + attachment.getSize() + " bytes");
							if (!event.isPrivate()) {
								sendPublishMessage(category, shortName, event.getAuthor(), event.getTextChannel());
							} else {
								for (Guild guild : bot.getGuildsWithUser(event.getAuthor())) {
									sendPublishMessage(category, shortName, event.getAuthor(), guild.getPublicChannel());
								}
							}
							bot.getDispatcher().updateFileList();
						} else {
							event.getAuthor().getPrivateChannel().sendMessage("Download of file `" + name + "` failed.");
						}
						if (!event.isPrivate() && bot.hasPermissionInChannel(event.getTextChannel(), Permission.MESSAGE_MANAGE))
							event.getMessage().deleteMessage();
					} else {
						event.getAuthor().getPrivateChannel().sendMessage(
								"File `" + name + "` is too large to add to sound list. *Was this for me? This bot detects any sound upload as a possible entry.*");
					}
				}
			}
		}
	}

	private void sendPublishMessage(String category, String name, User author, TextChannel channel) {
		String end = (category != null && !category.isEmpty()) ? " to **" + category + "**" : "";
		channel.sendMessageAsync("New sound `" + name + "` added" + end + " by " + author.getAsMention() + "!", null);
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
		return "**Upload** an `.mp3` or `.wav` file to add it to list of sounds.";
	}

}
