package net.dirtydeeds.discordsoundboard.chat.sounds;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.AbstractAttachmentProcessor;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class SoundAttachmentProcessor extends AbstractAttachmentProcessor {

  public static final SimpleLog LOG =
          SimpleLog.getLog("SoundAttachmentProcessor");
  private static final int MAX_FILE_SIZE_IN_BYTES = 2000000; // 2MB
  private static final int MAX_DURATION_IN_SECONDS = 12; // 12s
  private static final String WAS_THIS_FOR_ME =
          " *Was this for me?* I watch for `.mp3`, `.wav`, `.flac`, `.m4a` files.";

  public SoundAttachmentProcessor(SoundboardBot bot) {
    super("Sound Upload", bot);
  }

  protected boolean handleAttachment(MessageReceivedEvent event,
                                     Attachment attachment) {
    User user = event.getAuthor();
    String category = event.getMessage().getContent();
    AttachmentFile file = getAttachmentDetails(attachment);

    // Check for maximum file size allowed.
    if (attachment.getSize() >= MAX_FILE_SIZE_IN_BYTES) {
      LOG.info("File " + file.name + " is too large.");
      String end = (event.isFromType(ChannelType.PRIVATE)) ?
              "" : WAS_THIS_FOR_ME;
      pm(event, "File `" + file.name + "` is too large." + end);
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
      LOG.info(user.getName() + " tried to upload a file that already exists.");
      pm(event, "A sound with the name `" + file.shortName +
              "` already exists! Type `.whatis " + file.shortName +
              "` for details.");
      return false;
    }

    // Download the file.
    if (attachment.download(target)) {
      LOG.info("Download succeeded: " + attachment.getFileName());
      dispatcher.updateFileList();
      SoundFile soundFile = dispatcher.getSoundFileByName(file.shortName);
      if (soundFile == null) {
        e(event, "Something went wrong - could not find downloaded file.");
        return false;
      }
      // Check duration.
      net.dirtydeeds.discordsoundboard.beans.User u = bot.getUser(user);
      if ((u != null && u.getPrivilegeLevel() < 2 || !bot.isOwner(user)) &&
              soundFile.getDuration() > MAX_DURATION_IN_SECONDS) {
        // Delete the file.
        LOG.info("File was too long! Deleting the file.");
        if (!target.delete()) LOG.warn("Could not delete file.");
        dispatcher.updateFileList();
        pm(event,
                "File `" + file.name + "` is *too long* (**" +
                        soundFile.getDuration() + "s**). Want *less than or equal to* **" +
                        MAX_DURATION_IN_SECONDS + "s**.");
        return false;
      }

      // Send message(s).
      pm(event, getDownloadedMessage(
              file.name, category, soundFile, attachment.getSize()));
      StyledEmbedMessage publishMessage = getPublishMessage(category,
              file.shortName, user, soundFile, event.getGuild());
      if (!event.isFromType(ChannelType.PRIVATE)) embed(event, publishMessage);
      if (!user.getName().equals(bot.getOwner())) { // Alert bot owner too.
        sendPublishMessageToOwner(publishMessage);
        LOG.info("Sent information for uploaded file to owner.");
      }
    } else {
      e(event, "Download of file `" + file.name + "` failed!");
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
      if (file.extension.equals("wav") || file.extension.equals("mp3") ||
              file.extension.equals("flac") || file.extension.equals("m4a"))
        return true;
    }
    return false;
  }

  private StyledEmbedMessage getPublishMessage(String category, String name,
                                               User author, SoundFile file,
                                               Guild guild) {
    category = ((category != null) &&
            !category.isEmpty() &&
            !category.equals("Uncategorized")) ? category : "\u2014";

    StyledEmbedMessage msg =
            new StyledEmbedMessage("A New Sound Was Added!", bot);
    msg.addDescription(author.getAsMention() + " added a new sound. " +
                    "Play it with `?" + file.getSoundFileId() + "`.");
    msg.addContent("Name", "`" + name + "`", true);
    msg.addContent("Category", category, true);
    if (file.getDuration() != null) {
      msg.addContent("Duration", file.getDuration() + " seconds", true);
    }
    if (guild != null) {
      msg.addContent("Server", guild.getName(), false);
    }
    Color color = StringUtils.toColor(name);
    msg.setColor(color);
    return msg;
  }

  private String getDownloadedMessage(String name, String category,
                                      SoundFile file, long size) {
    String msg = "`" + name + "` downloaded (**om nom nom**) and added!" +
            " Play with `?" + file.getSoundFileId() + "`.\n\u2014\n";
    if (category != null && !category.isEmpty() &&
            !category.equals("Uncategorized"))
      msg += "Category: **" + category + "**\n";
    else msg += "No category given (or given didn't match an existing one)!\n";
    msg += "File Size: **" + size / 1000 + " kB**";
    return msg;
  }

  private void sendPublishMessageToOwner(StyledEmbedMessage msg) {
    SoundboardBot sender = bot;
    User owner = bot.getUserByName(bot.getOwner());
    for (SoundboardBot b : bot.getDispatcher().getBots()) {
      if (owner != null) break;
      else if (b.equals(bot)) continue;
      owner = b.getUserByName(bot.getOwner());
      sender = b;
    }
    if (owner != null) {
      if (!sender.equals(bot)) {
        LOG.info("Sending message to owner via different bot " + sender.getBotName());
        msg.addContent("Bot", bot.getBotName(), false);
      }
      owner.openPrivateChannel().queue((PrivateChannel c) ->
        c.sendMessage(msg.getMessage()).queue());
    }
  }

  @Override
  public boolean canBeRunBy(User user, Guild guild) {
    return !bot.isUser(user) && bot.isAuthenticated(user, guild);
  }

  @Override
  public String getCommandHelpString() {
    return "Upload (an) .mp3, .flac, .m4a OR .wav file(s) to add sounds." +
            " Use the Comment field to specify category.";
  }
}
