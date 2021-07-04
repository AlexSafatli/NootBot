package net.dirtydeeds.discordsoundboard.chat.sounds;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.AbstractAttachmentProcessor;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.api.exceptions.*;

public class SoundAttachmentProcessor extends AbstractAttachmentProcessor {

  private static final int MAX_FILE_SIZE_IN_BYTES = 2000000; // 2MB
  private static final int MAX_DURATION_IN_SECONDS = 12; // s
  private static final String WAS_THIS_FOR_ME =
          "*Was this for me?* I want `.mp3`, `.wav`, `.flac`, `.m4a` files.";

  public SoundAttachmentProcessor(SoundboardBot bot) {
    super("Sound Upload", bot);
  }

  protected boolean handleAttachment(MessageReceivedEvent event,
                                     Attachment attachment) {
    String category = event.getMessage().getContentRaw();
    AttachmentFile file = getAttachmentDetails(attachment);

    // Check for maximum file size allowed.
    if (attachment.getSize() >= MAX_FILE_SIZE_IN_BYTES) {
      String end = (event.isFromType(ChannelType.PRIVATE)) ?
              "" : WAS_THIS_FOR_ME;
      pm(event, "File `" + file.name + "` is too large." + end);
      event.getMessage().addReaction("😶").queue();
      return false;
    }

    // Check for category path.
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
      category = null;
    }

    // See if it already exists!
    File target = new File(downloadPath.toString(), file.name);
    if (target.exists() || bot.getSoundMap().get(file.name) != null) {
      pm(event, "A sound with the name `" + file.shortName +
              "` already exists!");
      event.getMessage().addReaction("😶").queue();
      return false;
    }

    // Download the file.
    try {
      if (attachment.downloadToFile(target).get() != null) {
        JDALogger.getLog("Upload").info("Download succeeded: " +
                attachment.getFileName());
        bot.getDispatcher().updateFileList();
        SoundFile soundFile = bot.getDispatcher().getSoundFileByName(
                file.shortName);
        if (soundFile == null) {
          e(event, "Oops! Could not find downloaded file by name.");
          return false;
        }
        // Check duration.
        User user = event.getAuthor();
        net.dirtydeeds.discordsoundboard.beans.User u = bot.getUser(user);
        if ((u != null && u.getPrivilegeLevel() < 2 || !bot.isOwner(user)) &&
                soundFile.getDuration() > MAX_DURATION_IN_SECONDS) {
          // Delete the file.
          JDALogger.getLog("Upload").info(
                  "File was too long! Deleting the file.");
          if (!target.delete()) JDALogger.getLog("Upload").warn(
                  "Could not delete file.");
          bot.getDispatcher().updateFileList();
          pm(event,
                  "File `" + file.name + "` is *too long* (**" +
                          soundFile.getDuration() + "s**). Want <= **" +
                          MAX_DURATION_IN_SECONDS + "s**.");
          return false;
        }

        // Send message(s).
        pm(event, getDownloadedMessage(
                file.name, category, soundFile, attachment.getSize()));
        StyledEmbedMessage publishMessage = getPublishMessage(category,
                file.shortName, user, soundFile, event.getGuild());
        if (!event.isFromType(ChannelType.PRIVATE))
          embed(event, publishMessage);
        if (!user.getName().equals(bot.getOwner())) { // Alert bot owner too.
          sendPublishMessageToOwner(publishMessage);
          JDALogger.getLog("Upload").info(
                  "Sent information for uploaded file to owner.");
        }
      } else {
        e(event, "Download of file `" + file.name + "` failed!");
        return false;
      }
    } catch (InterruptedException | ExecutionException e) {
      e(event, "Download of file failed!");
      return false;
    }

    return true;
  }

  public void processAsSlashCommand(SlashCommandEvent event) {

  }

  @Override
  public boolean isApplicableCommand(MessageReceivedEvent event) {
    return super.isApplicableCommand(event) && hasApplicableAttachment(event);
  }

  public boolean isApplicableCommand(SlashCommandEvent event) {
    return false;
  }

  private boolean hasApplicableAttachment(MessageReceivedEvent event) {
    List<Attachment> attachments = event.getMessage().getAttachments();
    for (Attachment attachment : attachments) {
      AttachmentFile file = getAttachmentDetails(attachment);
      String ext = file.extension;
      if (ext.equals("wav") || ext.equals("mp3") || ext.equals("flac") ||
              ext.equals("m4a"))
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
      msg.addContent("Duration", file.getDuration() + " seconds",
              true);
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
        JDALogger.getLog("Upload").info(
                "Sending message to owner via different bot " +
                        sender.getBotName());
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

  public boolean canBeRunAsSlashCommand() {
    return false;
  }

  @Override
  public String getCommandHelpString() {
    return "Upload (an) .mp3, .flac, .m4a OR .wav file(s) to add sounds." +
            " Use the Comment field to specify category.";
  }
}
