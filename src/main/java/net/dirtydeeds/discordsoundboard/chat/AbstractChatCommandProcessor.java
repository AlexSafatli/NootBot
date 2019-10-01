package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractChatCommandProcessor implements
        ChatCommandProcessor {

  private final String prefix;
  private final String title;
  private List<Message> buffer;
  protected SoundboardBot bot;

  public AbstractChatCommandProcessor(String prefix, String title, SoundboardBot bot) {
    this.prefix = prefix;
    this.title = title;
    this.bot = bot;
    this.buffer = new LinkedList<>();
  }

  public void process(MessageReceivedEvent event) {
    if (!isApplicableCommand(event)) return;
    clearBuffer();
    try {
      handleEvent(event, event.getMessage().getContent().toLowerCase());
    } catch (Exception ex) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      String err = sw.toString();
      e(event, "```" + StringUtils.truncate(err, 256) + "```");
      System.err.println(err);
    }
    deleteOriginalMessage(event);
  }

  protected void deleteOriginalMessage(MessageReceivedEvent event) {
    if (!event.isFromType(ChannelType.PRIVATE)) delete(event.getMessage());
  }

  protected abstract void handleEvent(MessageReceivedEvent event,
                                      String message);

  private boolean isApplicableCommand(String cmd) {
    return (cmd.toLowerCase().startsWith(prefix) && cmd.length() > 1);
  }

  public boolean isApplicableCommand(MessageReceivedEvent event) {
    return isApplicableCommand(event.getMessage().getContent()) &&
            !bot.isUser(event.getAuthor()); // Never run a command for the bot.
  }

  public boolean canBeRunByAnyone() {
    return true;
  }

  public boolean canBeRunBy(User user, Guild guild) {
    return true;
  }

  public String getPrefix() {
    return this.prefix;
  }

  public String getTitle() {
    return this.title;
  }

  private void delete(Message m) {
    if (bot.hasPermissionInChannel(m.getTextChannel(), Permission.MESSAGE_MANAGE))
      m.delete().queue();
  }

  protected void clearBuffer() {
    for (Message m : buffer) delete(m);
    buffer.clear();
  }

  protected StyledEmbedMessage buildStyledEmbedMessage(MessageReceivedEvent
                                                               event) {
    return StyledEmbedMessage.forUser(bot, event.getAuthor(), getTitle(), "");
  }

  protected void pm(MessageReceivedEvent event, String message) {
    bot.sendMessageToUser(message, event.getAuthor());
  }

  protected void pm(MessageReceivedEvent event, StyledEmbedMessage message) {
    event.getAuthor().openPrivateChannel().queue((PrivateChannel c) ->
      c.sendMessage(message.getMessage()).queue());
  }

  private StyledEmbedMessage makeEmbed(String message, User user) {
    return StyledEmbedMessage.forUser(bot, user, getTitle(), message);
  }

  private void sendTyping(MessageReceivedEvent event) {
    if (!event.isFromType(ChannelType.PRIVATE) && event.getGuild() != null) {
      TextChannel c = bot.getBotChannel(event.getGuild());
      c.sendTyping().queue();
    }
  }

  private void sendEmbed(MessageReceivedEvent event, String message,
                         boolean error, boolean warning) {
    StyledEmbedMessage em = makeEmbed(
            message, event.getAuthor()).isWarning(warning).isError(error);
    embed(event, em);
  }

  protected void m(MessageReceivedEvent event, String message) {
    sendEmbed(event, message, false, false);
  }

  protected void w(MessageReceivedEvent event, String message) {
    sendEmbed(event, message, false, true);
  }

  protected void e(MessageReceivedEvent event, String message) {
    StyledEmbedMessage msg = makeEmbed(message, event.getAuthor());
    msg.addContent("Message", "`" +
            event.getMessage().getContent() + "`", false);
    msg.addContent("Processor", "`" +
            getClass().getSimpleName() + "`", true);
    embed(event, msg.isError(true));
  }

  protected void embed(MessageReceivedEvent event, StyledEmbedMessage em) {
    if (event.isFromType(ChannelType.PRIVATE)) {
      pm(event, em);
      return;
    }
    sendTyping(event);
    TextChannel channel = (bot.hasPermissionInChannel(
            event.getTextChannel(), Permission.MESSAGE_WRITE)) ?
            event.getTextChannel() : bot.getBotChannel(event.getGuild());
    if (channel != null && em != null) {
      channel.sendMessage(em.getMessage()).queue((Message msg) -> buffer.add(msg));
    }
  }

  protected void embedForUser(MessageReceivedEvent event,
                              StyledEmbedMessage em) {
    //em.addFooterText(StyledEmbedMessage.FOR_USER_FOOTER_PREFIX +
    //                 event.getAuthor().getName());
    em.setFooterIcon(event.getAuthor().getEffectiveAvatarUrl());
    embed(event, em);
  }

  public String getCommandHelpString() {
    return getPrefix();
  }

}
