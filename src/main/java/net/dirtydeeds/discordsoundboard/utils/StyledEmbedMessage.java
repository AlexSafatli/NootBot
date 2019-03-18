package net.dirtydeeds.discordsoundboard.utils;

import java.awt.Color;
import java.util.List;
import java.util.Arrays;

import net.dirtydeeds.discordsoundboard.Icons;
import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class StyledEmbedMessage {

  private EmbedBuilder builder;
  private String footer;
  private String footerIconUrl;
  private String errorTitle;

  private static final List<String> ERROR_STRINGS = Arrays.asList(
          "Fwubbed it.", "Excuse me, I stuttered.", "Nani?",
          "Derp", "Bwah!");

  private static final String FOOTER_TEXT = Version.NAME + " " +
          Version.VERSION + " by " + Version.AUTHOR;
  private static final String DEFAULT_TOP = " ";
  private static final Color EMBED_COLOR = new Color(87, 70, 158);
  private static final Color ERROR_COLOR = new Color(179, 0, 0);
  private static final Color WARN_COLOR = new Color(255, 217, 0);

  public StyledEmbedMessage() {
    footerIconUrl = Icons.ELLIPSIS;
    errorTitle = StringUtils.randomString(ERROR_STRINGS);
    builder = new EmbedBuilder();
    builder.setColor(EMBED_COLOR);
  }

  public StyledEmbedMessage(String title) {
    this();
    footer = FOOTER_TEXT;
    builder.setTitle(title);
    builder.setAuthor(DEFAULT_TOP, null, null);
    updateFooter();
  }

  public StyledEmbedMessage(String title, SoundboardBot bot) {
    this(title);
    String numSounds = bot.getSoundMap().size() + " sounds";
    footer = bot.getBotName() + Strings.SEPARATOR + numSounds;
    footerIconUrl = bot.getAPI().getSelfUser().getEffectiveAvatarUrl();
    updateFooter();
  }

  private void updateFooter() {
    builder.setFooter(footer, footerIconUrl);
  }

  public void setTitle(String title) {
    builder.setTitle(title);
  }

  public void addDescription(String desc) {
    builder.setDescription(desc);
  }

  public void setColor(Color color) {
    builder.setColor(color);
  }

  public void setImage(String url) {
    builder.setImage(url);
  }

  public void setThumbnail(String url) {
    builder.setThumbnail(url);
  }

  public void setFooter(String footer) {
    this.footer = footer;
    updateFooter();
  }

  public void setFooterIcon(String url) {
    footerIconUrl = url;
    updateFooter();
  }

  public void addFooterText(String text) {
    footer += Strings.SEPARATOR + text;
    updateFooter();
  }

  public StyledEmbedMessage isWarning(boolean warning) {
    if (warning) {
      builder.setColor(WARN_COLOR);
      builder.setAuthor(DEFAULT_TOP, null, null);
    }
    return this;
  }

  public StyledEmbedMessage isError(boolean error) {
    if (error) {
      builder.setColor(ERROR_COLOR);
      builder.setAuthor(errorTitle, null, Icons.TIMES);
    }
    return this;
  }

  public void addContent(String name, String value, boolean inline) {
    builder.addField(name, value, inline);
  }

  public Message getMessage() {
    MessageBuilder mb = new MessageBuilder();
    mb.setEmbed(builder.build());
    return mb.build();
  }

  public static StyledEmbedMessage forSoundFile(SoundboardBot bot,
                                                SoundFile file, String title,
                                                String description) {
    StyledEmbedMessage msg = new StyledEmbedMessage(title, bot);
    msg.addDescription(description);
    msg.addContent("Category", (!file.getCategory().equals("sounds")) ?
            file.getCategory() : "\u2014", true);
    msg.addContent("Name", "`" + file.getSoundFileId() + "`", true);
    msg.addContent("Played", file.getNumberOfPlays() + " times", true);
    msg.addContent("Duration", file.getDuration() + " seconds", true);
    if (file.getNumberOfReports() > 0) {
      msg.addContent("Reported", file.getNumberOfReports() + " times", true);
    }
    if (file.getSoundFile() != null) {
      String stamp = StringUtils.dayTimeStamp(file.getLastModified());
      if (!stamp.isEmpty()) msg.addContent("Added", stamp, true);
    }
    List<net.dirtydeeds.discordsoundboard.beans.User> usersWithEntrance =
            bot.getDispatcher().getUsersWithEntrance(file.getSoundFileId());
    if (!usersWithEntrance.isEmpty()) {
      msg.addContent("Entrance For",
              StringUtils.listToString(usersWithEntrance), true);
    }
    Color color = StringUtils.toColor(file.getSoundFileId());
    msg.setColor(color);
    return msg;
  }

  public static StyledEmbedMessage forUser(SoundboardBot bot, User user,
                                           String title, String description) {
    StyledEmbedMessage msg = new StyledEmbedMessage(title, bot);
    VoiceChannel usersChannel, botsChannel = null;
    if (!description.isEmpty()) msg.addDescription(description);
    try {
      usersChannel = bot.getUsersVoiceChannel(user);
      if (usersChannel != null)
        botsChannel = bot.getConnectedChannel(usersChannel.getGuild());
    } catch (Exception e) {
      usersChannel = null;
      botsChannel = null;
    }
    if (usersChannel != null && usersChannel.equals(botsChannel)) {
      msg.addFooterText(StringUtils.truncate(usersChannel.getName(), 20));
    }
    msg.setFooterIcon(user.getEffectiveAvatarUrl());
    Color color = StringUtils.toColor(user.getName());
    msg.setColor(color);
    return msg;
  }

  public static StyledEmbedMessage forMember(SoundboardBot bot, Member member,
                                           String title, String description) {
    return forUser(bot, member.getUser(), title, description);
  }


}

