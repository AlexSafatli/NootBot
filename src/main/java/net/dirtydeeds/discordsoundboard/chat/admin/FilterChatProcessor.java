package net.dirtydeeds.discordsoundboard.chat.admin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.*;

import net.dirtydeeds.discordsoundboard.chat.ChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FilterChatProcessor implements ChatCommandProcessor {

  private static final String FILTER_FORMAT_STRING = "%s\n\u2014\nOriginally " +
          "posted by %s.\n*This was filtered to this channel because it " +
          "contained %s.*";

  private final Pattern regexp;
  private final List<String> channelnames;
  private final String patternname;
  private boolean deleteOriginal;
  protected SoundboardBot bot;

  public FilterChatProcessor(Pattern regexp, String cname,
                             SoundboardBot bot) {
    this(regexp, new String[] { cname }, "a certain pattern", true, bot);
  }

  public FilterChatProcessor(Pattern regexp, String cname,
                             String pname, SoundboardBot bot) {
    this(regexp, new String[] { cname }, pname, true, bot);
  }

  public FilterChatProcessor(Pattern regexp, String[] cnames,
                             String pname, boolean delete, SoundboardBot bot) {
    this.regexp = regexp;
    this.channelnames = Arrays.asList(cnames);
    this.patternname = pname;
    this.bot = bot;
    this.deleteOriginal = delete;
  }

  public void process(MessageReceivedEvent event) {
    if (!isApplicableCommand(event)) return;
    try {
      copyMessage(event, event.getMessage().getContentRaw());
      if (deleteOriginal) deleteOriginalMessage(event);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  protected void copyMessage(MessageReceivedEvent event, String message) {
    List<TextChannel> textChannels = new LinkedList<>();
    for (String cname : channelnames) {
      textChannels.addAll(event.getGuild().getTextChannelsByName(cname, true));
    }
    String modified = String.format(FILTER_FORMAT_STRING, message,
            event.getAuthor().getAsMention(),
            patternname);
    for (TextChannel channel : textChannels) {
      channel.sendMessage(modified).queue();
    }
  }

  protected void deleteOriginalMessage(MessageReceivedEvent event) {
    if (!event.isFromType(ChannelType.PRIVATE)) delete(event.getMessage());
  }

  public boolean isApplicableCommand(MessageReceivedEvent event) {
    if (event.getGuild() == null) return false;
    List<TextChannel> textChannels = new LinkedList<>();
    for (String cname : channelnames) {
      textChannels.addAll(event.getGuild().getTextChannelsByName(cname, true));
    }
    Matcher m = regexp.matcher(event.getMessage().getContentRaw());
    return !textChannels.isEmpty()
            && !event.isFromType(ChannelType.PRIVATE)
            && !textChannels.contains(event.getTextChannel())
            && m.matches();
  }

  public boolean canBeRunByAnyone() {
    return true;
  }

  public boolean canBeRunBy(User user, Guild guild) {
    return true;
  }

  private void delete(Message m) {
    if (bot.hasPermissionInChannel(m.getTextChannel(),
            Permission.MESSAGE_MANAGE))
      m.delete().queue();
  }

  public String getTitle() {
    return "";
  }

  public String getCommandHelpString() {
    return "";
  }
}