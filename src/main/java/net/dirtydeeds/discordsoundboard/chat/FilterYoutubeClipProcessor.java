package net.dirtydeeds.discordsoundboard.chat;

import java.util.regex.*;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.*;

public class FilterYoutubeClipProcessor extends FilterChatProcessor {

  private static final String CHANNEL = "videos";

  public FilterYoutubeClipProcessor(SoundboardBot bot) {
    super(Pattern.compile(".*youtube\\.com/watch.*"), CHANNEL,
          "a YouTube link", bot);
  }
}