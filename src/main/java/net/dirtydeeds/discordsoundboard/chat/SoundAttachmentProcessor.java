package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SoundAttachmentProcessor implements ChatCommandProcessor {

	protected SoundboardBot bot;
	public static final SimpleLog LOG = SimpleLog.getLog("SoundAttachmentProcessor");
	private static final int MAX_FILE_SIZE_IN_BYTES = 2000000; // 2MB
	private static final int MAX_DURATION_IN_SECONDS = 12; // 12s
	
	public SoundAttachmentProcessor(SoundboardBot bot) {
		this.bot = bot;
	}
	
	public String getTitle() {
		return "Sound Uploader";
	}

	private boolean handleAttachment(MessageReceivedEvent event, Attachment attachment) {
		// Get the category to upload to.
		String category = event.getMessage().getContent();

		// Get the name of the file.
		String name = attachment.getFileName().toLowerCase(); // Ensure is lowercase.
		String shortName = name.substring(0, name.indexOf("."));
		String extension = name.substring(name.indexOf(".") + 1);
		
		// Only allow wav/mp3 uploads.
		if (!extension.equals("wav") && !extension.equals("mp3")) {
			return false;
		}

		if (attachment.getSize() >= MAX_FILE_SIZE_IN_BYTES) {
			String end = (event.isFromType(ChannelType.PRIVATE)) ? "" : " *Was this for me? This bot looks for `.mp3` or `.wav` uploads.*";
			event.getAuthor().getPrivateChannel().sendMessage("File `" + name + "` is too large to add to sound list." + end).queue();
			event.getMessage().addReaction("😶").queue();
			return false;
		}

		Path downloadPath = bot.getSoundsPath();
		if (bot.isASoundCategory(category)) {
			for (Category _category : bot.getDispatcher().getCategories()) {
				if (_category.getName().equalsIgnoreCase(category)) {
					downloadPath = _category.getFolderPath();
					category = _category.getName();
					break;
				}
			}
		} else {
			if (bot.getDispatcher().getNumberOfCategories() >= 1 || (category != null && !category.isEmpty())) {
				event.getAuthor().getPrivateChannel().sendMessage("No category was provided " +
						"(*or it did not match an existing one*)!").queue();
			}
			category = null;
		}

		// See if it already exists!
		File target = new File(downloadPath.toString(), name);
		LOG.info("Will download file with path: " + downloadPath.toString() + " and name: " + name);
		if (target.exists() || bot.getSoundMap().get(name) != null) {
			LOG.info("Uploader tried to upload a file whose name already exists.");
			event.getAuthor().getPrivateChannel().sendMessage("A sound with name `" + name + "` **already exists**!").queue();
			return false;
		}
		// Download the file and put it in the proper folder/category.
		else if (attachment.download(target)) {
			LOG.info("Download succeeded for attachment: " + attachment.getFileName());
			SoundboardDispatcher dispatcher = bot.getDispatcher();
			dispatcher.updateFileList();
			LOG.info("Updated sound list.");
			SoundFile soundFile = dispatcher.getSoundFileByName(shortName);
			if (soundFile == null) { LOG.fatal("Could not find uploaded file bean with name " + shortName); return false; }
			// Check duration.
			net.dirtydeeds.discordsoundboard.beans.User u = bot.getUser(event.getAuthor());
			if ((u != null && u.getPrivilegeLevel() < 2 || !bot.isOwner(event.getAuthor())) && 
					soundFile.getDuration() > MAX_DURATION_IN_SECONDS) {
				// Delete the file.
				LOG.info("File was too long! Deleting the file.");
				Long duration = soundFile.getDuration();
				target.delete();
				dispatcher.updateFileList();
				event.getAuthor().getPrivateChannel().sendMessage("File `" + name + 
						"` is too long to add to sound list. *Duration:* **" + duration + "** seconds.").queue();
			} else {
				// Send message(s).
				if (category == null || category.isEmpty()) category = "Uncategorized";
				event.getAuthor().getPrivateChannel().sendMessage(getDownloadedMessage(
						name, category, soundFile, attachment.getSize()).getMessage()).queue();
				if (!event.isFromType(ChannelType.PRIVATE)) {
					StyledEmbedMessage publishMessage = getPublishMessage(category, shortName, event.getAuthor(), soundFile);
					event.getGuild().getPublicChannel().sendMessage(publishMessage.getMessage()).queue();
					LOG.info("Sending announcement for uploaded file.");
					if (!event.getAuthor().getName().equals(bot.getOwner())) {// Alert bot owner as well.
						User owner = bot.getUserByName(bot.getOwner());
						if (owner != null) owner.getPrivateChannel().sendMessage(publishMessage.getMessage()).queue();
					}
				}
			}
		} else {
			event.getAuthor().getPrivateChannel().sendMessage("Download of file `" + name + "` **failed**!").queue();
			return false;
		}
		return true;
	}
	
	public void process(MessageReceivedEvent event) {
		if (isApplicableCommand(event)) {
			boolean downloaded = false;

			// Get all attachments from the message.
			List<Attachment> attachments = event.getMessage().getAttachments();
			
			// Process attachments.
			for (Attachment attachment : attachments) {
				if (handleAttachment(event, attachment)) downloaded = true;
			}

			// Delete original message if needed.
			if (downloaded && 
				!event.isFromType(ChannelType.PRIVATE) && 
				bot.hasPermissionInChannel(event.getTextChannel(), Permission.MESSAGE_MANAGE))
				event.getMessage().deleteMessage().queue();
		}
	}

	private StyledEmbedMessage getPublishMessage(String category, String name, User author, SoundFile file) {
		StyledEmbedMessage msg = new StyledEmbedMessage("A New Sound Was Added!", bot);
		msg.addDescription("A new sound was added to the bot by " + author.getAsMention() + ".");
		msg.addContent("Name", "`" + name + "`", true);
		if (category != null && !category.isEmpty() && !category.equals("Uncategorized")) msg.addContent("Category", category, true);
		msg.addContent("Uploader", author.getName(), true);
		msg.addContent("Duration", file.getDuration() + " seconds", true);
		return msg;
	}
	
	private StyledEmbedMessage getDownloadedMessage(String name, String category, SoundFile file, long size) {
		StyledEmbedMessage msg = new StyledEmbedMessage("New Sound Added Successfully", bot);
		msg.addDescription("`" + name + "` downloaded and added to list of sounds. Play it with `?" + file.getSoundFileId() + "`.");
		if (category != null && !category.isEmpty() && !category.equals("Uncategorized")) msg.addContent("Category", category, true);
		msg.addContent("Duration", file.getDuration() + " seconds", true);
		msg.addContent("File Size", size/1000 + " kB", true);
		return msg;
	}
	
	public boolean isApplicableCommand(MessageReceivedEvent event) {
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
		return "**Upload** an `.mp3` or `.wav` file(s) to add to sounds. *Use the Comment field to specify category.*";
	}

}
