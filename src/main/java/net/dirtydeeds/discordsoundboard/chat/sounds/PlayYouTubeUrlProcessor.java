package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayYouTubeUrlProcessor extends SingleArgumentChatCommandProcessor {

  private final String REGEX = "/youtu(?:.*\\/v\\/|.*v\\=|\\.be\\/)([A-Za-z0-9_\\-]{11})/";
  private final Pattern YOUTUBE_PATTERN = Pattern.compile(REGEX);

  public PlayYouTubeUrlProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Play YouTube Video URL", bot);
  }

  private void play(MessageReceivedEvent event, String url) {
    try {
      bot.playYouTubeVideoIDForChatCommand(url, event);
      m(event, "Playing `" + url + "`.");
    } catch (Exception e) {
      e(event, "Could not play => " + e.getMessage());
    }
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, "You're not allowed to do that.");
    } else {
      String youtubeUrl = message.substring(getPrefix().length());
      Matcher matcher = YOUTUBE_PATTERN.matcher(youtubeUrl);
      if (matcher.find()) {
        play(event, matcher.group());
      } else {
        w(event, "Could not find a valid YouTube video ID in that link.");
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + "url - play a sound by url";
  }
}
