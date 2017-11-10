package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class FavoritePhraseProcessor extends
  SingleArgumentChatCommandProcessor {

  public FavoritePhraseProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Favorite Phrase", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String m = event.getMessage().getContent(),
           phrase = m.substring(getPrefix().length() + 1).trim();
    if (phrase == null) {
      pm(event, "You didn't give me anything.");
    } else {
      StringUtils.cacheFavoritePhrase(phrase);
      pm(event, "Included phrase `" + phrase +
         "` in list of possible game names!");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <phrase> - let the bot know about a phrase you like";
  }
}