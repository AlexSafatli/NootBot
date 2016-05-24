package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message.Attachment;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class SoundAttachmentProcessor implements ChatCommandProcessor {

	protected SoundboardBot bot;
	private static final int MAX_FILE_SIZE_IN_BYTES = 1000000; // 1 MB
	
	public SoundAttachmentProcessor(SoundboardBot bot) {
		this.bot = bot;
	}
	
	public void process(MessageReceivedEvent event) {
		if (isApplicableCommand(event)) {
			List<Attachment> attachments = event.getMessage().getAttachments();
			for (Attachment attachment : attachments) {
				String name = attachment.getFileName();
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
									"Downloaded file `" + name + "` and added to list of sounds.");
							bot.getDispatcher().updateFileList();
						} else {
							event.getAuthor().getPrivateChannel().sendMessage(
									"Download of file `" + name + "` failed.");
						}
						if (!event.isPrivate() && bot.hasPermissionInChannel(
								event.getTextChannel(), Permission.MESSAGE_MANAGE))
							event.getMessage().deleteMessage();
					} else {
						event.getChannel().sendMessageAsync(
								"File `" + name + "` is too large to add to library.", null);
					}
				}
			}
		}
	}

	public boolean isApplicableCommand(MessageReceivedEvent event) {
		String author = event.getAuthor().getUsername();
		return (!event.getMessage().getAttachments().isEmpty() && 
				author.equals(bot.getOwner()));
	}
	
	public boolean canBeRunByAnyone() {
		return false;
	}
	
	public String getCommandHelpString() {
		return "**Upload** an `.mp3` or `.wav` file if you are the bot owner to add it to list of sounds.";
	}

}
