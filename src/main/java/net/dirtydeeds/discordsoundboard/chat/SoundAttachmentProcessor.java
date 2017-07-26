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
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class SoundAttachmentProcessor extends AbstractAttachmentProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SoundAttachmentProcessor");
	private static final int MAX_FILE_SIZE_IN_BYTES = 2000000; // 2MB
	private static final int MAX_DURATION_IN_SECONDS = 12; // 12s
	private static final String WAS_THIS_FOR_ME =
	  " *Was this for me?* I watch for `.mp3`, `.wav`, `.flac`, `.m4a` files.";

	public SoundAttachmentProcessor(SoundboardBot bot) {
		super("Sound Uploader", bot);
	}

	protected boolean handleAttachment(MessageReceivedEvent event, Attachment attachment) {
		// Get the user.
		User user = event.getAuthor();

		// Get the category to upload to.
		String category = event.getMessage().getContent();

		// Get the details for the file.
		AttachmentFile file = getAttachmentDetails(attachment);

		// Check for maximum file size allowed.
		if (attachment.getSize() >= MAX_FILE_SIZE_IN_BYTES) {
			LOG.info("File " + file.name + " is too large.");
			String end = (event.isFromType(ChannelType.PRIVATE)) ?
			             "" : WAS_THIS_FOR_ME;
			pm(event, "File `" + file.name +
			   "` is too large to add to sound list (my capacity is finite)." + end);
			event.getMessage().addReaction("😶").queue();
			return false;
		}

		// Check for category path.
		Path downloadPath = bot.getSoundsPath();
		SoundboardDispatcher dispatcher = bot.getDispatcher();
		if (bot.isASoundCategory(category)) {
			for (Category _category : dispatcher.getCategories()) {
				if (_category.getName().equalsIgnoreCase(category)) {
					downloadPath = _category.getFolderPath();
					category = _category.getName();
					break;
				}
			}
		} else {
			LOG.info("No category given or found (" + category + ").");
			category = null;
		}

		// See if it already exists!
		File target = new File(downloadPath.toString(), file.name);
		LOG.info("Will download file with path: " +
		         downloadPath.toString() + " and name: " + file.name);
		if (target.exists() || bot.getSoundMap().get(file.name) != null) {
			LOG.info(user.getName() +
			         " tried to upload a file whose name already exists.");
			pm(event, "A sound with name `" + file.shortName +
			   "` **already exists**! Type `.whatis " +
			   file.shortName + "` for details.");
			return false;
		}

		// Download the file.
		if (attachment.download(target)) {
			LOG.info("Download succeeded for attachment: " +
			         attachment.getFileName());
			dispatcher.updateFileList();
			SoundFile soundFile = dispatcher.getSoundFileByName(file.shortName);
			// Check duration.
			net.dirtydeeds.discordsoundboard.beans.User u = bot.getUser(user);
			if ((u != null && u.getPrivilegeLevel() < 2 || !bot.isOwner(user)) &&
			    soundFile.getDuration() > MAX_DURATION_IN_SECONDS) {
				// Delete the file.
				LOG.info("File was too long! Deleting the file.");
				target.delete();
				dispatcher.updateFileList();
				pm(event, "File `" + file.name +
				   "` is *too long* to add to sound list. Duration: **" +
				   soundFile.getDuration() + "s**. Want *less than or equal to* **" +
				   MAX_DURATION_IN_SECONDS + "s**.");
				return false;
			}
			// Send message(s).
			if (category == null || category.isEmpty())
				category = "Uncategorized";
			pm(event, getDownloadedMessage(
			     file.name, category, soundFile, attachment.getSize()));
			if (!event.isFromType(ChannelType.PRIVATE)) {
				StyledEmbedMessage publishMessage = getPublishMessage(category,
				                                    file.shortName, user, soundFile);
				embed(event, publishMessage);
				LOG.info("Sending announcement for uploaded file.");
				if (!user.getName().equals(bot.getOwner())) { // Alert bot owner as well.
					sendPublishMessageToOwner(publishMessage);
				}
			}
		} else {
			e(event, "Download of file `" + file.name +
			  "` **failed** for an unknown reason!");
			return false;
		}

		return true;
	}

	@Override
	public boolean isApplicableCommand(MessageReceivedEvent event) {
		return super.isApplicableCommand(event) && hasApplicableAttachment(event);
	}

	private boolean hasApplicableAttachment(MessageReceivedEvent event) {
		List<Attachment> attachments = event.getMessage().getAttachments();
		for (Attachment attachment : attachments) {
			AttachmentFile file = getAttachmentDetails(attachment);
			// Only allow wav/mp3 uploads.
			if (file.extension.equals("wav") || file.extension.equals("mp3") ||
			    file.extension.equals("flac") || file.extension.equals("m4a"))
				return true;
		}
		return false;
	}

	private StyledEmbedMessage getPublishMessage(String category, String name,
	    User author, SoundFile file) {
		StyledEmbedMessage msg = new StyledEmbedMessage("A New Sound Was Added!",
		    bot);
		msg.addDescription("A new sound was added to the bot by " +
		                   author.getAsMention() + ".");
		msg.addContent("Name", "`" + name + "`", true);
		msg.addContent("Category",
		               (category != null &&
		                !category.isEmpty() &&
		                !category.equals("Uncategorized")) ? category : "\u2014",
		               true);
		msg.addContent("Duration", file.getDuration() + " seconds", true);
		return msg;
	}

	private String getDownloadedMessage(String name, String category,
	                                    SoundFile file, long size) {
		String msg = "`" + name +
		             "` downloaded and added to sounds! Play with `?" +
		             file.getSoundFileId() + "`.\n\u2014\n";
		if (category != null && !category.isEmpty() &&
		    !category.equals("Uncategorized"))
			msg += "Category: **" + category + "**\n";
		else
			msg += "**No category given** (or given didn't match an existing one)!\n";
		msg += "File Size: **" + size / 1000 + " kB**";
		return msg;
	}

	private void sendPublishMessageToOwner(StyledEmbedMessage msg) {
		User owner = bot.getUserByName(bot.getOwner());
		if (owner != null) {
			if (!owner.hasPrivateChannel()) {
				try {
					owner.openPrivateChannel().block();
					owner.getPrivateChannel().sendMessage(msg.getMessage()).queue();
				} catch (RateLimitedException e) {
					LOG.fatal("Rate-limited when trying to open PM channel with " +
					          owner.getName());
				}
			}
		}
	}

	@Override
	public boolean canBeRunBy(User user, Guild guild) {
		return !bot.isUser(user) && bot.isAuthenticated(user, guild);
	}

	@Override
	public String getCommandHelpString() {
		return "Upload (an) .mp3 OR .wav file(s) to add sounds. Use the Comment field to specify category.";
	}

}
