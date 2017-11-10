package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class UnfavoritePhraseProcessor extends
  SingleArgumentChatCommandProcessor {

  public UnfavoritePhraseProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Unfavorite Phrase", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String m = event.getMessage().getContent(),
           phrase = m.substring(getPrefix().length() + 1).trim();
    if (phrase == null) {
      pm(event, "You didn't give me anything.");
    } else {
      if (bot.getDispatcher().removePhrase(phrase)) {
        pm(event, "Excluded phrase `" + phrase +
           "` from list of possible game names!");
      } else {
        pm(event, "Didn't have phrase `" + phrase +
           "` in list of possible game names!");
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <phrase> - let the bot know about a phrase you dislike";
  }
}