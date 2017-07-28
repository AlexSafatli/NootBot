package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public abstract class AbstractAttachmentProcessor implements
  ChatCommandProcessor {

  private final String title;
  protected SoundboardBot bot;

  public AbstractAttachmentProcessor(String title, SoundboardBot bot) {
    this.title = title;
    this.bot = bot;
  }

  protected class AttachmentFile {
    public String name;
    public String shortName;
    public String extension;

    public AttachmentFile(Attachment attachment) {
      name = attachment.getFileName().toLowerCase(); // Ensure is lowercase.
      shortName = name.substring(0, name.indexOf("."));
      extension = name.substring(name.indexOf(".") + 1);
    }

    public String toString() {
      return "Attachment [" + name + "]";
    }
  }

  public void process(MessageReceivedEvent event) {
    if (!isApplicableCommand(event)) return;
    boolean succeeded = false;

    // Get all attachments from the message.
    List<Attachment> attachments = event.getMessage().getAttachments();

    // Process attachments.
    for (Attachment attachment : attachments) {
      try {
        if (handleAttachment(event, attachment))
          succeeded = true;
      } catch (Exception ex) {
        e(event, getAttachmentDetails(attachment) + "\n" + ex.getMessage());
        ex.printStackTrace();
      }
    }

    // Delete original message if needed.
    if (succeeded &&
        !event.isFromType(ChannelType.PRIVATE) &&
        bot.hasPermissionInChannel(event.getTextChannel(),
                                   Permission.MESSAGE_MANAGE))
      delete(event.getMessage());
  }

  protected abstract boolean handleAttachment(MessageReceivedEvent event, Attachment attachment);

  public boolean isApplicableCommand(MessageReceivedEvent event) {
    return (!event.getMessage().getAttachments().isEmpty() &&
            canBeRunBy(event.getAuthor(), event.getGuild()));
  }

  public boolean canBeRunByAnyone() {
    return true;
  }

  public boolean canBeRunBy(User user, Guild guild) {
    return true;
  }

  public String getTitle() {
    return this.title;
  }

  protected AttachmentFile getAttachmentDetails(Attachment attachment) {
    return new AttachmentFile(attachment);
  }

  private void delete(Message m) {
    if (bot.hasPermissionInChannel(m.getTextChannel(),
                                   Permission.MESSAGE_MANAGE))
      m.deleteMessage().queue();
  }

  protected void pm(MessageReceivedEvent event, String message) {
    bot.sendMessageToUser(message, event.getAuthor());
  }

  protected void pm(MessageReceivedEvent event, StyledEmbedMessage message) {
    if (!event.getAuthor().hasPrivateChannel()) {
      try {
        event.getAuthor().openPrivateChannel().block();
      } catch (RateLimitedException e) {
        embed(event, message);
        return;
      }
    }
    event.getAuthor().getPrivateChannel().sendMessage(
      message.getMessage()).queue();
  }

  private StyledEmbedMessage makeEmbed(String message) {
    StyledEmbedMessage em = new StyledEmbedMessage(getTitle(), bot);
    em.addDescription(message);
    return em;
  }

  private void sendEmbed(MessageReceivedEvent event, String message,
                         boolean error, boolean warning) {
    if (event.isFromType(ChannelType.PRIVATE)
        || !bot.hasPermissionInChannel(event.getTextChannel(),
                                       Permission.MESSAGE_WRITE)) {
      pm(event, message);
    } else {
      event.getChannel().sendMessage(
        makeEmbed(message).isWarning(warning).isError(error).getMessage()
      ).queue();
    }
  }

  protected void m(MessageReceivedEvent event, String message) {
    sendEmbed(event, message, false, false);
  }

  protected void w(MessageReceivedEvent event, String message) {
    sendEmbed(event, message, false, true);
  }

  protected void e(MessageReceivedEvent event, String message) {
    sendEmbed(event, message, true, false);
  }

  protected void embed(MessageReceivedEvent event, StyledEmbedMessage
                       embed) {
    if (!event.isFromType(ChannelType.PRIVATE)) {
      event.getGuild().getPublicChannel().sendMessage(
        embed.getMessage()).queue();
    } else {
      event.getChannel().sendMessage(embed.getMessage()).queue();
    }
  }

  protected String lookupString(String key) {
    String value = bot.getDispatcher().getStringService().lookup(key);
    return (value != null) ? value : "<String Not Found: " + key + ">";
  }

  protected String formatString(String key, Object... args) {
    return String.format(lookupString(key), args);
  }

  public String getCommandHelpString() {
    return "";
  }
}