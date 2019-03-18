package net.dirtydeeds.discordsoundboard.chat.admin;

import java.util.regex.*;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;

public class FilterYoutubeClipProcessor extends FilterChatProcessor {

  private static final String CHANNEL = "videos";

  public FilterYoutubeClipProcessor(SoundboardBot bot) {
    super(Pattern.compile(".*youtube\\.com/watch.*"), CHANNEL,
          "a YouTube link", bot);
  }
}