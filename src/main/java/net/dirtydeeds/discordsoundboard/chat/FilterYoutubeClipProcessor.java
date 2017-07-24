package net.dirtydeeds.discordsoundboard.chat;

import java.util.regex.*;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.*;

public class FilterTwitchClipProcessor extends FilterChatProcessor {

  private static final String CHANNEL = "clips";

  public FilterYoutubeClipProcessor(SoundboardBot bot) {
    super(Pattern.compile(".*youtube\\.com/watch/.*"), CHANNEL, bot);
  }
 
}
