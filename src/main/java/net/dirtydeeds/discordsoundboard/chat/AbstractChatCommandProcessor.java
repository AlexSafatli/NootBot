package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractChatCommandProcessor implements
        ChatCommandProcessor {

  private final String prefix;
  private final String slashCommand;
  private final String title;
  private List<Message> buffer;
  private InteractionHook hook;
  protected SoundboardBot bot;
  protected boolean ephemeral = false;

  public AbstractChatCommandProcessor(String prefix, String title, SoundboardBot bot) {
    this.prefix = prefix;
    this.slashCommand = null;
    this.title = title;
    this.bot = bot;
    this.buffer = new LinkedList<>();
  }

  public AbstractChatCommandProcessor(String prefix, String slashCommand, String title, SoundboardBot bot) {
    this.prefix = prefix;
    this.slashCommand = slashCommand;
    this.title = title;
    this.bot = bot;
    this.buffer = new LinkedList<>();
  }

  public void process(MessageReceivedEvent event) {
    if (!isApplicableCommand(event)) return;
    clearBuffer();
    try {
      handleEvent(event, event.getMessage().getContentRaw().toLowerCase());
    } catch (Exception ex) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      String err = sw.toString();
      e(event, "```" + StringUtils.truncate(err, 256) + "```");
      System.err.println(err);
    }
    deleteOriginalMessage(event);
  }

  public void processAsSlashCommand(SlashCommandEvent event) {
    if (!isApplicableCommand(event)) return;
    event.deferReply(ephemeral).queue();
    hook = event.getHook();
    if (ephemeral) {
      hook.setEphemeral(true);
    }
    try {
      handleEvent(event);
    } catch (Exception ex) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      String err = sw.toString();
      e(event, "```" + StringUtils.truncate(err, 256) + "```");
      System.err.println(err);
    }
    hook = null;
  }

  protected void deleteOriginalMessage(MessageReceivedEvent event) {
    if (!event.isFromType(ChannelType.PRIVATE)) delete(event.getMessage());
  }

  protected abstract void handleEvent(MessageReceivedEvent event,
                                      String message);

  protected void handleEvent(SlashCommandEvent event) {

  }

  private boolean isApplicableCommand(String cmd) {
    return (cmd.toLowerCase().startsWith(prefix) && cmd.length() > 1);
  }

  public boolean isApplicableCommand(MessageReceivedEvent event) {
    return isApplicableCommand(event.getMessage().getContentRaw()) &&
            !bot.isUser(event.getAuthor()); // Never run a command for the bot.
  }
  public boolean isApplicableCommand(SlashCommandEvent event) {
    return canBeRunAsSlashCommand() &&
            event.getName().toLowerCase().startsWith(slashCommand) &&
            !bot.isUser(event.getUser()); // Never run a command for the bot.
  }

  public boolean canBeRunByAnyone() {
    return true;
  }

  public boolean canBeRunBy(User user, Guild guild) {
    return true;
  }

  public boolean canBeRunAsSlashCommand() {
    return slashCommand != null;
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

  private void clearBuffer() {
    for (Message m : buffer) delete(m);
    buffer.clear();
  }

  protected StyledEmbedMessage buildStyledEmbedMessage(MessageReceivedEvent
                                                               event) {
    return StyledEmbedMessage.forUser(bot, event.getAuthor(), getTitle(), "");
  }

  protected StyledEmbedMessage buildStyledEmbedMessage(SlashCommandEvent
                                                               event) {
    return StyledEmbedMessage.forUser(bot, event.getUser(), getTitle(), "");
  }

  protected void pm(MessageReceivedEvent event, String message) {
    bot.sendMessageToUser(message, event.getAuthor());
  }

  protected void pm(SlashCommandEvent event, String message) {
    if (hook != null) {
      hook.setEphemeral(true);
      hook.sendMessage(message).queue();
    } else {
      bot.sendMessageToUser(message, event.getUser());
    }
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

  private void sendEmbed(SlashCommandEvent event, String message,
                         boolean error, boolean warning) {
    StyledEmbedMessage em = makeEmbed(
            message, event.getUser()).isWarning(warning).isError(error);
    embed(event, em);
  }

  protected void m(MessageReceivedEvent event, String message) {
    sendEmbed(event, message, false, false);
  }

  protected void m(SlashCommandEvent event, String message) {
    sendEmbed(event, message, false, false);
  }

  protected void w(MessageReceivedEvent event, String message) {
    sendEmbed(event, message, false, true);
  }

  protected void w(SlashCommandEvent event, String message) {
    sendEmbed(event, message, false, true);
  }

  protected void e(MessageReceivedEvent event, String message) {
    StyledEmbedMessage msg = makeEmbed(message, event.getAuthor());
    msg.addContent("Message", "`" +
            event.getMessage().getContentRaw() + "`", false);
    msg.addContent("Processor", "`" +
            getClass().getSimpleName() + "`", true);
    embed(event, msg.isError(true));
  }

  protected void e(SlashCommandEvent event, String message) {
    StyledEmbedMessage msg = makeEmbed(message, event.getUser());
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

  protected void embed(SlashCommandEvent event, StyledEmbedMessage em) {
    if (hook != null) {
      hook.sendMessageEmbeds(em.getMessage().getEmbeds()).queue();
    } else {
      TextChannel channel = (bot.hasPermissionInChannel(
              event.getTextChannel(), Permission.MESSAGE_WRITE)) ?
              event.getTextChannel() : bot.getBotChannel(event.getGuild());
      if (channel != null && em != null) {
        channel.sendMessage(em.getMessage()).queue((Message msg) -> buffer.add(msg));
      }
    }
  }

  protected void embedForUser(MessageReceivedEvent event,
                              StyledEmbedMessage em) {
    //em.addFooterText(StyledEmbedMessage.FOR_USER_FOOTER_PREFIX +
    //                 event.getAuthor().getName());
    em.setFooterIcon(event.getAuthor().getEffectiveAvatarUrl());
    embed(event, em);
  }

  protected void embedForUser(SlashCommandEvent event,
                              StyledEmbedMessage em) {
    //em.addFooterText(StyledEmbedMessage.FOR_USER_FOOTER_PREFIX +
    //                 event.getAuthor().getName());
    em.setFooterIcon(event.getUser().getEffectiveAvatarUrl());
    embed(event, em);
  }

  public String getCommandHelpString() {
    return getPrefix();
  }

}
